package com.burpext.requestexporter.logic;

import burp.api.montoya.http.message.HttpRequestResponse;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

/**
 * Formats selected requests/responses as plain text and copies them to the
 * system clipboard, one "Request Index: N" block per entry in the order the
 * selection was supplied (already ascending, per Burp's table/tab order).
 */
public final class ClipboardExporter {

    private ClipboardExporter() {
    }

    public static void copyToClipboard(List<HttpRequestResponse> selected) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < selected.size(); i++) {
            HttpRequestResponse rr = selected.get(i);
            int requestIndex = i + 1;

            sb.append("Request Index: ").append(requestIndex).append("\n\n");

            sb.append("Request\n");
            sb.append(rr.request() != null ? rr.request().toString() : "");
            sb.append("\n\n");

            sb.append("Response\n");
            sb.append(rr.hasResponse() ? rr.response().toString() : "");

            if (i < selected.size() - 1) {
                sb.append("\n\n").append("=".repeat(60)).append("\n\n");
            }
        }

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(sb.toString()), null);
    }
}
