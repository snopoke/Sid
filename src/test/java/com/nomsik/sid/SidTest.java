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
		List<Match> matches = getMatches("/loadtest.txt");
		Assert.assertTrue(matches.size() == 1);
		Assert.assertTrue(matches.get(0).getSimilarityIndex() == 1d);
	}

	@Test
	public void test_exactMatch() throws IOException{
		List<Match> matches = getMatches("/exactmatch.txt");
		Assert.assertTrue(matches.size() == 1);
		Assert.assertTrue(matches.get(0).getSimilarityIndex() == 1d);
	}
	
	@Test
	public void test_nonExactMatch() throws IOException{
		List<Match> matches = getMatches("/nonexactmatch.txt");
		Assert.assertTrue(matches.size() == 1);
		Assert.assertEquals(0.75d,matches.get(0).getSimilarityIndex());
	}
	
	@Test
	public void test_diffNumFields() throws IOException{
		List<Match> matches = getMatches("/differentnumfields.txt");
		Assert.assertTrue(matches.size() == 1);
		Assert.assertEquals((8d/12d),matches.get(0).getSimilarityIndex());
	}
	
	@Test
	public void test_multi() throws IOException{
		List<Match> matches = getMatches("/multimatch.txt");
		Assert.assertTrue(matches.size() == 3);
		Assert.assertEquals((8d/9d),matches.get(0).getSimilarityIndex());
		Assert.assertEquals((6d/8d),matches.get(1).getSimilarityIndex());
		Assert.assertEquals((4d/7d),matches.get(2).getSimilarityIndex());
	}
	
	private List<Match> getMatches(String testFile) throws IOException {
		Sid sid = new Sid();
		sid.setIgnoreFirstLine(true);
		URL resource = getClass().getResource(testFile);
		sid.loadFromFile(resource.getFile());
		List<Match> matches = sid.getMatches(0d);
		return matches;
	}
}
