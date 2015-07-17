package json;

import java.io.*;
import java.util.*;

public class JSONObject extends JSONValue {

    Map<String, JSONValue> map = new LinkedHashMap<String, JSONValue>();
    String jsonStringValue = null;

    public JSONObject() {
    }

    public JSONValue makeCopy() {
        JSONObject jo = new JSONObject();
        for (Map.Entry<String, JSONValue> entry : map.entrySet())
            jo.put(entry.getKey(), entry.getValue().makeCopy());
        return jo;
    }

    public String toString() {
        if (jsonStringValue == null) {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, JSONValue> entry : map.entrySet()) {
                if (!first)
                    sb.append(',');
                else
                    first = false;
                sb.append(quoteEscapeString(entry.getKey()));
                sb.append(':');
                sb.append(entry.getValue());
            }
            sb.append('}');
            jsonStringValue = sb.toString();
        }
        return jsonStringValue;
    }

    public String toStringIndent(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        String newIndent = '\n' + indent;
        boolean first = true;
        for (Map.Entry<String, JSONValue> entry : map.entrySet()) {
            sb.append(newIndent);
            if (first) {
                newIndent = ",\n" + indent;
                first = false;
            }
            sb.append(quoteEscapeString(entry.getKey()));
            sb.append(": ");
            sb.append(entry.getValue().toStringIndent(indent + "  "));
        }
        sb.append('\n');
        sb.append(indent);
        sb.append('}');
        return sb.toString();
    }

    public Map<String, JSONValue> getValue() {
        return map;
    }

    public JSONValue get(String key) {
        return map.get(key);
    }

    public void put(String key, JSONValue jsonValue) {
        map.put(key, jsonValue);
        jsonStringValue = null;
    }

    public void put(String key, String value) {
        put(key, new JSONString(value));
    }

    public void put(String key, double value) {
        put(key, new JSONNumber(value));
    }

    public void put(String key, boolean value) {
        put(key, new JSONBoolean(value));
    }

    public void remove(String key) {
        map.remove(key);
    }

    public static JSONObject parseJSON(ParseState parseState) {
        JSONObject jsonObject = new JSONObject();
        boolean isOK = false;
        if (parseState.hasMore(2) && parseState.current() == '{') {    // open and close brace at least
            ++parseState.pos;
            do {
                isOK = false;
                if (parseState.skipWhite()) {
                    if (parseState.current() == '}') {
                        ++parseState.pos;
                        return jsonObject;
                    } else {
                        JSONString key = JSONString.parseJSON(parseState);
                        if (key != null && parseState.skipWhite() && parseState.current() == ':') {
                            ++parseState.pos;
                            JSONValue jsonValue = JSONValue.parseJSONValue(parseState);
                            if (jsonValue != null) {
                                jsonObject.put(key.getValue(), jsonValue);
                                if (parseState.skipWhite()) {
                                    if (parseState.current() != '}') {
                                        if (parseState.current() == ',') {
                                            ++parseState.pos;
                                            isOK = true;
                                        }
                                    } else
                                        isOK = true;
                                }
                            }
                        }
                    }
                }
            } while (isOK);
        }
        return null;
    }

    public void write(Writer writer) throws IOException {
        writer.write('{');
        boolean first = true;
        for (Map.Entry<String, JSONValue> entry : map.entrySet()) {
            if (!first)
                writer.write(',');
            else
                first = false;
            writeQuoteEscape(writer, entry.getKey());
            writer.write(':');
            entry.getValue().write(writer);
        }
        writer.write('}');
    }

}
