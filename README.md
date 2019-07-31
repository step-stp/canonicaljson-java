# canonicaljson-java

1 - Make sure the Java home paths are added for the installed JDK
2 - Using the command line run:

RunTest.cmd <<put path for folder>>

All subfolders with an input.json (and expected.json) will be picked up and processed as follows:
1- The file input.json will be parsed 
2- The resulting object will be stringify-ed 
3- If expected.json is available it will be compared to the result string.
4- An output.json will be created with the result of the process.

or  

RunTest.cmd filename.json

The input file will be parsed and the result object stringify-ed and an output.json will be created in the same folder. 

