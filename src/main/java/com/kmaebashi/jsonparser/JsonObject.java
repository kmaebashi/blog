package com.kmaebashi.jsonparser;
import com.kmaebashi.jsonparserimpl.JsonObjectImpl;

import java.util.Map;

public interface JsonObject extends JsonElement {
    Map<String, JsonElement> getMap();
    int getLeftBraceLineNumber();
    int getRightBraceLineNumber();
    int getKeyLineNumber(String key);
    @Override
    String stringify();

    static JsonObject newInstance(Map<String, JsonElement> map) {
        return new JsonObjectImpl(map, 0, 0, null);
    }
}
