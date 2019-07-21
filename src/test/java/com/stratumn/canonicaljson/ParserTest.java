package com.stratumn.canonicaljson;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.junit.Test;

public class ParserTest {

	@Test
	public void testSanity() throws IOException {
//		Object obj = new Parser("{\"a\":\"b\"}").parse();
		new Parser("[1, 2]").parse();
	}

	
    List<Datum> valid = Arrays.asList(
    		new Datum[]
    				{
    				new    Datum("false",false,"false")
    					  ,new Datum("null",null,"null")
    					  ,new Datum("true",true,"true")
    					  ,new Datum("100E+100",new BigDecimal("100e100"),"big integers")
    					  ,new Datum("-1",-1,"negative integers")
    					  ,new Datum("1.21e1",new BigDecimal("12.1"),"decimal numbers")
    					  ,new Datum("\"\\ufb01\"",'ﬁ',"unicode encoded characters")
    					  ,new Datum("\"\\b\"",'\b',"short escaped characters")
    					  ,new Datum("[]",new Vector<Object>(),"empty arrays") 
    					  ,new Datum("[\"a\", 1, true]",new Vector<Object>(Arrays.asList(new Object[] {"a", 1, true})),"arrays")
    				} 
    		);
    		
//    @Test
//	public void testValid() throws IOException {
//    	for (Datum dt : valid)
//    	{	
//    		
//    		//new JsonCanonicalizer(dt.input).getEncodedString();
//    		try {
//    		Object obj = new Parser(dt.input ).parse();
//    		}catch (Exception e) {
//    			System.out.println("Testing failed on " + dt.description);
//    			e.printStackTrace(); 
//			}
//    	}
//	}	 
 
	
	class Datum
	{
		
		String input;
		Object output;
		String description;
		public Datum(String input, Object output, String description) {
			super();
			this.input = input;
			this.output = output;
			this.description = description;
		}
		@Override
		public String toString() {
			return "Datum [input=" + input + ", output=" + output + ", description=" + description + "]";
		}
		
		
		
	}
}
