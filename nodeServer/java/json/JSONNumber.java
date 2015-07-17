package json;

public class JSONNumber extends JSONValue {

    double doubleValue;
    String jsonStringValue = null;

    public JSONNumber(double d) {
        doubleValue = d;
    }

    public JSONValue makeCopy() {
        return new JSONNumber(doubleValue);
    }

    public String toString() {
        if (jsonStringValue == null) {
            if (Double.isNaN(doubleValue)) {
                jsonStringValue = "NaN";
            } else {
                if ((long) doubleValue == doubleValue)
                    jsonStringValue = Long.toString((long) doubleValue);    // just for nicer (w/o trailing .0) format
                else
                    jsonStringValue = Double.toString(doubleValue);
            }
        }
        return jsonStringValue;
    }

    public double getValue() {
        return doubleValue;
    }

    public static JSONNumber parseJSON(ParseState parseState) {
        JSONNumber jsonNumber = null;
        if (parseState.hasMore()) {
            if (parseState.equalsHere("NaN")) {
                parseState.pos += 3;
                jsonNumber = new JSONNumber(Double.NaN);
            } else {    // must be terminated with a "non-number" character
                int start = parseState.pos;
                while (parseState.hasMore()) {
                    char c = parseState.current();
                    if (c >= '0' && c <= '9' || c == '.' || c == 'e' || c == 'E' || c == '-' || c == '+')
                        ++parseState.pos;
                    else
                        break;
                }
                if (start < parseState.pos) {
                    try {
                        double d = Double.parseDouble(parseState.str.substring(start, parseState.pos));
                        jsonNumber = new JSONNumber(d);
                    } catch (NumberFormatException e) {
                    }
                }
                if (jsonNumber == null)
                    parseState.pos = start;
            }
        }
        return jsonNumber;
    }

}

