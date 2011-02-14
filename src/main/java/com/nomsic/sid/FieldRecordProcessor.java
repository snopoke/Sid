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
 *  FieldRecordProcessor.java
 */
package com.nomsic.sid;

import java.io.IOException;
import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.io.LineProcessor;

/**
 * Processes the lines of the record file and builds a map containing one entry
 * for each unique field. The value of each entry is a Simset containing all the
 * records that have that field in common.
 * 
 * The record file must be of the format: <field>,<recordId>
 * 
 * The field may be surrounded by double quotes and may contain any characters
 * 
 * @author Simon Kelly <simongdkelly@gmail.com>
 * 
 */
final class FieldRecordProcessor implements
		LineProcessor<Collection<SortedSet<String>>> {

	private static final Logger log = LoggerFactory
			.getLogger(FieldRecordProcessor.class);
	private SortedMap<String, SortedSet<String>> map;
	private boolean ignoreFirstLine;
	private boolean firstCall = true;

	public FieldRecordProcessor() {
		map = Maps.newTreeMap();
	}

	@Override
	public Collection<SortedSet<String>> getResult() {
		return map.values();
	}

	@Override
	public boolean processLine(String line) throws IOException {
		if (ignoreFirstLine && firstCall) {
			firstCall = false;
			return true;
		}
		int i = line.lastIndexOf(',');
		if (i < 0) {
			log.warn("invalid line:" + line);
			return true;
		}
		boolean extraTrim = line.startsWith("\"");
		int begin = extraTrim ? 1 : 0;
		int end = extraTrim ? i - 1 : i;
		String field = line.substring(begin, end).trim();
		String recordId = line.substring(i + 1).trim();

		addFieldRecord(field, recordId);
		return true;
	}

	public void addFieldRecord(String field, String recordId) {
		if (map.containsKey(field)) {
			map.get(field).add(recordId);
		} else {
			TreeSet<String> recordSet = new TreeSet<String>();
			recordSet.add(recordId);
			map.put(field, recordSet);
		}
	}

	public void setIgnoreFirstLine(boolean ignoreFirstLine) {
		this.ignoreFirstLine = ignoreFirstLine;
	}
}