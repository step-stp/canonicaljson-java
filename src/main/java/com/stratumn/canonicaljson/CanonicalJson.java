 
package com.stratumn.canonicaljson;

import java.io.IOException;
/***
 * 
 * @author STP
 * Helper class for canonicaliziong JSON
 * TODO support beans and datatypes by integrating GSON library and using generics to identify class type. 
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
    	return new Transformer(/*gap, indent, replacer*/).transform( value  );
    }
    
    /***
     * Parse string to an Object of type map or vector
     * @param source
     * @return
     * @throws IOException
     */
    public static Object parse(String source ) throws IOException 
    {
    	return new Parser(source).parse();
    }
}
