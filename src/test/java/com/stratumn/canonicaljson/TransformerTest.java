package com.stratumn.canonicaljson;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.Vector;

import org.junit.Test;

public class TransformerTest
{
   
   
   
   @Test
   public void objectTransform() throws IOException { 
      TreeMap<String,Object> obj = new TreeMap<String, Object>();
      obj.put("a", 12 );
      obj.put("b", "123"); 
      assertEquals(CanonicalJson.stringify(obj),"{\"a\":12,\"b\":\"123\"}");
   }
   
   @Test
   public void arrayTransform() throws IOException { 
      TreeMap<String,Object> obj = new TreeMap<String, Object>();
      obj.put("a", new Vector<Object>(Arrays.asList( 1,2,"a" ))); 
      assertEquals(CanonicalJson.stringify(obj),"{\"a\":[1,2,\"a\"]}");
   }    
    
   
}
