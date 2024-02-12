package com.kmaebashi.jsonparser;

import com.kmaebashi.jsonparserimpl.JsonArrayImpl;

import java.util.List;

public interface JsonArray extends JsonElement {
    List<JsonElement> getArray();
    int getLeftBracketLineNumber();
    int getRightBracketLineNumber();
    @Override
    String stringify();

    static JsonArray newInstance(List<JsonElement> array) {
        return new JsonArrayImpl(array, 0, 0);
    }
}
