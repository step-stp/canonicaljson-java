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

import static com.stratumn.canonicaljson.Constants.C_BACKSPACE;
import static com.stratumn.canonicaljson.Constants.C_BACK_SLASH;
import static com.stratumn.canonicaljson.Constants.C_CARRIAGE_RETURN;
import static com.stratumn.canonicaljson.Constants.C_DOUBLE_QUOTE;
import static com.stratumn.canonicaljson.Constants.C_FORM_FEED;
import static com.stratumn.canonicaljson.Constants.C_LINE_FEED;
import static com.stratumn.canonicaljson.Constants.C_TAB;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/***
 * Transformer converts an object Vector / Map / Java Object to JSON stream 
 * 
 */
public class Transformer
{ 
   private StringBuilder buffer;
   /* Regular expressions that matches characters otherwise inexpressible in 
     JSON (U+0022 QUOTATION MARK, U+005C REVERSE SOLIDUS, 
    and ASCII control characters U+0000 through U+001F) or UTF-8 (U+D800 through U+DFFF) */
   private static final Pattern FORBIDDEN = Pattern.compile("[\\u0022\\u005c\\u0000-\\u001F\\ud800-\\udfff]");

   /***
    * Transforms an object to Canonicalized JSON
    * @param obj
    * @return
    * @throws IOException
    */
   public String transform(Object obj) throws IOException
   {
      buffer = new StringBuilder();
      serialize(obj);
      return buffer.toString();
   }

   /***
    * Escapes a character with a backslash
    * @param c
    */
   private void escape(char c)
   {
      buffer.append(C_BACK_SLASH).append(c);
   }

   /***
     * MUST represent all strings (including object member names) in their minimal-length UTF-8 encoding
      * avoiding escape sequences for characters except those otherwise inexpressible in JSON (U+0022 QUOTATION MARK, U+005C REVERSE SOLIDUS, and ASCII control characters U+0000 through U+001F) or UTF-8 (U+D800 through U+DFFF), and
      * avoiding escape sequences for combining characters, variation selectors, and other code points that affect preceding characters, and
      * using two-character escape sequences where possible for characters that require escaping:
      * \b U+0008 BACKSPACE
      * \t U+0009 CHARACTER TABULATION ("tab")
      * \n U+000A LINE FEED ("newline")
      * \f U+000C FORM FEED
      * \r U+000D CARRIAGE RETURN
      * \" U+0022 QUOTATION MARK
      * \\ U+005C REVERSE SOLIDUS ("backslash"), and
      * using six-character \\u00xx uppercase hexadecimal escape sequences for control characters that require escaping but lack a two-character sequence, and
      * using six-character \\uDxxx uppercase hexadecimal escape sequences for lone surrogates
     * @param value 
     */
   private void serializeString(String value) 
   {
      buffer.append(C_DOUBLE_QUOTE);
      if(!FORBIDDEN.matcher(value).find())
         buffer.append(value);
      else
      {   
         char[] chars = value.toCharArray();
         for (int i=0; i< chars.length; i++)
         { 
            if(!FORBIDDEN.matcher(Character.toString(chars[i])).find())
            {
               buffer.append(chars[i]);
               continue;
            }  
            if (Character.isSurrogate(chars[i]) && chars.length >i+1 && Character.isSurrogatePair(chars[i], chars[i+1]) )
            {
                  buffer.appendCodePoint(Character.toCodePoint(chars[i], chars[++i])); 
                  continue; 
            } 
            //escape special characters and unicode characters.
            switch(chars[i])
            {
               case C_LINE_FEED:
                  escape('n');
               break;
               case C_BACKSPACE:
                  escape('b');
               break;
               case C_FORM_FEED:
                  escape('f');
               break;

               case C_CARRIAGE_RETURN:
                  escape('r');
               break;

               case C_TAB:
                  escape('t');
               break;

               case C_DOUBLE_QUOTE:
               case C_BACK_SLASH:
                  escape(chars[i]);
               break;
               default: 
                     escape('u');
                     String hex = String.format("%04x", (int) chars[i]).toUpperCase();
                     buffer.append(hex);        
               break;
            }
         }        
      }
      buffer.append(C_DOUBLE_QUOTE);
   }

