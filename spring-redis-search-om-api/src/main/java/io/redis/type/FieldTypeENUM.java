package io.redis.type;

import lombok.Getter;

@Getter
public enum FieldTypeENUM {

    TAG("{", "}"),
    TEXT("(", ")"),
    NUMERIC("[", "]");

    private String delimiterStart;
    private String delimiterEnd;

    FieldTypeENUM(String delimiterStart, String delimiterEnd) {
        this.delimiterStart = delimiterStart;
        this.delimiterEnd = delimiterEnd;
    }

}