package com.dtcluster.util;

import io.javalin.http.BadRequestResponse;

public final class Validate {
    private Validate() {}

    public static void notBlank(String v, String field) {
        if (v == null || v.isBlank()) throw new BadRequestResponse(field + " is required");
    }

    public static void positive(long v, String field) {
        if (v <= 0) throw new BadRequestResponse(field + " must be a positive number");
    }

    public static void nonNegative(double v, String field) {
        if (v < 0) throw new BadRequestResponse(field + " must not be negative");
    }
}
