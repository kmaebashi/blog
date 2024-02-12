package com.kmaebashi.jsonparserimpl;

public class Util {
    private Util() {}

    static void addIndent(StringBuilder sb, int indentLevel) {
        for (int i = 0; i < Constant.INDENT_OFFSET * indentLevel; i++) {
            sb.append(" ");
        }
    }
}
