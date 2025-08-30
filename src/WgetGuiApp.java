import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.util.regex.*;

public class WgetGuiApp {
    private String fileCategory;
    private JFrame frame;
    private JPanel mainPanel;
    private JTextField urlField;
    private JTextField saveField;
    private JButton browseButton;
    private JButton downloadButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton refreshButton;
    private JButton clearButton;
    private JButton coffeeButton;
    private JProgressBar progressBar;
    private JTextArea logArea;
    private JLabel sizeLabel;

    private JPanel topPanel;
    private JScrollPane logScroll;
    private Process wgetProcess;

    private JPanel miniPanel;
    private JLabel fileLabel;
    private JButton showButton;

    public WgetGuiApp(String fileCategory) {
        this.fileCategory = fileCategory;
        buildUI();
    }

    private void buildUI() {
        frame = new JFrame(fileCategory + " Window");
        frame.setSize(750, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createTitledBorder(fileCategory + " Download"));

        // ===== Top Panel =====
        topPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.setBackground(new Color(245, 245, 245));

        // URL Panel
        JPanel urlPanel = new JPanel(new BorderLayout(5, 5));
        urlField = new JTextField();
        urlPanel.add(new JLabel("URL: "), BorderLayout.WEST);
        urlPanel.add(urlField, BorderLayout.CENTER);
        refreshButton = createStyledButton("Refresh", e -> onRefresh(e));
        urlPanel.add(refreshButton, BorderLayout.EAST);
        topPanel.add(urlPanel);

        // Save Panel
        JPanel savePanel = new JPanel(new BorderLayout(5, 5));
        saveField = new JTextField();
        savePanel.add(new JLabel("Save to: "), BorderLayout.WEST);
        savePanel.add(saveField, BorderLayout.CENTER);
        browseButton = createStyledButton("Browse", e -> onBrowse(e));
        savePanel.add(browseButton, BorderLayout.EAST);
        topPanel.add(savePanel);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        downloadButton = createStyledButton("Download", e -> onDownload(e));
        pauseButton = createStyledButton("Pause", e -> onPause(e));
        resumeButton = createStyledButton("Resume", e -> onResume(e));
        coffeeButton = createStyledButton("Buy Coffee", e -> onCoffee());
        clearButton = createStyledButton("Clear UI", e -> onClear());

        buttonPanel.add(downloadButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resumeButton);
        buttonPanel.add(coffeeButton);
        buttonPanel.add(clearButton);

        topPanel.add(buttonPanel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ===== Center Log Area =====
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(250, 250, 250));
        logArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        logScroll = new JScrollPane(logArea);
        mainPanel.add(logScroll, BorderLayout.CENTER);

        // ===== Bottom Panel =====
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(0, 30)); // ارتفاع 30px
        progressBar.setStringPainted(true);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        sizeLabel = new JLabel("File Size: Unknown");
        bottomPanel.add(sizeLabel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        buildMiniPanel();
    }

    private void buildMiniPanel() {
        miniPanel = new JPanel(new BorderLayout(5, 5));
        fileLabel = new JLabel("No file yet");
        showButton = createStyledButton("Show UI", e -> {
            if (!frame.isVisible()) {
                frame.setVisible(true);
            }
            frame.setState(JFrame.NORMAL);
            frame.toFront();
            frame.requestFocus();
        });

        JProgressBar miniProgress = new JProgressBar();
        miniProgress.setPreferredSize(new Dimension(0, 30)); // ارتفاع 30px
        miniProgress.setStringPainted(true);
        this.progressBar = miniProgress;

        miniPanel.add(fileLabel, BorderLayout.WEST);
        miniPanel.add(progressBar, BorderLayout.CENTER);
        miniPanel.add(showButton, BorderLayout.EAST);
    }

    private JButton createStyledButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        button.setPreferredSize(new Dimension(120, 35));
        button.setBackground(new Color(30, 144, 255)); // أزرق بسيط
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return button;
    }

    private void onCoffee() {
        try {
            Desktop.getDesktop().browse(new URI("https://buymeacoffee.com/belmelahmeh"));
        } catch (Exception e) {
            logArea.append("Failed to open Coffee page.\n");
        }
    }

    private void onBrowse(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            saveField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onDownload(ActionEvent e) {
        String url = urlField.getText();
        String savePath = saveField.getText();
        if (url.isEmpty() || savePath.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter URL and save location.");
            return;
        }
        fileLabel.setText(url.substring(url.lastIndexOf("/") + 1));

        try {
            ProcessBuilder pb = new ProcessBuilder("wget", "-P", savePath, "--progress=dot", url);
            pb.redirectErrorStream(true);
            wgetProcess = pb.start();
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(wgetProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logArea.append(line + "\n");
                        parseProgress(line);
                    }
                } catch (IOException ex) {
                    logArea.append("Error: " + ex.getMessage() + "\n");
                }
            }).start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error starting wget: " + ex.getMessage());
        }
    }

    private void onPause(ActionEvent e) {
        if (wgetProcess != null) {
            wgetProcess.destroy();
            logArea.append("Download paused.\n");
        }
    }

    private void onResume(ActionEvent e) {
        onDownload(null);
        logArea.append("Download resumed.\n");
    }

    private void onRefresh(ActionEvent e) {
        logArea.setText("");
        progressBar.setValue(0);
        sizeLabel.setText("File Size: Unknown");
    }

    private void onClear() {
        topPanel.setVisible(false);
        logScroll.setVisible(false);
    }

    private void parseProgress(String line) {
        Pattern pattern = Pattern.compile("(\\d+)%");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            int progress = Integer.parseInt(matcher.group(1));
            SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
        }
        if (line.contains("Length:")) {
            String[] parts = line.split(" ");
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    sizeLabel.setText("File Size: " + part + " bytes");
                    break;
                }
            }
        }
    }

    public JPanel getMiniPanel() {
        return miniPanel;
    }

    public String getFileCategory() {
        return fileCategory;
    }
}