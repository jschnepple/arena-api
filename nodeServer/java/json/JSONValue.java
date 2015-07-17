package json;

import java.io.*;

public abstract class JSONValue {

    public abstract JSONValue makeCopy();

    public static String quoteEscapeString(String str) {
        StringBuilder sb = new StringBuilder(str.length() + 2);
        sb.append('"');
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            switch (c) {
                case '\b':
                    sb.append("\\b");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    if (c < ' ' || c > 127)
                        sb.append(String.format("\\u%04x", (int) c));
                    else
                        sb.append(c);
                    break;
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static String unEscapeString(String str) {
        if (str.indexOf('\\') == -1)
            return str;
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == '\\' && i + 1 < str.length()) {
                char c = str.charAt(++i);
                switch (c) {
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'u':
                        int utf32 = -1;
                        if (i + 4 < str.length()) {
                            try {
                                utf32 = Integer.parseInt(str.substring(i + 1, i + 5), 16);
                            } catch (NumberFormatException e) {
                            }
                        }
                        if (utf32 == -1) {
                            sb.append(c);    // NumberFormatException -> send 'u' and continue
                        } else {
                            i += 4;
                            sb.append((char) utf32);
                        }
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            } else
                sb.append(str.charAt(i));
        }
        return sb.toString();
    }

    public static JSONValue parseJSONValue(ParseState parseState) {
        if (parseState.skipWhite()) {
            char c = parseState.current();
            switch (c) {
                case '{':
                    return JSONObject.parseJSON(parseState);
                case '"':
                    return JSONString.parseJSON(parseState);
                case '[':
                    return JSONArray.parseJSON(parseState);
                case 'n':
                    return JSONNull.parseJSON(parseState);
                case 't':
                case 'f':
                    return JSONBoolean.parseJSON(parseState);
                default:
                    if ((c >= '0' && c <= '9') || c == '-' || c == '.' || c == 'N')        // NaN ok
                        return JSONNumber.parseJSON(parseState);
                    break;
            }
        }
        return null;
    }

    public static JSONValue parseJSONValue(String str) {
        return parseJSONValue(new ParseState(str, 0));
    }

    public void write(Writer writer) throws IOException {
        writer.write(toString());
    }

    public static void writeQuoteEscape(Writer writer, String str) throws IOException {
        writer.write('"');
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (c < ' ') {
                writer.write('\\');
                switch (c) {
                    case '\b':
                        writer.write('b');
                        break;
                    case '\r':
                        writer.write('r');
                        break;
                    case '\n':
                        writer.write('n');
                        break;
                    case '\t':
                        writer.write('t');
                        break;
                    case '\f':
                        writer.write('f');
                        break;
                    default:
                        writer.write(String.format("\\u%04x", (int) c));
                        break;
                }
            } else if (c == '"' || c == '\\') {
                writer.write('\\');
                writer.write(c);
            } else if (c > 127) {
                writer.write(String.format("\\u%04x", (int) c));
            } else
                writer.write(c);
        }
        writer.write('"');
    }

    public String stringValue() {
        return this instanceof JSONString ? ((JSONString) this).getValue() : toString();
    }

    public double doubleValue() {
        return this instanceof JSONNumber ? ((JSONNumber) this).getValue() : 0.0;
    }

    public int intValue() {
        return this instanceof JSONNumber ? (int) ((JSONNumber) this).getValue() : 0;
    }

    public long longValue() {
        return this instanceof JSONNumber ? (long) ((JSONNumber) this).getValue() : 0L;
    }

    public boolean booleanValue() {
        return this instanceof JSONBoolean ? ((JSONBoolean) this).getValue() : false;
    }

    public String toStringIndent(String indent) {
        return toString();
    }

    public JSONValue get(String key) {
        return ((JSONObject) this).get(key);
    }

    public JSONValue get(int index) {
        return ((JSONArray) this).get(index);
    }

}
