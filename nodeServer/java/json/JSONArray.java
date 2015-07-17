package json;

import java.io.*;
import java.util.*;

public class JSONArray extends JSONValue {
    List<JSONValue> list;
    String jsonStringValue = null;

    public JSONArray() {
        list = new ArrayList<JSONValue>();
    }

    public JSONArray(int size) {
        list = new ArrayList<JSONValue>(size);
    }

    public JSONValue makeCopy() {
        JSONArray ja = new JSONArray(list.size());
        for (JSONValue jsonValue : list)
            ja.add(jsonValue.makeCopy());
        return ja;
    }

    public int size() {
        return list.size();
    }

    public List<JSONValue> getValue() {
        return list;
    }

    public JSONValue get(int index) {
        return list.get(index);
    }

    public void set(int index, JSONValue value) {
        list.set(index, value);
        jsonStringValue = null;
    }

    public void add(JSONValue jsonValue) {
        list.add(jsonValue);
        jsonStringValue = null;
    }

    public String toString() {
        if (jsonStringValue == null) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            boolean first = true;
            for (JSONValue jsonValue : list) {
                if (!first)
                    sb.append(',');
                else
                    first = false;
                sb.append(jsonValue.toString());
            }
            sb.append(']');
            jsonStringValue = sb.toString();
        }
        return jsonStringValue;
    }

    public String toStringIndent(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        String newIndent = '\n' + indent;
        boolean first = true;
        for (JSONValue jsonValue : list) {
            sb.append(newIndent);
            if (first) {
                newIndent = ",\n" + indent;
                first = false;
            }
            sb.append(jsonValue.toStringIndent(indent + "  "));
        }
        sb.append('\n');
        sb.append(indent);
        sb.append(']');
        return sb.toString();
    }

    public static JSONArray parseJSON(ParseState parseState) {
        JSONArray jsonArray = new JSONArray();
        boolean isOK = false;
        if (parseState.hasMore(2) && parseState.current() == '[') {    // open and close brace at least
            ++parseState.pos;
            do {
                isOK = false;
                if (parseState.skipWhite()) {
                    if (parseState.current() == ']') {
                        ++parseState.pos;
                        return jsonArray;
                    } else {
                        JSONValue jsonValue = JSONValue.parseJSONValue(parseState);
                        if (jsonValue != null) {
                            jsonArray.add(jsonValue);
                            if (parseState.skipWhite()) {
                                if (parseState.current() != ']') {
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
            } while (isOK);
        }
        return null;
    }

    public void write(Writer writer) throws IOException {
        writer.write('[');
        boolean first = true;
        for (JSONValue jsonValue : list) {
            if (!first)
                writer.write(',');
            else
                first = false;
            jsonValue.write(writer);
        }
        writer.write(']');
    }
}
