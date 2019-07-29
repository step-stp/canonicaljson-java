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
 		new Parser("{\"a\":\"b\"}").parse();
//		new Parser("{\"string\": \"\\u20ac$\\u000F\\u000aA'\\u0042\\u0022\\u005c\\\\\\\"\\/\"}").parse();
		
	}

	
    List<Datum> valid = Arrays.asList(
    		new Datum[]
    				{
    				new    Datum("false",false,"false")
    					  ,new Datum("null",null,"null")
    					  ,new Datum("true",true,"true")
    					  ,new Datum("100E+100",new BigDecimal("100e100"),"big integers")
    					  ,new Datum("-1",new BigDecimal(-1),"negative integers")
    					  ,new Datum("1.21e1",new BigDecimal("12.1"),"decimal numbers")
    					  ,new Datum("\"\\ufb01\"","ﬁ","unicode encoded characters")
    					  ,new Datum("\"\\b\"","\b","short escaped characters")
    					  ,new Datum("[]",new Vector<Object>(),"empty arrays") 
    					  ,new Datum("[\"a\", 1, true]",new Vector<Object>(Arrays.asList(new Object[] {"a", new BigDecimal(1), new Boolean(true)})),"arrays")
    				} 
    		);
    		
     @Test
	public void testValid() throws IOException {
    	for (Datum dt : valid)
    	{	 
    		Object result ;
    		try {
    			result =   new Parser(dt.input ).parse() ;
    			if (result!=dt.output && !result.equals(dt.output))
    				System.out.println("Testing failed on " + dt.description);
    		}catch (Exception e) {
    			System.out.println("Testing failed on " + dt.description);
    			//e.printStackTrace(); 
			}
    	}
	}	 
 
	
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
