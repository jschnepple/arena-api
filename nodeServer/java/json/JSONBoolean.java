package json;

public class JSONBoolean extends JSONValue {

    boolean boolValue;

    public JSONBoolean(boolean b) {
        boolValue = b;
    }

    public JSONValue makeCopy() {
        return new JSONBoolean(boolValue);
    }

    public String toString() {
        return boolValue ? "true" : "false";
    }

    public boolean getValue() {
        return boolValue;
    }

    public static JSONBoolean parseJSON(ParseState parseState) {
        JSONBoolean jsonBoolean;
        if (parseState.equalsHere("true")) {
            parseState.pos += 4;
            jsonBoolean = new JSONBoolean(true);
        } else if (parseState.equalsHere("false")) {
            parseState.pos += 5;
            jsonBoolean = new JSONBoolean(false);
        } else
            jsonBoolean = null;
        return jsonBoolean;
    }
}
