package json;

import java.io.*;

public class JSONString extends JSONValue {

    String stringValue;
    String jsonStringValue = null;

    public JSONString(String str) {
        stringValue = str;
    }

    public JSONValue makeCopy() {
        return new JSONString(stringValue);
    }

    public String getValue() {
        return stringValue;
    }

    public String toString() {
        if (jsonStringValue == null)
            jsonStringValue = quoteEscapeString(stringValue);
        return jsonStringValue;
    }

    public static JSONString parseJSON(ParseState parseState) {
        JSONString jsonString = null;

        if (parseState.hasMore(2) && parseState.current() == '"') {    // space for two quotes at least
            int start = ++parseState.pos;
            while (parseState.hasMore()) {
                if (parseState.current() == '"') {
                    int k = 1;
                    while (parseState.current(-k) == '\\')
                        ++k;
                    if ((k & 1) != 0)     // even number of backslashes precede the quote
                        break;
                }
                ++parseState.pos;
            }
            if (parseState.hasMore()) {
                ++parseState.pos;    // must be a quote
                String s = unEscapeString(parseState.str.substring(start, parseState.pos - 1));
                jsonString = new JSONString(s);
            }
        }
        return jsonString;
    }

    public void write(Writer writer) throws IOException {
        writeQuoteEscape(writer, stringValue);
    }

}
