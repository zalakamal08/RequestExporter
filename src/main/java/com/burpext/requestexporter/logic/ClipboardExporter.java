package com.burpext.requestexporter.logic;

import burp.api.montoya.http.message.HttpRequestResponse;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

/**
 * Formats selected requests/responses as plain text and copies them to the
 * system clipboard, one "Request Index: N" block per entry, in ascending
 * order of that index (see RequestIndexResolver for how N is derived).
 */
public final class ClipboardExporter {

    private ClipboardExporter() {
    }

    public static void copyToClipboard(List<HttpRequestResponse> selected, List<Integer> requestIndices) {
        copyToClipboard(selected, requestIndices, true);
    }

    public static void copyToClipboard(List<HttpRequestResponse> selected, List<Integer> requestIndices,
                                        boolean includeResponse) {
        List<Integer> sortOrder = SelectionOrdering.byAscendingIndex(requestIndices);

        StringBuilder sb = new StringBuilder();
        for (int pos = 0; pos < sortOrder.size(); pos++) {
            int i = sortOrder.get(pos);
            HttpRequestResponse rr = selected.get(i);

            sb.append("Request Index: ").append(requestIndices.get(i)).append("\n\n");

            sb.append("Request\n");
            sb.append(rr.request() != null ? rr.request().toString() : "");

            if (includeResponse) {
                sb.append("\n\n");
                sb.append("Response\n");
                sb.append(rr.hasResponse() ? rr.response().toString() : "");
            }

            if (pos < sortOrder.size() - 1) {
                sb.append("\n\n").append("=".repeat(60)).append("\n\n");
            }
        }

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(sb.toString()), null);
    }
}
