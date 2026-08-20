package com.burpext.requestexporter.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import com.burpext.requestexporter.logic.PostmanCollectionExporter;
import com.burpext.requestexporter.logic.ProxyHistoryHostFilter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.List;

/**
 * "Request Exporter" suite tab: lets the user pick a host seen in Proxy HTTP
 * history and export every request captured for that host (optionally with
 * responses) as a single Postman collection.
 */
public class RequestExporterTab extends JPanel {

    private final MontoyaApi api;
    private final JComboBox<String> hostCombo = new JComboBox<>();
    private final JCheckBox includeResponseCheck = new JCheckBox("Include responses", true);
    private final JLabel statusLabel = new JLabel(" ");

    public RequestExporterTab(MontoyaApi api) {
        this.api = api;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildControls(), BorderLayout.NORTH);
        add(buildFooter(), BorderLayout.SOUTH);
        refreshHosts();
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JLabel credit = new JLabel("<html><span style='color:#8a8a8a;'>Built by</span> "
                + "<span style='color:#3574f0; font-weight:bold;'>@zalakamal08</span></html>");
        footer.add(Box.createHorizontalGlue());
        footer.add(credit);
        return footer;
    }

    private JPanel buildControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.add(new JLabel("Host: "));
        hostCombo.setMaximumSize(new java.awt.Dimension(400, hostCombo.getPreferredSize().height));
        row1.add(hostCombo);
        row1.add(Box.createHorizontalStrut(8));
        JButton refreshButton = new JButton("Refresh Hosts");
        refreshButton.addActionListener(e -> refreshHosts());
        row1.add(refreshButton);
        row1.add(Box.createHorizontalGlue());
        alignLeft(row1);
        panel.add(row1);

        panel.add(Box.createVerticalStrut(8));

        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
        row2.add(includeResponseCheck);
        row2.add(Box.createHorizontalGlue());
        alignLeft(row2);
        panel.add(row2);

        panel.add(Box.createVerticalStrut(8));

        JPanel row3 = new JPanel();
        row3.setLayout(new BoxLayout(row3, BoxLayout.X_AXIS));
        JButton exportButton = new JButton("Export All Requests for Host to Postman Collection");
        exportButton.addActionListener(e -> exportSelectedHost());
        row3.add(exportButton);
        row3.add(Box.createHorizontalGlue());
        alignLeft(row3);
        panel.add(row3);

        panel.add(Box.createVerticalStrut(8));

        JPanel row4 = new JPanel();
        row4.setLayout(new BoxLayout(row4, BoxLayout.X_AXIS));
        row4.add(statusLabel);
        row4.add(Box.createHorizontalGlue());
        alignLeft(row4);
        panel.add(row4);

        return panel;
    }

    private void alignLeft(JPanel row) {
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void refreshHosts() {
        String previouslySelected = (String) hostCombo.getSelectedItem();
        List<String> hosts = ProxyHistoryHostFilter.distinctHosts(api);

        hostCombo.removeAllItems();
        for (String host : hosts) {
            hostCombo.addItem(host);
        }
        if (previouslySelected != null && hosts.contains(previouslySelected)) {
            hostCombo.setSelectedItem(previouslySelected);
        }

        statusLabel.setText(hosts.size() + " host(s) found in Proxy history.");
    }

    private void exportSelectedHost() {
        String host = (String) hostCombo.getSelectedItem();
        if (host == null || host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No host selected.", "Request Exporter", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ProxyHistoryHostFilter.Result result = ProxyHistoryHostFilter.forHost(api, host);
        if (result.requestResponses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No requests found for host: " + host,
                    "Request Exporter", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Postman Collection");
        chooser.setSelectedFile(new File(host + "-collection.json"));
        int choice = chooser.showSaveDialog(this);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File outFile = chooser.getSelectedFile();
        if (!outFile.getName().toLowerCase().endsWith(".json")) {
            outFile = new File(outFile.getParentFile(), outFile.getName() + ".json");
        }

        boolean includeResponse = includeResponseCheck.isSelected();
        List<HttpRequestResponse> selected = result.requestResponses;
        List<Integer> indices = result.requestIndices;
        File finalOutFile = outFile;

        try {
            PostmanCollectionExporter.export(selected, indices, finalOutFile, includeResponse);
            api.logging().logToOutput("Postman collection for host " + host + " saved to " + finalOutFile.getAbsolutePath());
            statusLabel.setText("Exported " + selected.size() + " request(s) for " + host + " to " + finalOutFile.getName());
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                    "Postman collection saved:\n" + finalOutFile.getAbsolutePath()));
        } catch (Exception ex) {
            api.logging().logToError("Failed to export Postman collection for host " + host + ": " + ex.getMessage());
            statusLabel.setText("Export failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
