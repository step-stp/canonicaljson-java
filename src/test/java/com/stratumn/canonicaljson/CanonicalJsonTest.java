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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CanonicalJsonTest
{
  
   /***
    * Reads both input file and expected file
    * Parse input file , Stringify the output 
    * compares the out of the process to the expected file .
    * @param inputFile
    * @param expectedFile
    * @return
    * @throws IOException
    */
   private String[] applyParseStringify(String inputFile, String expectedFile) throws IOException
   {
      File inputFileObj = new File(inputFile);
      String rawInput = String.join("", Files.readAllLines(inputFileObj.toPath(),Charset.forName("UTF-8")));// FileUtils.readFileToString(inputFileObj, "UTF-8");

      String expected = null;
      if(expectedFile != null) expected =String.join("", Files.readAllLines(new File(expectedFile).toPath(),Charset.forName("UTF-8"))); // FileUtils.readFileToString(new File(expectedFile), "UTF-8").trim();
      String actual = CanonicalJson.stringify(CanonicalJson.parse(rawInput));
      Files.write(new File(inputFileObj.getParent(), "output.json").toPath(), actual.getBytes()/*.replaceAll(",",",\r\n")*/);
//      FileUtils.writeStringToFile(, actual/*.replaceAll(",",",\r\n")*/, "UTF-8");
      return new String[]{rawInput, expected, actual };
   }

   /***
    * 
    * @throws IOException
    */
   @Test
   public void canonicalJsonSpecTests() throws IOException
   {
      // get folders that contain input / expected json files.
      File rootFolder = new File("src/test/resources/test/");
      processTestFiles(rootFolder,true);
   }

   /***
    * Processes all input files, creates output files in same folder, and compares the output to the expected. 
    * An error is displayed if there is a comparison failure. 
    * An error is displayed if there is a malformed JSON.
    * @param folder
    */
   private void processTestFiles(File folder,boolean addLineSeparators)
   {
      List<File> testFolders = null;
      if (folder.isFile())
      {
         testFolders = new ArrayList<File>();
         testFolders.add(folder.getParentFile());
      }
      else if (folder.isDirectory())
         testFolders = getTestFolders(folder);
      for(File testfolder : testFolders)
      { 
         File expected = new File(testfolder, "expected.json");
         File input = new File(testfolder, "input.json");
         if(!input.exists())
         {
            System.err.println("Input  files missing in " + testfolder.getPath());
            continue;
         }
         if(!expected.exists()) expected = null;
         try
         {
            String[] expact = applyParseStringify(input.getAbsolutePath(), expected == null ? null : expected.getAbsolutePath());
            if(expact[1] != null && !expact[2].equals(expact[1])) 
               System.err.println("Values not equal Expected/Actual @" + testfolder.getPath()  + "\r\n" 
               + (expact[1] != null ?"Expected: \r\n" + (addLineSeparators ? expact[1].replaceAll(",",",\r\n" ):expact[1]) + "\r\n"  : "") //expected
               + "Actual: \r\n"
               + (addLineSeparators ? expact[2].replaceAll(",",",\r" ):expact[2]) ) ;  //actual
         }
         catch(IOException e)
         {
            System.err.println( "Malformed JSON: " + e.getLocalizedMessage() + " @ " + testfolder.getPath() );
         }

      }
   }

   /***
    * recursively finds the folders containing input.json (with or without expected.json)
    * @param parentFolder
    * @return
    */
   private List<File> getTestFolders(File parentFolder)
   {
      List<File> testFoldersList = new ArrayList<File>();
      File[] subFolders = parentFolder.listFiles();
      for(File folder : subFolders)
      {
         if(new File(folder, "input.json").exists())
            testFoldersList.add(folder);
         else
            testFoldersList.addAll(getTestFolders(folder));
      }
      return testFoldersList;
   }

  
   public static void main(String[] args) throws Exception
   {
      if (args.length != 1)
      { 
         System.out.println("Usage: CanonicalJsonTest  <DataFolder | JSON InputFile>");
         System.out.println("       DataFolder/input.json  DataFolder/expected.json");
         System.out.println("If Parameter is a folder, searches for all subfolders with input.json (and expected.json)."
            + "\rReads/parses DataFolder/input.json then serializes it to canonical json and write output.json in same folder. "
            + "\rCompares output to expected.json if found under same folder. ");
         System.exit(-1);
      }
      File file = new File(args[0]); 
      if (file.exists() )
         new CanonicalJsonTest().processTestFiles(file, false);
   }

}
