package com.incomrecycle.common.json;

import com.google.code.microlog4android.format.PatternFormatter;
import java.io.StringWriter;

public class JSONStringer extends JSONWriter {
    public JSONStringer() {
        super(new StringWriter());
    }

    public String toString() {
        return this.mode == PatternFormatter.DATE_CONVERSION_CHAR ? this.writer.toString() : null;
    }
}
