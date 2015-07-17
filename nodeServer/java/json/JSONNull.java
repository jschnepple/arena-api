package json;

public class JSONNull extends JSONValue {

    public JSONNull() {
    }

    public JSONValue makeCopy() {
        return new JSONNull();
    }

    public String toString() {
        return "null";
    }

    public Object getValue() {
        return null;
    }

    public static JSONNull parseJSON(ParseState parseState) {
        JSONNull jsonNull;
        if (parseState.equalsHere("null")) {
            parseState.pos += 4;
            jsonNull = new JSONNull();
        } else
            jsonNull = null;
        return jsonNull;
    }
}

