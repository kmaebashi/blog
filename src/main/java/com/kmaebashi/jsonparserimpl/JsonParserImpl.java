package com.kmaebashi.jsonparserimpl;

import com.kmaebashi.jsonparser.JsonArray;
import com.kmaebashi.jsonparser.JsonElement;
import com.kmaebashi.jsonparser.JsonObject;
import com.kmaebashi.jsonparser.JsonParseException;
import com.kmaebashi.jsonparser.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

public class JsonParserImpl implements JsonParser {
    private final Reader reader;
    private Lexer lexer;
    private Token lookAheadToken;
    private boolean lookingAhead = false;

    public JsonParserImpl(Reader reader) {
        this.reader = reader;
    }

    private Token getToken() throws IOException, JsonParseException {
        if (this.lookingAhead) {
            this.lookingAhead = false;
            return lookAheadToken;
        } else {
            return lexer.getToken();
        }
    }

    private void ungetToken(Token token) {
        lookAheadToken = token;
        lookingAhead = true;
    }

    public JsonElement parse() throws IOException, JsonParseException {
        this.lexer = new Lexer(this.reader);

        return parseJsonElement();
    }

    JsonElement parseJsonElement() throws IOException, JsonParseException {
        Token token = getToken();

        if (token.type == TokenType.INT) {
            return new JsonValueImpl(token.intValue, token.lineNumber);
        } else if (token.type == TokenType.REAL) {
            return new JsonValueImpl(token.realValue, token.lineNumber);
        } else if (token.type == TokenType.STRING) {
            return new JsonValueImpl(token.tokenString, token.lineNumber);
        } else if (token.type == TokenType.TRUE) {
            return new JsonValueImpl(true, token.lineNumber);
        } else if (token.type == TokenType.FALSE) {
            return new JsonValueImpl(false, token.lineNumber);
        } else if (token.type == TokenType.NULL) {
            return new JsonValueImpl(token.lineNumber);
        } else if (token.type == TokenType.LEFT_BRACKET) {
            return parseArray(token);
        } else if (token.type == TokenType.LEFT_BRACE) {
            return parseObject(token);
        } else {
            throw new JsonParseException("不正なトークンです(" + token.type + ")", token.lineNumber);
        }
    }

    private JsonArray parseArray(Token leftBracketToken) throws IOException, JsonParseException {
        ArrayList<JsonElement> arrayList = new ArrayList<>();

        Token token;
        boolean tailComma = false;
        for (;;) {
            token = lexer.getToken();
            if (token.type == TokenType.RIGHT_BRACKET) {
                if (tailComma) {
                    throw new JsonParseException("JSONでは配列の末尾に,は付けられません", token.lineNumber);
                }
                break;
            }
            ungetToken(token);
            JsonElement elem = parseJsonElement();
            arrayList.add(elem);

            token = lexer.getToken();
            if (token.type != TokenType.COMMA) {
                break;
            }
            tailComma = true;
        }
        if (token.type != TokenType.RIGHT_BRACKET) {
            throw new JsonParseException("配列の要素の終わりがカンマでも]でもありません(" + token.type + ")",
                                         token.lineNumber);
        }
        return new JsonArrayImpl(Collections.unmodifiableList(arrayList),
                                            leftBracketToken.lineNumber, token.lineNumber);
    }

    private JsonObject parseObject(Token leftBraceToken) throws IOException, JsonParseException {
        Map<String, JsonElement> map = new LinkedHashMap<>();
        Map<String, Integer> keyLineNumberMap = new HashMap<>();

        Token token;
        boolean tailComma = false;
        for (;;) {
            token = lexer.getToken();
            if (token.type == TokenType.RIGHT_BRACE) {
                if (tailComma) {
                    throw new JsonParseException("JSONではオブジェクトの末尾に,は付けられません", token.lineNumber);
                }
                break;
            }
            ungetToken(token);
            Token keyToken = getToken();
            if (keyToken.type != TokenType.STRING) {
                throw new JsonParseException("オブジェクトのキーが文字列ではありません(" + keyToken.type + ")",
                                             token.lineNumber);
            }
            keyLineNumberMap.put(keyToken.tokenString, token.lineNumber);
            Token colonToken = getToken();
            if (colonToken.type != TokenType.COLON) {
                throw new JsonParseException("オブジェクトのキーの後ろがコロンではありません(" + colonToken.type + ")",
                        token.lineNumber);
            }
            JsonElement elem = parseJsonElement();
            if (map.containsKey(keyToken.tokenString)) {
                throw new JsonParseException("オブジェクトのキーが重複しています(" + keyToken.tokenString + ")",
                        token.lineNumber);
            }
            map.put(keyToken.tokenString, elem);

            token = lexer.getToken();
            if (token.type != TokenType.COMMA) {
                break;
            }
            tailComma = true;
        }
        if (token.type != TokenType.RIGHT_BRACE) {
            throw new JsonParseException("オブジェクトの要素の終わりがカンマでも}でもありません(" + token.type + ")",
                    token.lineNumber);
        }
        return new JsonObjectImpl(Collections.unmodifiableMap(map),
                                  leftBraceToken.lineNumber, token.lineNumber,
                                  Collections.unmodifiableMap(keyLineNumberMap));
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

}
