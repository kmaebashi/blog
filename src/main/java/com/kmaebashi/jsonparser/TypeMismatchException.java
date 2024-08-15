package com.kmaebashi.jsonparser;

public class TypeMismatchException extends Exception {
    public TypeMismatchException(JsonElement actual, String expected) {
        super(createMessage(actual, expected));
    }

    private static String createMessage(JsonElement actual, String expected) {
        String actualType;

        if (actual instanceof JsonArray) {
            actualType = "配列";
        } else if (actual instanceof JsonObject) {
            actualType = "オブジェクト";
        } else {
            actualType = ((JsonValue)actual).getType().toString();
        }
        return "型の不一致エラー。"
                + expected + "に対して"
                + actualType + "を設定しようとしています。"
                + "(line:" + actual.getLineNumber() + ")。";
    }
}
