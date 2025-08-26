import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.regex.*;

public class WgetGuiApp extends JFrame {
    private JTextField urlField;
    private JTextField saveField;
    private JButton browseButton;
    private JButton downloadButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton refreshButton;
    private JProgressBar progressBar;
    private JTextArea logArea;
    private JLabel sizeLabel;

    private Process currentProcess;
    private boolean paused = false;
    private long totalSize = -1;

    private long lastBytes = 0;
    private long lastTime = 0;

    private final Font defaultFont = new Font("Arial", Font.PLAIN, 14);
    private final Dimension buttonSize = new Dimension(150, 22);

    // --- custom JTextField with fixed height ---
    static class FixedHeightTextField extends JTextField {
        private final int fixedHeight;

        public FixedHeightTextField(int columns, int height) {
            super(columns);
            this.fixedHeight = height;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = fixedHeight;
            return d;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            d.height = fixedHeight;
            return d;
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension d = super.getMaximumSize();
            d.height = fixedHeight;
            return d;
        }
    }

    public WgetGuiApp() {
        setTitle("Wget GUI App");
        setSize(650, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Panel for inputs
        JPanel inputPanel = new JPanel(new GridLayout(3, 1));

        // URL field
        JPanel urlPanel = new JPanel(new BorderLayout());
        JLabel urlLabel = new JLabel("Download URL: ");
        urlLabel.setFont(defaultFont);
        urlPanel.add(urlLabel, BorderLayout.WEST);

        urlField = new FixedHeightTextField(30, 20);
        urlField.setFont(defaultFont);
        urlPanel.add(urlField, BorderLayout.CENTER);

        refreshButton = new JButton("Refresh Link");
        refreshButton.setFont(defaultFont);
        refreshButton.setPreferredSize(buttonSize);
        urlPanel.add(refreshButton, BorderLayout.EAST);
        inputPanel.add(urlPanel);

        // Save path
        JPanel savePanel = new JPanel(new BorderLayout());
        JLabel saveLabel = new JLabel("Save As: ");
        saveLabel.setFont(defaultFont);
        savePanel.add(saveLabel, BorderLayout.WEST);

        saveField = new FixedHeightTextField(30, 20);
        saveField.setFont(defaultFont);
        savePanel.add(saveField, BorderLayout.CENTER);

        browseButton = new JButton("Browse...");
        browseButton.setFont(defaultFont);
        browseButton.setPreferredSize(buttonSize);
        savePanel.add(browseButton, BorderLayout.EAST);
        inputPanel.add(savePanel);

        // Buttons row
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        downloadButton = createButton("Start Download");
        pauseButton = createButton("Pause");
        resumeButton = createButton("Resume");

        buttonsPanel.add(downloadButton);
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(resumeButton);
        inputPanel.add(buttonsPanel);

        add(inputPanel, BorderLayout.NORTH);

        // Progress bar
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setFont(defaultFont);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(0, 22));
        progressPanel.add(progressBar, BorderLayout.CENTER);

        sizeLabel = new JLabel("File size: Unknown");
        sizeLabel.setFont(defaultFont);
        progressPanel.add(sizeLabel, BorderLayout.SOUTH);

        add(progressPanel, BorderLayout.SOUTH);

        // Log area
        logArea = new JTextArea();
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // Actions
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                saveField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        downloadButton.addActionListener(this::handleStartDownload);
        pauseButton.addActionListener(this::handlePause);
        resumeButton.addActionListener(this::handleResume);
        refreshButton.addActionListener(this::handleRefreshLink);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(defaultFont);
        button.setPreferredSize(buttonSize);
        return button;
    }

    private void handleStartDownload(ActionEvent e) {
        String url = urlField.getText().trim();
        String savePath = saveField.getText().trim();
        if (url.isEmpty() || savePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter URL and save path.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        startDownload(url, savePath, false);
    }

    private void startDownload(String url, String savePath, boolean resume) {
        new Thread(() -> {
            try {
                totalSize = -1;
                lastBytes = 0;
                lastTime = System.currentTimeMillis();

                String command = resume
                        ? "wget -c -O \"" + savePath + "\" \"" + url + "\""
                        : "wget -O \"" + savePath + "\" \"" + url + "\"";

                logArea.append("Running: " + command + "\n");

                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
                pb.redirectErrorStream(true);
                currentProcess = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    logArea.append(line + "\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());

                    // extract total size
                    if (line.contains("Length:")) {
                        Matcher m = Pattern.compile("Length: (\\d+)").matcher(line);
                        if (m.find()) {
                            totalSize = Long.parseLong(m.group(1));
                            sizeLabel.setText("File size: " + formatSize(totalSize));
                        }
                    }

                    // extract percent + speed
                    if (line.contains("%")) {
                        String percent = line.replaceAll(".*?(\\d+)%.*", "$1");
                        try {
                            int p = Integer.parseInt(percent);
                            progressBar.setValue(p);

                            if (totalSize > 0) {
                                long downloaded = (p * totalSize) / 100;
                                long now = System.currentTimeMillis();
                                long deltaTime = now - lastTime;
                                long deltaBytes = downloaded - lastBytes;
                                String speedStr = "";
                                if (deltaTime > 0 && deltaBytes > 0) {
                                    double speed = (deltaBytes * 1000.0) / deltaTime;
                                    speedStr = " - " + formatSpeed(speed);
                                }
                                lastTime = now;
                                lastBytes = downloaded;

                                progressBar.setString(p + "% (" +
                                        formatSize(downloaded) + " / " +
                                        formatSize(totalSize) + ")" + speedStr);
                            } else {
                                progressBar.setString(p + "%");
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    if (paused)
                        break;
                }

                int exitCode = currentProcess.waitFor();
                if (!paused) {
                    if (exitCode == 0) {
                        JOptionPane.showMessageDialog(this, "Download completed!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        progressBar.setValue(100);
                        if (totalSize > 0)
                            progressBar
                                    .setString("100% (" + formatSize(totalSize) + " / " + formatSize(totalSize) + ")");
                    } else {
                        JOptionPane.showMessageDialog(this, "Download failed. Exit code: " + exitCode, "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Exception",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }).start();
    }

    private void handlePause(ActionEvent e) {
        if (currentProcess != null) {
            paused = true;
            currentProcess.destroy();
            logArea.append("Download paused.\n");
        }
    }

    private void handleResume(ActionEvent e) {
        String url = urlField.getText().trim();
        String savePath = saveField.getText().trim();
        if (url.isEmpty() || savePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Missing URL or save path.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        paused = false;
        startDownload(url, savePath, true);
    }

    private void handleRefreshLink(ActionEvent e) {
        String newUrl = JOptionPane.showInputDialog(this, "Enter new download URL:", urlField.getText());
        if (newUrl != null && !newUrl.trim().isEmpty()) {
            urlField.setText(newUrl.trim());
            logArea.append("Download link updated.\n");
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private String formatSpeed(double bytesPerSec) {
        if (bytesPerSec < 1024)
            return String.format("%.1f B/s", bytesPerSec);
        int exp = (int) (Math.log(bytesPerSec) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB/s", bytesPerSec / Math.pow(1024, exp), pre);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WgetGuiApp app = new WgetGuiApp();
            app.setVisible(true);
        });
    }
}
