package com.burpext.requestexporter;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import com.burpext.requestexporter.logic.ClipboardExporter;
import com.burpext.requestexporter.logic.PostmanCollectionExporter;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Burp Suite extension entry point.
 * Adds a "Request Exporter" context menu to the Proxy and Repeater tabs for
 * exporting selected requests/responses to the clipboard or a Postman collection.
 */
public class RequestExporter implements BurpExtension {

    private MontoyaApi api;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("Request Exporter");
        api.logging().logToOutput("Request Exporter loaded. Right-click selected requests in Proxy or Repeater.");

        api.userInterface().registerContextMenuItemsProvider(new ContextMenuItemsProvider() {
            @Override
            public List<Component> provideMenuItems(ContextMenuEvent event) {
                return buildMenu(event);
            }
        });
    }

    private List<Component> buildMenu(ContextMenuEvent event) {
        if (!event.isFromTool(ToolType.PROXY, ToolType.REPEATER)) {
            return List.of();
        }

        List<HttpRequestResponse> selected = collectSelection(event);
        if (selected.isEmpty()) {
            return List.of();
        }

        JMenu menu = new JMenu("Request Exporter");

        JMenuItem copyItem = new JMenuItem("Copy to Clipboard");
        copyItem.addActionListener(e -> copyToClipboard(selected));

        JMenuItem postmanItem = new JMenuItem("Create Postman Collection");
        postmanItem.addActionListener(e -> exportToPostman(selected));

        menu.add(copyItem);
        menu.add(postmanItem);
        return List.of(menu);
    }

    /**
     * selectedRequestResponses() reflects the order Burp displays entries in the
     * source table (Proxy history / Repeater tab list), which is already ascending.
     * Falls back to the single message-editor selection when no list selection exists.
     */
    private List<HttpRequestResponse> collectSelection(ContextMenuEvent event) {
        List<HttpRequestResponse> selected = event.selectedRequestResponses();
        if (!selected.isEmpty()) {
            return new ArrayList<>(selected);
        }
        List<HttpRequestResponse> single = new ArrayList<>();
        event.messageEditorRequestResponse().ifPresent(mer -> single.add(mer.requestResponse()));
        return single;
    }

    private void copyToClipboard(List<HttpRequestResponse> selected) {
        try {
            ClipboardExporter.copyToClipboard(selected);
            api.logging().logToOutput("Copied " + selected.size() + " request(s) to clipboard.");
        } catch (Exception ex) {
            api.logging().logToError("Failed to copy to clipboard: " + ex.getMessage());
        }
    }

    private void exportToPostman(List<HttpRequestResponse> selected) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Postman Collection");
        chooser.setSelectedFile(new File("RequestExporter-collection.json"));
        int result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File outFile = chooser.getSelectedFile();
        if (!outFile.getName().toLowerCase().endsWith(".json")) {
            outFile = new File(outFile.getParentFile(), outFile.getName() + ".json");
        }

        try {
            PostmanCollectionExporter.export(selected, outFile);
            api.logging().logToOutput("Postman collection saved to " + outFile.getAbsolutePath());
            JOptionPane.showMessageDialog(null, "Postman collection saved:\n" + outFile.getAbsolutePath());
        } catch (Exception ex) {
            api.logging().logToError("Failed to export Postman collection: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to export: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
