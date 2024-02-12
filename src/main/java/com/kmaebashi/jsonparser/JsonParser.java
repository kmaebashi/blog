package com.kmaebashi.jsonparser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import com.kmaebashi.jsonparserimpl.JsonParserImpl;

public interface JsonParser extends AutoCloseable {
    static JsonParser newInstance(String path)
            throws IOException {
        Reader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(path), StandardCharsets.UTF_8));
        return new JsonParserImpl(reader);
    }

    static JsonParser newInstance(Reader reader) {
        return new JsonParserImpl(reader);
    }

    JsonElement parse() throws IOException, JsonParseException;
    void close() throws IOException;
}
