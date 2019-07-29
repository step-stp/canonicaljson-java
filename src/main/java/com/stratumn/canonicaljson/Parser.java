package com.stratumn.canonicaljson;

import static com.stratumn.canonicaljson.Constants.C_BACKSPACE;
import static com.stratumn.canonicaljson.Constants.C_BACK_SLASH;
import static com.stratumn.canonicaljson.Constants.C_CARRIAGE_RETURN;
import static com.stratumn.canonicaljson.Constants.C_COLON;
import static com.stratumn.canonicaljson.Constants.C_COMMA;
import static com.stratumn.canonicaljson.Constants.C_DOUBLE_QUOTE;
import static com.stratumn.canonicaljson.Constants.C_FORM_FEED;
import static com.stratumn.canonicaljson.Constants.C_FORWARD_SLASH;
import static com.stratumn.canonicaljson.Constants.C_LEFT_BRACKET;
import static com.stratumn.canonicaljson.Constants.C_LEFT_CURLY_BRACKET;
import static com.stratumn.canonicaljson.Constants.C_LINE_FEED;
import static com.stratumn.canonicaljson.Constants.C_RIGHT_BRACKET;
import static com.stratumn.canonicaljson.Constants.C_RIGHT_CURLY_BRACKET;
import static com.stratumn.canonicaljson.Constants.C_TAB;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

/***
 * 
 * Parser class is responsible for parsing JSON streams and returning an object 
 * 
 */
public class Parser
{

   private static final char NULL_CHAR = '\u0000';
   private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false");

   private static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?([eE][-+]?[0-9]+)?");
   /***
    * Matches characters valid for a unicode characater. 
    */
   private static final Pattern HEX_PATTERN = Pattern.compile("([0-9,a-f,A-F]){4}");
   /* Regular expressions that matches characters otherwise inexpressible in 
   JSON (U+0022 QUOTATION MARK, U+005C REVERSE SOLIDUS, 
   and ASCII control characters U+0000 through U+001F) or UTF-8 (U+D800 through U+DFFF) */
   private static final Pattern FORBIDDEN = Pattern.compile("[\\u0022\\u005c\\u0000-\\u001F\\ud800-\\udfff]");

   /***
    * index and value of the current character
    */
   private int index;
   private char chr;
   /***
    * input json data
    */
   private String jsonData;

   /**
    * object value
    */
   private Object root;

   public Parser(String jsonString) throws IOException
   {
      this.jsonData = jsonString;
   }

   /***
    * starts parsing returning the object
    * @return
    * @throws IOException
    */
   public Object parse() throws IOException
   {
      
      root = parseElement();
      scan();
      if(chr != NULL_CHAR && !isWhiteSpace(chr))
      {
         throw new IOException("Improperly terminated JSON object:" + chr);
      }
      return root;
   }

   /***
    * Initiates parsing next element based on type 
    * @return
    * @throws IOException
    */
   private Object parseElement() throws IOException
   {

      switch(scan())
      {//skipwhite space and find first chr
         case C_LEFT_BRACKET:
            return parseArray();
         case C_LEFT_CURLY_BRACKET:
            return parseObject();
         case C_DOUBLE_QUOTE:
            return parseQuotedString();
         case NULL_CHAR:
            throw new IOException("Unexpected end of data reached");
         default:
            return parseSimpleType();
      }
   }

   /***
    * Parses an object of the form "{"key":value}" or empty object {}
    * @return
    * @throws IOException
    */
   private Object parseObject() throws IOException
   {
      TreeMap<String, Object> dict = new TreeMap<String, Object>();
      boolean next = false;
      //chr = { 
      while(peek() != C_RIGHT_CURLY_BRACKET)
      {
         if(next) scanFor(C_COMMA);
         next = true;
         scanFor(C_DOUBLE_QUOTE);
         //chr = "
         String name = parseQuotedString();
         scanFor(C_COLON);
         //chr = : 
         if(dict.put(name, parseElement()) != null)
         {
            throw new IOException("Duplicate property: " + name);
         }

      }
      scan();
      return dict;
   }

   /***
    * Parses Arrays of the form [X,Y,Z] or empty [] 
    * @return
    * @throws IOException
    */
   private Object parseArray() throws IOException
   {
      Vector<Object> array = new Vector<Object>();
      boolean next = false;
      //current chr = [ 
      while(peek() != C_RIGHT_BRACKET)
      {
         if(next)
            scanFor(C_COMMA);
         else
            next = true;
         array.add(parseElement());
      }
      scan();
      return array;
   }

