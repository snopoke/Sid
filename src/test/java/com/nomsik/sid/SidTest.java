package com.nomsik.sid;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.nomsic.sid.Match;
import com.nomsic.sid.Sid;

public class SidTest {

	@Test
	public void test_load() throws IOException{
		Sid sid = new Sid();
		URL resource = getClass().getResource("/loadtest.txt");
		sid.loadFromFile(resource.getFile());
		List<Match> matches = sid.getMatches(0d);
		Assert.assertTrue(matches.size() == 1);
		Assert.assertTrue(matches.get(0).getSimilarityIndex() == 1d);
	}
	
	@Test
	public void test_exactMatch() throws IOException{
		Sid sid = new Sid();
		URL resource = getClass().getResource("/exactmatch.txt");
		sid.loadFromFile(resource.getFile());
		List<Match> matches = sid.getMatches(0d);
		Assert.assertTrue(matches.size() == 1);
		Assert.assertTrue(matches.get(0).getSimilarityIndex() == 1d);
	}
	
	@Test
	public void test_nonExactMatch() throws IOException{
		Sid sid = new Sid();
		URL resource = getClass().getResource("/nonexactmatch.txt");
		sid.loadFromFile(resource.getFile());
		List<Match> matches = sid.getMatches(0d);
		Assert.assertTrue(matches.size() == 1);
		Assert.assertEquals(1d,matches.get(0).getSimilarityIndex());
	}
}
