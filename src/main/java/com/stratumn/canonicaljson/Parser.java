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
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;
/***
 * 
 * @author Ahmad Hamid
 *
 */
public class Parser {

	private static final char NULL_CHAR = '\u0000';
	private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false");
	private static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?([eE][-+]?[0-9]+)?");
	// Regular expressions that matches characters otherwise inexpressible in 
	// JSON (U+0022 QUOTATION MARK, U+005C REVERSE SOLIDUS, 
	// and ASCII control characters U+0000 through U+001F) or UTF-8 (U+D800 through U+DFFF)
	private static final  Pattern FORBIDDEN = Pattern.compile("[\\u0022\\u005c\\u0000-\\u001F\\ud800-\\udfff]");

	
	
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

	public Parser(String jsonString) throws IOException {
		this.jsonData = jsonString;
	}

	/***
     * starts parsing returning the object
     * @return
     * @throws IOException
     */
    public Object parse() throws IOException {
		  
    	 
		root = parseElement();  
		scan();
        if (chr!=NULL_CHAR && !isWhiteSpace(chr)) {
            throw new IOException("Improperly terminated JSON object:" + chr);
        } 
		return root;
	}

	/***
	 * Initiates parsing next element based on type 
	 * @return
	 * @throws IOException
	 */
	private Object parseElement() throws IOException {
		
		switch (scan()) {//skipwhite space and find first chr
			case C_LEFT_BRACKET:
				return parseArray();
			case C_LEFT_CURLY_BRACKET:
				return parseObject();
			case C_DOUBLE_QUOTE:
				return parseQuotedString();
			default:
				return parseSimpleType();
		}
	}

	/***
	 * parses an object of the form "{"key":value}" or empty object {}
	 * @return
	 * @throws IOException
	 */
	private Object parseObject() throws IOException {
		TreeMap<String, Object> dict = new TreeMap<String, Object>();
		boolean next = false;
	 	//chr = { 
		while (peek()!= C_RIGHT_CURLY_BRACKET  ) { 
			if (next) 
				scanFor(C_COMMA);
            next = true;
		    scanFor(C_DOUBLE_QUOTE);
		    //chr = "
			String name = parseQuotedString();
			scanFor(C_COLON);
			//chr = : 
			if (dict.put(name, parseElement()) != null) {
				throw new IOException("Duplicate property: " + name);
			} 
		
		} 
		scan();
		return dict;
	}

	private Object parseArray() throws IOException {
		Vector<Object> array = new Vector<Object>();
		 boolean next = false; 
	    //current chr = [ 
		while (peek() != C_RIGHT_BRACKET  ) {
			if (next)
				scanFor(C_COMMA);
			else next = true; 
			array.add(parseElement());
		} 
		scan();  
		return array;
	}

	
	/***
	 * Parses Boolean nummeric and null values
	 * @return
	 * @throws IOException
	 */
	private Object parseSimpleType() throws IOException {
	 
		StringBuilder tempBuffer = new StringBuilder();  
		while (chr!=NULL_CHAR && chr != C_COMMA && chr != C_RIGHT_BRACKET && chr != C_RIGHT_CURLY_BRACKET )
		{   
			//construct the token
			tempBuffer.append(chr);
			next();
			if (isWhiteSpace(chr))
				break;
		}
		String token = tempBuffer.toString();
		if (token.length() == 0) {
			throw new IOException("Missing argument");
		}
		if (NUMBER_PATTERN.matcher(token).matches()) {
			return Double.valueOf(token); // Syntax check...
		} else if (BOOLEAN_PATTERN.matcher(token).matches()) {
			return new Boolean(token);
		} else if (token.equals("null")) {
			return null;
		} else {
			throw new IOException("Unrecognized or malformed JSON token: " + token);
		}
	}

	/***
	 * parse string tokens between two quotes.
	 * @return
	 * @throws IOException
	 */
	private String parseQuotedString() throws IOException {
		StringBuilder result = new StringBuilder();
		// When parsing for string values, we must look for " and \ characters.
		//current chr = "
		if (chr!= C_DOUBLE_QUOTE)
		   throw new IOException("Bad String");
		while (next() != C_DOUBLE_QUOTE) {
			if (chr < ' ') {
				throw new IOException(chr == '\n' ? "Unterminated string literal"
						: "Unescaped control character: 0x" + Integer.toString(chr, 16));
			}//TODO redundant ??
//			if (FORBIDDEN.matcher(Character.toString(chr)).matches())
//				 throw new IOException("Forbidden Char :" + chr); 
			// escaped character
			if (chr == C_BACK_SLASH) {
				switch (next()) {
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

				case 'u':// hex
					char c = 0;
					for (int i = 0; i < 4; i++) {
						c = (char) ((c << 4) + getHexChar());
					}
					break;

				default:
					throw new IOException("Unsupported escape:" + chr);
				}
			} 
			result.append(chr);
		}
		 
		return result.toString();
	}

	
	private char getHexChar() throws IOException {
		  
		switch (next()) {
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return (char) (chr - '0');

		case 'a':
		case 'b':
		case 'c':
		case 'd':
		case 'e':
		case 'f':
			return (char) (chr - 'a' + 10);

		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
			return (char) (chr - 'A' + 10);
		}
		throw new IOException("Bad hex in \\u escape: " + chr);
	}

	/***
	 * Retuirns the next non white character without moving the cursor to it.
	 * @return
	 * @throws IOException
	 */
	private char peek() throws IOException {
		int bookmark = index;
		char c = scan();
		index = bookmark;
		chr = jsonData.charAt(index-1);
		return c;
	}
//
//	
//
//	char nextChar() throws IOException {
//		int maxLength = jsonData.length();
//		if (index < maxLength) {
//			return jsonData.charAt(index++);
//		}
//		throw new IOException("Unexpected EOF reached");
//	}

	/***
	 * White space check
	 * @param c
	 * @return
	 */
	private boolean isWhiteSpace(char c) {
		return Character.isWhitespace(c);
		// return c == 0x20 || c == 0x0A || c == 0x0D || c == 0x09;
	}
	
	/***
	 * Moves to the next nonwhitepace character and tests if that char matches expected
	 * @param expected
	 * @throws IOException
	 */
	private void scanFor(char expected) throws IOException { 
		if (scan() != expected) {
			throw new IOException("Expected '" + expected + "' but got '" + chr + "'");
		}
	}
	
	
	/*** 
	 * Moves to the next non-whitespace character
	 * @return
	 * @throws IOException
	 */
	private char scan() throws IOException {
		 
		while (next()!= NULL_CHAR && isWhiteSpace(chr));  
		return chr;
	}

	/***
	 * Jumps to the next character
	 * 
	 * @return next char or null if end is reached
	 * @throws IOException
	 */
	private char next() throws IOException {
		int maxLength = jsonData.length();
		if (index < maxLength) {
			chr = jsonData.charAt(index++);
		} else
			chr = NULL_CHAR;
		return chr;
	}

}
