package com.dtcluster.util;

import io.javalin.http.Context;

public final class Http {
    private Http() {}

    public static long id(Context ctx) {
        return ctx.pathParamAsClass("id", Long.class).get();
    }

    /** ?hours=N for dashboard windows; defaults to 24, capped at 720 (30 days). */
    public static int hours(Context ctx) {
        int h = ctx.queryParamAsClass("hours", Integer.class).getOrDefault(24);
        return Math.max(1, Math.min(h, 720));
    }
}
