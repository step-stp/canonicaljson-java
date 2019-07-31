/*
  Copyright 2017 Stratumn SAS. All rights reserved.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.stratumn.canonicaljson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class CanonicalJsonTest {
	 
 
	@Test
	public void testSanity()
	{
	   try
      {
	      String expected = "{\"ð�€€\":\"U+10000 LINEAR B SYLLABLE B008 A\"}";
	      String actual =  CanonicalJson.stringify(CanonicalJson.parse("{\"ð�€€\": \"U+10000 LINEAR B SYLLABLE B008 A\"}")) ; 
	      Assert.assertEquals(expected , actual);
      }
      catch(IOException e)
      {       
         e.printStackTrace();
      }
	}
	
	/***
	 * Reads both input file and expected file
	 * Parse input file , Stringify the output 
	 * compares the out of the process to the expected file .
	 * @param inputFile
	 * @param expectedFile
	 * @return
	 * @throws IOException
	 */
	private static String[] applyParseStringify(String inputFile,String expectedFile) throws IOException { 
	    File inputFileObj = new File(inputFile);
		String rawInput = FileUtils.readFileToString(inputFileObj, "UTF-8");
		
		String expected = null;
		if (expectedFile!=null)
			expected = FileUtils.readFileToString(new File(expectedFile), "UTF-8").trim(); 
		String actual =  CanonicalJson.stringify(CanonicalJson.parse(rawInput)) ;	
		return new String[] {rawInput, expected, actual};
	}
	 
 
	@Test
	public void canonicalJsonSpecTests() throws IOException {
		// get folders that contain input / expected json files.
		File rootFolder = new File("src/test/resources/test/");
		List<File> testFolders = getTestFolders(rootFolder);

		for (File testfolder : testFolders) {
			File input = new File(testfolder, "input.json");
			File expected = new File(testfolder, "expected.json");
			if (!input.exists()) {
				System.err.println("Input  files missing in " + testfolder.getPath());
				continue;
			}
			if (!expected.exists())
				expected = null;
			try {
				String[] expact = applyParseStringify(input.getAbsolutePath(), expected == null ? null : expected.getAbsolutePath());
				if (!expact[2].equals(expact[1]))
					System.err.println(testfolder.getPath() + "\rValues not equal Expected/Actual:\r" + expact[0].replaceAll("\\r|\\n|\\s", "")  + "\r" + expact[1] + "\r" + expact[2]);
			
			} catch (IOException e) {
				System.err.println("Malformed JSON: " + e.getLocalizedMessage());
				
			}

		}

	}
	
	/***
	 * recursively finds the folders containing input.json
	 * @param parentFolder
	 * @return
	 */
	private List<File> getTestFolders(File parentFolder)
	{   
		List<File> testFoldersList = new ArrayList<File>();
		File[] subFolders = parentFolder.listFiles( );
		for (File folder : subFolders) 
		{	
			if (new File(folder, "input.json").exists())
				testFoldersList.add(folder);
			else
				testFoldersList.addAll(getTestFolders(folder));
		}
		return testFoldersList;
	}
	
	 public static void main(String[] args) throws Exception {
	    new CanonicalJsonTest().canonicalJsonSpecTests();
	 }
	
}
