package com.incomrecycle.common.json;

import com.google.code.microlog4android.format.PatternFormatter;
import it.sauronsoftware.ftp4j.FTPCodes;

public class JSONTokener {
    private int myIndex = 0;
    private String mySource;

    public JSONTokener(String s) {
        this.mySource = s;
    }

    public void back() {
        if (this.myIndex > 0) {
            this.myIndex--;
        }
    }

    public static int dehexchar(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        //         if (c >= 'A' && c <= PatternFormatter.LOGGER_LOCATION_FILE) {

        if (c >= 'A' && c <= PatternFormatter.RELATIVE_TIME_CONVERSION_CHAR) {
            return c - 55;
        }
        if (c < 'a' || c > 'f') {
            return -1;
        }
        return c - 87;
    }

    public boolean more() {
        if (this.mySource != null && this.myIndex < this.mySource.length()) {
            return true;
        }
        return false;
    }

    public char next() {
        if (!more()) {
            return '\u0000';
        }
        char c = this.mySource.charAt(this.myIndex);
        this.myIndex++;
        return c;
    }

    public char next(char c) throws JSONException {
        char n = next();
        if (n == c) {
            return n;
        }
        throw syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
    }

    public String next(int n) throws JSONException {
        int i = this.myIndex;
        int j = i + n;
        if (j >= this.mySource.length()) {
            throw syntaxError("Substring bounds error");
        }
        this.myIndex += n;
        return this.mySource.substring(i, j);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public char nextClean() throws com.incomrecycle.common.json.JSONException {
        char next;
        char c = '\0';
        char next2;
        char next3;
        char next4;
        while (true) {
            next = this.next();
            if (next == '/') {
                switch (this.next()) {
                    default: {
                        this.back();
                        c = '/';
                        break;
                    }
                    case '/': {
                        do {
                            next2 = this.next();
                            if (next2 != '\n' && next2 != '\r') {
                                continue;
                            }
                            break;
                        } while (next2 != '\0');
                        continue;
                    }
                    case '*': {
                        while (true) {
                            do {
                                this.back();
                                next3 = this.next();
                                if (next3 == '\0') {
                                    throw this.syntaxError("Unclosed comment");
                                }
                                if (next3 == '*') {
                                    continue;
                                }
                            } while (this.next() != '/');
                            break;
                        }
                        break;
                    }
                }
            } else if (next == '#') {
                do {
                    next4 = this.next();
                    if (next4 != '\n' && next4 != '\r') {
                        continue;
                    }
                    break;
                } while (next4 != '\0');
            } else {
                if ((c = next) == '\0') {
                    break;
                }
                if (next > ' ') {
                    return next;
                }
                continue;
            }
        }
        return c;
    }

    public String nextString(char quote) throws JSONException {
        StringBuffer sb = new StringBuffer();
        while (true) {
            char c = next();
            switch (c) {
                case '\u0000':
                case '\n':
                case '\r':
                    throw syntaxError("Unterminated string");
                case '\\':
                    c = next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case FTPCodes.RESTART_MARKER /*110*/:
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            sb.append((char) Integer.parseInt(next(4), 16));
                            break;
                        case FTPCodes.SERVICE_NOT_READY /*120*/:
                            sb.append((char) Integer.parseInt(next(2), 16));
                            break;
                        default:
                            sb.append(c);
                            break;
                    }
                default:
                    if (c != quote) {
                        sb.append(c);
                        break;
                    }
                    return sb.toString();
            }
        }
    }

    public String nextTo(final char c) {
        final StringBuffer sb = new StringBuffer();
        char next;
        while (true) {
            next = this.next();
            if (next == c || next == '\0' || next == '\n' || next == '\r') {
                break;
            }
            sb.append(next);
        }
        if (next != '\0') {
            this.back();
        }
        return sb.toString().trim();
    }

    public String nextTo(final String s) {
        final StringBuffer sb = new StringBuffer();
        char next;
        while (true) {
            next = this.next();
            if (s.indexOf(next) >= 0 || next == '\0' || next == '\n' || next == '\r') {
                break;
            }
            sb.append(next);
        }
        if (next != '\0') {
            this.back();
        }
        return sb.toString().trim();
    }

    public Object nextValue() throws JSONException {
        char c = nextClean();
        switch (c) {
            case '\"':
            case '\'':
                return nextString(c);
            case '(':
            case '[':
                back();
                return new JSONArray(this);
            case '{':
                back();
                return new JSONObject(this);
            default:
                StringBuffer sb = new StringBuffer();
                char b = c;
                while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
                    sb.append(c);
                    c = next();
                }
                back();
                String s = sb.toString().trim();
                if (s.equals("")) {
                    throw syntaxError("Missing value");
                } else if (s.equals("true")) {
                    return Boolean.TRUE;
                } else {
                    if (s.equals("false")) {
                        return Boolean.FALSE;
                    }
                    if (s.equals("null")) {
                        return JSONObject.NULL;
                    }
                    if ((b < '0' || b > '9') && b != '.' && b != '-' && b != '+') {
                        return s;
                    }
                    if (b == '0') {
                        if (s.length() <= 2 || !(s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                            try {
                                return new Integer(Integer.parseInt(s, 8));
                            } catch (Exception e) {
                            }
                        } else {
                            try {
                                return new Integer(Integer.parseInt(s.substring(2), 16));
                            } catch (Exception e2) {
                            }
                        }
                    }
                    try {
                        return new Integer(s);
                    } catch (Exception e3) {
                        try {
                            return new Long(s);
                        } catch (Exception e4) {
                            try {
                                return new Double(s);
                            } catch (Exception e5) {
                                return s;
                            }
                        }
                    }
                }
        }
    }

    public char skipTo(char to) {
        char c;
        int index = this.myIndex;
        do {
            c = next();
            if (c == '\u0000') {
                this.myIndex = index;
                break;
            }
        } while (c != to);
        back();
        return c;
    }

    public boolean skipPast(String to) {
        this.myIndex = this.mySource.indexOf(to, this.myIndex);
        if (this.myIndex < 0) {
            this.myIndex = this.mySource.length();
            return false;
        }
        this.myIndex += to.length();
        return true;
    }

    public JSONException syntaxError(String message) {
        return new JSONException(message + toString());
    }

    public String toString() {
        return " at character " + this.myIndex + " of " + this.mySource;
    }
}
