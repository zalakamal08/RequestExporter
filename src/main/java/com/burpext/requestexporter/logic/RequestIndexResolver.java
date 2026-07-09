package com.burpext.requestexporter.logic;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Resolves the "Request Index" shown next to each exported entry so it matches
 * Burp's own "#" row number in Proxy > HTTP history, rather than an arbitrary
 * 1..N ordinal over just the current selection.
 *
 * The Montoya API doesn't expose that row number directly on a selected
 * HttpRequestResponse, so for Proxy-sourced selections it is recovered by
 * matching raw request bytes against api.proxy().history() (whose list order
 * is exactly the "#" column) and using the matching entry's 1-based position.
 * Repeater has no equivalent history/index API, so its entries fall back to
 * their ordinal position within the current selection.
 */
public final class RequestIndexResolver {

    private RequestIndexResolver() {
    }

    public static List<Integer> resolve(MontoyaApi api, ToolType toolType, List<HttpRequestResponse> selected) {
        List<Integer> indices = new ArrayList<>();

        if (toolType == ToolType.PROXY) {
            List<ProxyHttpRequestResponse> history = api.proxy().history();
            List<byte[]> historyBytes = new ArrayList<>(history.size());
            for (ProxyHttpRequestResponse h : history) {
                historyBytes.add(h.request().toByteArray().getBytes());
            }

            boolean[] consumed = new boolean[historyBytes.size()];
            for (HttpRequestResponse rr : selected) {
                byte[] reqBytes = rr.request().toByteArray().getBytes();
                int foundAt = -1;
                for (int i = 0; i < historyBytes.size(); i++) {
                    if (!consumed[i] && Arrays.equals(historyBytes.get(i), reqBytes)) {
                        foundAt = i;
                        break;
                    }
                }
                if (foundAt >= 0) {
                    consumed[foundAt] = true;
                }
                indices.add(foundAt >= 0 ? foundAt + 1 : -1);
            }
        } else {
            for (int i = 0; i < selected.size(); i++) {
                indices.add(-1);
            }
        }

        for (int i = 0; i < indices.size(); i++) {
            if (indices.get(i) == -1) {
                indices.set(i, i + 1);
            }
        }
        return indices;
    }
}
