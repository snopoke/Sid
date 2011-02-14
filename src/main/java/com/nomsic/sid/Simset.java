package com.nomsic.sid;

import java.util.SortedSet;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * Represents a set of records that contain one or more identical fields. The
 * number of identical fields that each record has in common is denoted by
 * fieldCount.
 * 
 * @author Simon Kelly <simon@cell-life.org>
 * 
 */
public class Simset {

	private int fieldCount = 1;

	private SortedSet<String> recordSet;

	private String toString;

	public Simset(int fieldCount, SortedSet<String> set) {
		this.recordSet = set;
		this.fieldCount = fieldCount;
	}

	public Simset(String response_id) {
		this.recordSet = Sets.newTreeSet();
		this.recordSet.add(response_id);
	}

	public void setFieldCount(int count) {
		this.fieldCount = count;
	}

	public int getFieldCount() {
		return fieldCount;
	}

	public void setRecordSet(SortedSet<String> set) {
		this.recordSet = set;
	}

	public SortedSet<String> getRecordSet() {
		return recordSet;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(recordSet);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Simset) {
			Simset that = (Simset) object;
			return Objects.equal(this.recordSet, that.recordSet);
		}
		return false;
	}

	public void addRecord(String response_id) {
		this.toString = null;
		recordSet.add(response_id);
	}

	public String printRecordSet() {
		if (toString == null) {
			toString = recordSet.toString();
		}
		return toString;
	}

	@Override
	public String toString() {
		return printRecordSet() + "(" + fieldCount + ")";
	}

	public int size() {
		return recordSet.size();
	}

	public void incrementCount() {
		this.fieldCount++;
	}
}
