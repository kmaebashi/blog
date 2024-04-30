package com.kmaebashi.jsonparser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
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

    static JsonElement parse(String json) throws JsonParseException {
        JsonParserImpl parser = new JsonParserImpl(new StringReader(json));
        try {
            return parser.parse();
        } catch (IOException ex) {
            assert false : "文字列からのパースなのにIOExceptionが発生している";
        }
        return null; // make compiler happy.
    }
}
