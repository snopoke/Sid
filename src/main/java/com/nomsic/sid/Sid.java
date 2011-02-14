/******************************************************************
 *  Copyright 2011 Simon Kelly
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  Sid.java
 */
package com.nomsic.sid;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

/**
 * Sid builds Sørensen similarity indexes between records based on the records
 * fields. 
 * 
 * @author Simon Kelly <simongdkelly@gmail.com>
 */
public class Sid {

	private static final Logger log = LoggerFactory.getLogger(Sid.class);

	/**
	 * This map contains entries whose key is a recordId and value is a sorted
	 * set of Simsets that the record belongs to i.e. the record is present in
	 * each Simset
	 */
	private SortedMap<String, Set<Simset>> recordSimsetMap;

	/**
	 * This map contains entries whose key is a recordId and value is the number
	 * of fields that the record contains.
	 */
	private Map<String, Integer> recordFieldCountMap;

	private File recordFile;

	private Collection<SortedSet<String>> recordSets;

	public boolean ignoreFirstLine;

	public Sid() {
		recordSimsetMap = Maps.newTreeMap();
		recordFieldCountMap = Maps.newHashMap();
	}

	private void buildRecordFieldCountMap() {
		for (String recordId : recordSimsetMap.keySet()) {
			int recordFieldCount = getRecordFieldCount(recordId);
			recordFieldCountMap.put(recordId, recordFieldCount);
		}
	}

	/**
	 * Given the cardinality map of the record field sets this method creates
	 * the Simsets and populates the recordSimsetMap
	 * 
	 * @param recordSetCardinalityMap
	 *            map with key equal to a set of recordIds and value equal to
	 *            the number of times that set of recordIds appeared in the
	 *            original set of record sets
	 */
	private void buildRecordSimsetMap(Map<SortedSet<String>, Integer> recordSetCardinalityMap) {
		for (Entry<SortedSet<String>, Integer> entry : recordSetCardinalityMap.entrySet()) {
			Simset simset = new Simset(entry.getValue(), entry.getKey());
			for (String recordId : simset.getRecordSet()) {
				if (recordSimsetMap.containsKey(recordId)) {
					boolean add = recordSimsetMap.get(recordId).add(simset);
					if (!add){
						log.error("Simset '{}' not added for record '{}'", simset, recordId);
					}
				} else {
					Set<Simset> treeSet = Sets.newHashSet();
					treeSet.add(simset);
					recordSimsetMap.put(recordId, treeSet);
				}
			}
		}

	}

	/**
	 * Given recordA and recordB calculate the Sørensen similarity index
	 * between the two records.
	 * 
	 * @param recordA
	 *            the id of recordA
	 * @param recordB
	 *            the id of recordB
	 * @return the Sørensen similarity index for recordA and recordB
	 */
	private double calculateSimlarity(String recordA, String recordB) {
		Integer recordAFieldCount = recordFieldCountMap.get(recordA);
		Integer recordBFieldCount = recordFieldCountMap.get(recordB);
		int commonFieldCount = countCommonFields(recordA, recordB);
		double qs = (double) (2 * commonFieldCount)
				/ (double) (recordAFieldCount + recordBFieldCount);
		return qs;
	}

	private Set<Match> computeMatches() {
		int recordCount = 1;
		int recordSetCount = 1;
		int matchCount = 1;

		Set<Match> matches = Sets.newHashSet();
		for (Entry<String, Set<Simset>> entry : recordSimsetMap.entrySet()) {
			recordCount++;
			String recordA = entry.getKey();
			for (Simset simset :  entry.getValue()) {
				SortedSet<String> potentialSimalarRecords = simset.getRecordSet();
				recordSetCount++;
				for (String recordB : potentialSimalarRecords) {
					Match match = new Match(recordA, recordB);
					if (!recordB.equals(recordA) && !matches.contains(match)) {
						matchCount++;
						double qs = calculateSimlarity(recordA, recordB);
						match.setSimilarityIndex(qs);
						matches.add(match);
					}
				}
			}
		}
		log.debug("{} records processed", String.valueOf(recordCount));
		log.debug("{} record sets processed", String.valueOf(recordSetCount));
		log.debug("{} matches tested", String.valueOf(matchCount));
		return matches;
	}

	/**
	 * Calculate the number of common fields between recordA and recordB by
	 * summing the fieldCount for each Simset belonging to recordA that also
	 * contains recordB
	 * 
	 * @param recordA
	 * @param recordB
	 * @return number of fields in common
	 */
	private int countCommonFields(String recordAId, String recordBId) {
		int count = 0;
		Set<Simset> recordASimsets = recordSimsetMap.get(recordAId);
		for (Simset simset : recordASimsets) {
			if (simset.getRecordSet().contains(recordBId)) {
				count += simset.getFieldCount();
			}
		}
		return count;
	}

	/**
	 * 
	 * @param thresholdSq
	 * @return
	 * @throws IOException
	 */
	public List<Match> getMatches(final double thresholdSq) throws IOException {
		
		if (recordSets == null) {
			Preconditions.checkNotNull(recordFile);
			FieldRecordProcessor processor = new FieldRecordProcessor();
			processor.setIgnoreFirstLine(ignoreFirstLine);
			recordSets = Files.readLines(recordFile, Charset.defaultCharset(),
					processor);
		}

		@SuppressWarnings("unchecked")
		Map<SortedSet<String>, Integer> recordSetCardinalityMap = CollectionUtils.getCardinalityMap(recordSets);

		buildRecordSimsetMap(recordSetCardinalityMap);
		buildRecordFieldCountMap();

		Set<Match> matches = computeMatches();
		matches = Sets.filter(matches, new Predicate<Match>() {
			@Override
			public boolean apply(Match m) {
				return m.getSimilarityIndex() > thresholdSq;
			}
		});
		ArrayList<Match> matchList = Lists.newArrayList();
		matchList.addAll(matches);
		Collections.sort(matchList, new Comparator<Match>() {
			@Override
			public int compare(Match o1, Match o2) {
				return o2.getSimilarityIndex().compareTo(o1.getSimilarityIndex());
			}
		});
		return matchList;
	}


	/**
	 * Calculates the number of fields for a given record based on the set of
	 * Simsets it belongs to.
	 * 
	 * @param recordId
	 * @return fieldCount
	 */
	private int getRecordFieldCount(String recordId) {
		int fieldCount = 0;
		for (Simset simset : recordSimsetMap.get(recordId)) {
			fieldCount += simset.getFieldCount();
		}
		return fieldCount;
	}

	public void loadFromFile(String recordFileName) throws IOException {
		loadFromFile(new File(recordFileName));
	}
	
	public void loadFromFile(File recordFile) throws IOException {
		try {
			log.info("Reading from file '{}'", recordFile.getAbsolutePath());
			String firstLine = Files.readFirstLine(recordFile, Charset.defaultCharset());
			log.debug("File first line: {}", firstLine);
			this.recordFile = recordFile;
		} catch (IOException e) {
			log.error("Error reading from file", e);
			throw e;
		}
	}
	
	public void loadFromRecordMap(Map<String, String> fieldRecordMap){
		FieldRecordProcessor processor = new FieldRecordProcessor();
		processor.setIgnoreFirstLine(ignoreFirstLine);
		for (Entry<String, String> entry : fieldRecordMap.entrySet()) {
			processor.addFieldRecord(entry.getKey(), entry.getValue());
		}
		recordSets = processor.getResult();
	}
	
	public void setIgnoreFirstLine(boolean ignoreFirstLine){
		this.ignoreFirstLine = ignoreFirstLine;
	}
	
}
