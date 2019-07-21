package com.stratumn.canonicaljson;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class CanonicalJsonTest {
	 
	private static String INPUT = "src/test/resources/input/";
	private static String OUTPUT = "src/test/resources/output/";
	
	private static void test(String inputFile,String expectedFile) throws IOException {
		System.out.println(new File(inputFile).getAbsolutePath());
		String rawInput = FileUtils.readFileToString(new File(inputFile), "utf-8");
		String expected = FileUtils.readFileToString(new File(expectedFile), "utf-8");
		 
		String actual =  CanonicalJson.stringify(CanonicalJson.parse(rawInput)) ;
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void arrays() throws IOException {
		test(INPUT + "arrays.json",OUTPUT + "arrays.json" );
	}

	@Test
	public void french() throws IOException {
		test(INPUT + "french.json",OUTPUT + "french.json");
	}

	@Test
	public void structures() throws IOException {
		test(INPUT + "structures.json",OUTPUT +"structures.json");
	}

	@Test
	public void values() throws IOException {
		test(INPUT + "values.json",OUTPUT +"values.json");
	}

	@Test
	public void weird() throws IOException {
		test(INPUT + "weird.json",OUTPUT +"weird.json");
	}
}
