package com.kmaebashi.jsonparserimpl;
import com.kmaebashi.jsonparser.JsonArray;
import com.kmaebashi.jsonparser.JsonElement;

import java.util.List;

public class JsonArrayImpl implements JsonArray {
    private final List<JsonElement> array;
    private final int leftBracketLineNumber;
    private final int rightBracketLineNumber;

    public JsonArrayImpl(List<JsonElement> arrayList,
                         int leftBracketLineNumber,
                         int rightBracketLineNumber) {
        this.array = arrayList;
        this.leftBracketLineNumber = leftBracketLineNumber;
        this.rightBracketLineNumber = rightBracketLineNumber;
    }

    @Override
    public List<JsonElement> getArray() {
        return this.array;
    }

    @Override
    public int getLeftBracketLineNumber() {
        return this.leftBracketLineNumber;
    }

    @Override
    public int getRightBracketLineNumber() {
        return this.rightBracketLineNumber;
    }

    @Override
    public String stringify() {
        StringBuilder sb = new StringBuilder();
        stringifySub(sb, 0);
        return sb.toString();
    }

    void stringifySub(StringBuilder sb, int indentLevel) {
        sb.append("[" + Constant.LINE_SEPARATOR);

        boolean isFirst = true;
        for (JsonElement elem : this.array) {
            if (!isFirst) {
                sb.append("," + Constant.LINE_SEPARATOR);
            }
            isFirst = false;
            if (elem instanceof JsonObjectImpl objElem) {
                Util.addIndent(sb, indentLevel + 1);
                objElem.stringifySub(sb, indentLevel + 1);
            } else if (elem instanceof JsonArrayImpl arrayElem) {
                Util.addIndent(sb, indentLevel + 1);
                arrayElem.stringifySub(sb, indentLevel + 1);
            } else if (elem instanceof JsonValueImpl valueElem) {
                Util.addIndent(sb, indentLevel + 1);
                valueElem.stringifySub(sb);
            }
        }
        sb.append(Constant.LINE_SEPARATOR);
        Util.addIndent(sb, indentLevel);
        sb.append("]");
    }
}
