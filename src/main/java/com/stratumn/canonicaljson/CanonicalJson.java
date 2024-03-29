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

import java.io.IOException;
/***
 * Helper class for canonicalization  JSON
 */
public class CanonicalJson {
 
    
    /**
     * Converts an object to a string representation of type json.
     * Object is Map, Vector, null , String, Boolean,Double.
     * @param value
     * @return
     * @throws IOException
     */
    public static String stringify(Object value) throws IOException
    {
    	return new Transformer().transform( value  );
    }
    
    /***
     * Parse string to an Object of type map<String,Object> , Object[] or Boolean, BigDecimal, Boolean, null
     * @param source
     * @return
     * @throws IOException
     */
    public static Object parse(String source ) throws IOException 
    {
    	return new Parser(source).parse();
    }
     
    
    /***
     * Parses a string to object and converts it back to string in canonical Json form 
     * @param source
     * @return
     * @throws IOException
     */
    public static String canonizalize(String source) throws IOException
    {
       return new Transformer().transform(new Parser(source).parse());
    }
}
