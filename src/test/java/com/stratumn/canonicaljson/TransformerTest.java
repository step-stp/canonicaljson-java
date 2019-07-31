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
