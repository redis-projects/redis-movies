package io.redis.type;

import java.util.Arrays;

public enum FieldENUM {

    ACTORS("actors"),
    GENRE("genre"),
    DIRECTOR("director"),
    RATING("rating"),
    RUNTIME("runtime"),
    RELEASE_YEAR("year"),
    META_RATING("metascore");

    private String fieldName;

    FieldENUM(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public static FieldENUM fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(FieldENUM.values())
                .filter(v -> v.fieldName.equals(s))
                .findFirst()
                .orElse(null);
    }
}