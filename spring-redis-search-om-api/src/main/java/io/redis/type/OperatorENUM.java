package io.redis.type;

import java.util.Arrays;

public enum OperatorENUM {

    OR("OR"),
    AND("AND"),
    NOT("NOT");

    private String name;

    OperatorENUM(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static OperatorENUM fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(OperatorENUM.values())
                .filter(v -> v.name.equalsIgnoreCase(s))
                .findFirst()
                .orElse(OperatorENUM.AND);
    }
}