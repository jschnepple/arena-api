package json;

public class ParseState {

    public String str;
    public int pos;
    public int length;

    public ParseState(String str, int pos) {
        this.str = str;
        this.pos = pos;
        this.length = str.length();
    }

    // returns true if current is still valid - i.e. there's still some chars left
    public boolean skipWhite() {
        while (pos < str.length() && (str.charAt(pos) == ' ' || str.charAt(pos) == '\t' || str.charAt(pos) == '\n' || str.charAt(pos) == '\r'))
            ++pos;
        return pos < str.length();
    }

    public char current() {
        return str.charAt(pos);
    }

    public char current(int offset) {
        return str.charAt(pos + offset);
    }

    public boolean hasMore() {
        return pos < length;
    }

    public boolean hasMore(int n) {
        return pos + n < length;
    }

    public boolean equalsHere(String s) {
        return str.regionMatches(pos, s, 0, s.length());
    }

    public String toString() {
        return "ParseState pos:" + pos + " length:" + length + "str:" + str.substring(pos);
    }

}