   /***
    *  MUST represent all integer numbers (those with a zero-valued fractional part)
   * without a leading minus sign when the value is zero, and
   * without a decimal point, and
   * without an exponent
   *
   * MUST represent all non-integer numbers in exponential notation
   * including a nonzero single-digit significant integer part, and
   * including a nonempty significant fractional part, and
   * including no trailing zeroes in the significant fractional part (other than as part of a ".0" required to satisfy the preceding point), and
   * including a capital "E", and
   * including no plus sign in the exponent, and
   * including no insignificant leading zeroes in the exponent
    * @param bd
    * @throws IOException
    */
   private void serializeNumber(String value) throws IOException
   {
      BigDecimal bd = new BigDecimal((String) value);
      try
      { //attempt converting to fixed number
         buffer.append(bd.toBigIntegerExact().toString());
      }
      catch(ArithmeticException e)
      {
         //convert to exponential format with minimum fractional digits of 1 and all significant fractional digits
         NumberFormat formatter = new DecimalFormat("0.0E0");
         formatter.setMinimumFractionDigits(1);
         formatter.setMaximumFractionDigits(bd.precision());
         String val = formatter.format(bd).replace("+", "");
         buffer.append(val);
      }
   }

   /***
    * Attempts to serialize an object of type treemap, collection, null , string , boolean , bigdecimal
    * @param o
    * @throws IOException
    */
   @SuppressWarnings("unchecked")
   private void serialize(Object o) throws IOException
   {
      if(o instanceof Map)
      {
         
         TreeMap<String, Object> sortedTree = new TreeMap<String,Object>(new LexComparator()); 
         sortedTree.putAll((Map<String, Object>) o);
         buffer.append('{');
         boolean next = false;
         for(Map.Entry<String, Object> keyValue : sortedTree.entrySet())
         {
            if(next)
            {
               buffer.append(',');
            }
            next = true;
            serializeString(keyValue.getKey());
            buffer.append(':');
            serialize(keyValue.getValue());
         }
         buffer.append('}');
      }
      else
         if(o instanceof Collection<?>)
         {  
            buffer.append('[');
            boolean next = false;
            for(Object value : ((Collection<?>) o))
            {
               if(next)
               {
                  buffer.append(',');
               }
               next = true;
               serialize(value);
            }
            buffer.append(']');
         }
         else
            if(o == null)
            {
               buffer.append("null");
            }
            else
               if(o instanceof String)
               {
                  serializeString((String) o);
               }
               else
                  if(o instanceof Boolean)
                  {
                     buffer.append((Boolean) o);
                  }
                  else
                     if(o instanceof Double || o instanceof BigDecimal || o instanceof Integer)
                        serializeNumber(o.toString());
                     else
                     {
                        throw new InternalError("Unknown object: " + o);
                     }
   }

   public String getEncodedString()
   {
      return buffer.toString();
   }

   public byte[] getEncodedUTF8() throws IOException
   {
      return getEncodedString().getBytes("utf-8");
   }
   
   /***
    * Compares strings lexicographically
    *  MUST order the members of all objects lexicographically by the UCS (Unicode Character Set) code points of their names
    *  preserving and utilizing the code points in U+D800 through U+DFFF (inclusive) for all lone surrogates
    */
   class LexComparator implements Comparator<String>{
       
         @Override
         public int compare(String keyA, String keyB)
         {   
           int result = 0;
           if(!keyA.equals(keyB))
           for(int i=0 ; i<keyA.length() ; i++)
           { 
               if(i > keyB.length()-1)
               {
                   result = 1;
                   break;
               } 
               int aCodePoint =  keyA.codePointAt(i);
               int bCodePoint =  keyB.codePointAt(i);
               if(aCodePoint != bCodePoint)
               {
                   result =Integer.valueOf(aCodePoint).compareTo(bCodePoint);  
                   break;
               }
           }
           
           return result;
         } 
   }
}