   /***
    * Parses Boolean Numeric and null values
    * @return
    * @throws IOException
    */
   private Object parseSimpleType() throws IOException
   {

      StringBuilder tempBuffer = new StringBuilder();
      //construct the token
      tempBuffer.append(chr);
      char c;
      while((c = peek()) != NULL_CHAR && c != C_COMMA && c != C_RIGHT_BRACKET && c != C_RIGHT_CURLY_BRACKET)
      {
         //whitespace terminates simple types
         if(isWhiteSpace(next())) break;
         tempBuffer.append(chr);
      }

      String token = tempBuffer.toString();
      if(token.length() == 0)
      {
         throw new IOException("Missing argument");
      }
      if(NUMBER_PATTERN.matcher(token).matches())
      {
         return new BigDecimal(token);
      }
      else
         if(BOOLEAN_PATTERN.matcher(token).matches())
         {
            return new Boolean(token);
         }
         else
            if(token.equals("null"))
            {
               return null;
            }
            else
            {
               throw new IOException("Unrecognized or malformed JSON token: " + token);
            }
   }

   /***
    * parse string tokens between two quotes.
    * @return
    * @throws IOException
    */
   private String parseQuotedString() throws IOException
   {
      StringBuilder result = new StringBuilder();
      // When parsing for string values, we must look for " and \ characters.
      if(chr != C_DOUBLE_QUOTE) throw new IOException("Bad String");
      while(next() != C_DOUBLE_QUOTE)
      {
            if(chr == C_BACK_SLASH)
            {
               switch(next())
               {
                  case C_DOUBLE_QUOTE:
                  case C_BACK_SLASH:
                  case C_FORWARD_SLASH:
                  break; 
                  case 'b':
                     chr = C_BACKSPACE;
                  break; 
                  case 'f':
                     chr = C_FORM_FEED;
                  break; 
                  case 'n':
                     chr = C_LINE_FEED;
                  break; 
                  case 'r':
                     chr = C_CARRIAGE_RETURN;
                  break; 
                  case 't':
                     chr = C_TAB;
                  break; 
                  case 'u'://parse next for chars as a hex word.
                     chr = parseHex();
                  break; 
                  default:
                     throw new IOException("Unsupported escape:" + chr);
               }
            }else 
               if(FORBIDDEN.matcher(Character.toString(chr)).matches())
                  throw new IOException("Unescaped control character: " + Integer.toString(chr, 16));
         result.append(chr);
      }
      return result.toString();
   }

   /***
    * Reads 4 characters and attempts to convert hex to char
    * @return
    * @throws IOException 
    */
   private char parseHex() throws IOException
   {
      StringBuffer hex = new StringBuffer();
      for(int i = 0; i < 4; i++)
      {
         hex.append(next());
      }
      String hexStr = hex.toString();
      if(!HEX_PATTERN.matcher(hexStr).matches()) throw new IOException("Bad hex in escape: \\u" + hexStr);
      return (char) Integer.parseInt(hex.toString(), 16);
   }

   /***
    * Returns the next non white character without moving the cursor to it.
    * @return
    * @throws IOException
    */
   private char peek() throws IOException
   {
      int bookmark = index;
      char c = scan();
      index = bookmark;
      chr = jsonData.charAt(index - 1);
      return c;
   }

   /***
    * White space check
    * @param c
    * @return
    */
   private boolean isWhiteSpace(char c)
   {
      return Character.isWhitespace(c);
   }

   /***
    * Moves to the next nonwhitepace character and tests if that char matches expected
    * @param expected
    * @throws IOException
    */
   private void scanFor(char expected) throws IOException
   {
      if(scan() != expected)
      {
         throw new IOException("Expected '" + expected + "' but got '" + chr + "'");
      }
   }

   /*** 
    * Moves to the next non-whitespace character
    * @return
    * @throws IOException
    */
   private char scan() throws IOException
   {

      while(next() != NULL_CHAR && isWhiteSpace(chr))
         ;
      return chr;
   }

   /***
    * Jumps to the next character 
    * @return next char or null if end is reached
    * @throws IOException
    */
   private char next() throws IOException
   {
      int maxLength = jsonData.length();
      if(index < maxLength)
      {
         chr = jsonData.charAt(index++);
      }
      else
         chr = NULL_CHAR;
      return chr;
   }

}
