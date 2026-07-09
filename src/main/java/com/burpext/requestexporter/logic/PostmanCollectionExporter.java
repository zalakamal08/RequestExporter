package com.burpext.requestexporter.logic;

import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Serializes selected Burp requests as a Postman Collection v2.1 JSON file,
 * so a captured Proxy/Repeater selection can be dropped straight into Postman.
 */
public final class PostmanCollectionExporter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PostmanCollectionExporter() {
    }

    public static void export(java.util.List<HttpRequestResponse> selected, File outFile) throws IOException {
        ObjectNode collection = MAPPER.createObjectNode();

        ObjectNode info = collection.putObject("info");
        info.put("_postman_id", UUID.randomUUID().toString());
        info.put("name", "Request Exporter Collection");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");

        ArrayNode items = collection.putArray("item");
        for (int i = 0; i < selected.size(); i++) {
            items.add(requestToItem(selected.get(i), i + 1));
        }

        MAPPER.writerWithDefaultPrettyPrinter().writeValue(outFile, collection);
    }

    private static ObjectNode requestToItem(HttpRequestResponse rr, int requestIndex) {
        HttpRequest req = rr.request();

        ObjectNode item = MAPPER.createObjectNode();
        item.put("name", "Request " + requestIndex + " - " + req.method() + " " + req.path());

        ObjectNode request = item.putObject("request");
        request.put("method", req.method());

        ArrayNode headers = request.putArray("header");
        for (HttpHeader h : req.headers()) {
            if ("content-length".equalsIgnoreCase(h.name())) continue;
            ObjectNode header = headers.addObject();
            header.put("key", h.name());
            header.put("value", h.value());
        }

        String body = req.bodyToString();
        if (body != null && !body.isEmpty()) {
            ObjectNode bodyNode = request.putObject("body");
            bodyNode.put("mode", "raw");
            bodyNode.put("raw", body);
        }

        request.set("url", urlToJson(req));
        return item;
    }

    private static ObjectNode urlToJson(HttpRequest req) {
        ObjectNode url = MAPPER.createObjectNode();
        url.put("raw", req.url());
        url.put("protocol", req.httpService().secure() ? "https" : "http");

        ArrayNode host = url.putArray("host");
        for (String part : req.httpService().host().split("\\.")) {
            if (!part.isEmpty()) host.add(part);
        }

        String pathOnly = req.pathWithoutQuery();
        ArrayNode pathArray = url.putArray("path");
        for (String seg : pathOnly.split("/")) {
            if (!seg.isEmpty()) pathArray.add(seg);
        }

        String query = req.query();
        if (query != null && !query.isEmpty()) {
            ArrayNode queryArray = url.putArray("query");
            for (String pair : query.split("&")) {
                if (pair.isEmpty()) continue;
                int eq = pair.indexOf('=');
                ObjectNode q = queryArray.addObject();
                if (eq == -1) {
                    q.put("key", pair);
                    q.put("value", "");
                } else {
                    q.put("key", pair.substring(0, eq));
                    q.put("value", pair.substring(eq + 1));
                }
            }
        }

        return url;
    }
}
