package com.dtcluster.db;

import org.bson.Document;

/** Converts MongoDB documents to API documents with the existing public id field. */
public final class MongoDocs {
    private MongoDocs() {}

    public static long id(Document document) { return ((Number) document.get("_id")).longValue(); }
    public static long number(Document document, String key) { return ((Number) document.get(key)).longValue(); }
    public static Document api(Document document) {
        if (document == null) return null;
        Document result = new Document(document);
        Object id = result.remove("_id");
        if (id != null) result.put("id", id);
        return result;
    }
}
