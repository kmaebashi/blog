package com.kmaebashi.jsonparserimpl;

public class Token {
    final TokenType type;
    final String tokenString;
    int intValue;
    double realValue;
    final int lineNumber;

    Token(TokenType type, String tokenString, int lineNumber) {
        this.type = type;
        this.tokenString = tokenString;
        this.lineNumber = lineNumber;
    }

    Token(TokenType type, String tokenString, int intValue, int lineNumber) {
        this(type, tokenString, lineNumber);
        this.intValue = intValue;
    }

    Token(TokenType type, String tokenString, double realValue, int lineNumber) {
        this(type, tokenString, lineNumber);
        this.realValue = realValue;
    }
}
