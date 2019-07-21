package com.stratumn.canonicaljson;

import static com.stratumn.canonicaljson.Constants.*;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/***
 * Transformer converts and JSON stream to an object Vector / Map / Java Object
 * @author Ahmad Hamid
 *
 */
public class Transformer {
 
	 private StringBuilder buffer; 
	 public String transform(Object obj) throws IOException
	 {
		 buffer = new StringBuilder();
		 serialize(obj);
		 return buffer.toString();
	 }
	 
	 
	 private void escape(char c) {
	        buffer.append(C_BACK_SLASH).append(c);
	    }

	    private void serializeString(String value) {
	        buffer.append(C_DOUBLE_QUOTE);
	        for (char c : value.toCharArray()) {
	            switch (c) {
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
	              //  case C_FORWARD_SLASH:	
	                    escape(c);
	                    break;

	                default:
	                    if (c < 0x20) {
	                        escape('u');
	                        for (int i = 0; i < 4; i++) {
	                            int hex = c >>> 12;
	                            buffer.append((char) (hex > 9 ? hex + 'a' - 10 : hex + '0'));
	                            c <<= 4;
	                        }
	                        break;
	                    }
	                    buffer.append(c);
	            }
	        }
	        buffer.append(C_DOUBLE_QUOTE);
	    }

	    
	    @SuppressWarnings("unchecked")
		private void serialize(Object o) throws IOException
	    {
	        if (o instanceof TreeMap) {
	            buffer.append('{');
	            boolean next = false;
	            for (Map.Entry<String,Object> keyValue : ((TreeMap<String,Object>)o).entrySet()) {
	                if (next) {
	                    buffer.append(',');
	                }
	                next = true;
	                serializeString(keyValue.getKey());
	                buffer.append(':');
	                serialize(keyValue.getValue());
	            }
	            buffer.append('}');
	        } else if (o instanceof Vector) {
	            buffer.append('[');
	            boolean next = false;
	            for (Object value : ((Vector<Object>)o).toArray()) {
	                if (next) {
	                    buffer.append(',');
	                }
	                next = true;
	                serialize(value);
	            }
	            buffer.append(']');
	        } else if (o == null) {
	            buffer.append("null");
	        } else if (o instanceof String) {
	            serializeString((String)o);
	        } else if (o instanceof Boolean) {
	            buffer.append((Boolean)o);
	        } else if (o instanceof Double) {
	            buffer.append(NumberToJSON.serializeNumber((Double)o));
	        } else {
	            throw new InternalError("Unknown object: " + o);
	        }
	    }

	    public String getEncodedString() {
	        return buffer.toString();
	    }

	    public byte[] getEncodedUTF8() throws IOException {
	        return getEncodedString().getBytes("utf-8");
	    }
}
