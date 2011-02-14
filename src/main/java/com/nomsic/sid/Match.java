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
 *  Match.java
 */
package com.nomsic.sid;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

/**
 * A match represents the similarity index between two records.
 * 
 * @author Simon Kelly <simongdkelly@gmail.com>
 */
public class Match {

	private Double similarityIndex = 0d;
	private Set<String> records;

	public Match(String recordId1, String recordId2, double similarityIndex) {
		this.similarityIndex = similarityIndex;
		this.records = ImmutableSet.of(recordId1, recordId2);
	}

	public Match(String recordId1, String recordId2) {
		this.records = ImmutableSet.of(recordId1, recordId2);
	}

	public Double getSimilarityIndex() {
		return similarityIndex;
	}

	public void setSimilarityIndex(Double similarityIndex) {
		this.similarityIndex = similarityIndex;
	}
	
	/**
	 * @return an immutable Set containing exactly two records
	 */
	public Set<String> getRecords() {
		return records;
	}

	@Override
	public String toString() {
		return records + " (" + similarityIndex + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(records);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Match) {
			Match that = (Match) obj;
			return Objects.equal(this.records, that.records);
		}
		return false;
	}
}
