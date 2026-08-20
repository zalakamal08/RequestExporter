package com.burpext.requestexporter.logic;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Reads Burp's Proxy HTTP history and groups/filters it by target host, so the
 * RequestExporter UI tab can bulk-export every captured request for a chosen
 * host rather than requiring a manual selection.
 */
public final class ProxyHistoryHostFilter {

    private ProxyHistoryHostFilter() {
    }

    /** Distinct hosts seen in Proxy history, in first-seen order. */
    public static List<String> distinctHosts(MontoyaApi api) {
        LinkedHashSet<String> hosts = new LinkedHashSet<>();
        for (ProxyHttpRequestResponse item : api.proxy().history()) {
            hosts.add(item.request().httpService().host());
        }
        return new ArrayList<>(hosts);
    }

    /**
     * Every Proxy history entry for the given host, converted to
     * {@link HttpRequestResponse}, paired with its 1-based "#" row number in
     * the full (unfiltered) history.
     */
    public static Result forHost(MontoyaApi api, String host) {
        List<HttpRequestResponse> matches = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        List<ProxyHttpRequestResponse> history = api.proxy().history();
        for (int i = 0; i < history.size(); i++) {
            ProxyHttpRequestResponse item = history.get(i);
            if (!item.request().httpService().host().equalsIgnoreCase(host)) {
                continue;
            }
            matches.add(HttpRequestResponse.httpRequestResponse(item.finalRequest(), item.response()));
            indices.add(i + 1);
        }

        return new Result(matches, indices);
    }

    public static final class Result {
        public final List<HttpRequestResponse> requestResponses;
        public final List<Integer> requestIndices;

        Result(List<HttpRequestResponse> requestResponses, List<Integer> requestIndices) {
            this.requestResponses = requestResponses;
            this.requestIndices = requestIndices;
        }
    }
}
