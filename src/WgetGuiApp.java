import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.util.regex.*;

public class WgetGuiApp {
    private long fileSizeBytes = -1;
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
    private JPanel topPanel;
    private JScrollPane logScroll;
    private Process wgetProcess;

    private JPanel miniPanel;
    private JButton showButton;

    private Runnable onCloseCallback; // باش نعلم WgetManager عند الغلق

    public WgetGuiApp(String fileCategory) {
        this.fileCategory = fileCategory;
        buildUI();
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    private void buildUI() {
        frame = new JFrame(fileCategory + " Window");
        frame.setSize(750, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (onCloseCallback != null) {
                    onCloseCallback.run();
                }
            }
        });

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
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        buildMiniPanel();
    }

    private void buildMiniPanel() {
        miniPanel = new JPanel(new BorderLayout(5, 5));
        showButton = createStyledButton("Show UI", e -> {
            if (!frame.isVisible()) {
                frame.setVisible(true);
            }
            frame.setState(JFrame.NORMAL);
            frame.toFront();
            frame.requestFocus();
        });

        progressBar.setPreferredSize(new Dimension(200, 30)); // العرض 200px، الارتفاع 30px
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        progressBar.setMinimumSize(new Dimension(100, 30));
        JPanel progressWrapper = new JPanel(new BorderLayout());
        progressWrapper.add(progressBar, BorderLayout.CENTER);

        // ضبط حجم miniPanel نفسه (ارتفاع 40px مثلاً)
        miniPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        miniPanel.setPreferredSize(new Dimension(600, 40));
        miniPanel.setMinimumSize(new Dimension(300, 40));

        miniPanel.add(progressBar, BorderLayout.CENTER);
        miniPanel.add(showButton, BorderLayout.EAST);
    }

    private JButton createStyledButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        button.setPreferredSize(new Dimension(120, 35));
        button.setBackground(new Color(30, 144, 255));
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

    private String getDefaultSavePath(String url) {
        // المجلد القياسي للتحميلات
        File baseDir = new File(System.getProperty("user.home"), "Downloads");

        // تحديد نوع الملف حسب الامتداد
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        String extension = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex + 1).toLowerCase();
        }

        File targetDir;
        switch (extension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                targetDir = new File(baseDir, "Images");
                break;
            case "mp4":
            case "avi":
            case "mkv":
            case "mov":
                targetDir = new File(baseDir, "Videos");
                break;
            case "zip":
            case "rar":
            case "7z":
            case "tar":
            case "gz":
                targetDir = new File(baseDir, "Compressed");
                break;
            case "exe":
            case "msi":
            case "apk":
                targetDir = new File(baseDir, "Programs");
                break;
            case "pdf":
            case "doc":
            case "docx":
            case "xls":
            case "xlsx":
            case "ppt":
            case "pptx":
            case "txt":
                targetDir = new File(baseDir, "Documents");
                break;
            default:
                targetDir = new File(baseDir, "Others");
                break;
        }

        // تأكد أن المجلد موجود
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // إرجاع المسار الكامل
        return new File(targetDir, fileName).getAbsolutePath();
    }

    private void onDownload(ActionEvent e) {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "⚠ الرجاء إدخال رابط التحميل.");
            return;
        }

        // ضبط المسار الافتراضي تلقائياً
        String savePath = getDefaultSavePath(url);
        saveField.setText(savePath.substring(0, savePath.lastIndexOf(File.separator))); // فقط المجلد
        String fileName = new File(savePath).getName();

        try {
            ProcessBuilder pb = new ProcessBuilder("wget", "-c", "-P", saveField.getText(), "--progress=dot", url);
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

    private boolean validateUrl(String url) {
        try {
            ProcessBuilder pb = new ProcessBuilder("wget", "--spider", "-S", url);
            Process p = pb.start();
            int exitCode = p.waitFor();
            return exitCode == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private void fetchFileSize(String url) {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("wget", "--spider", "-S", url);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.toLowerCase().contains("length:")) {
                            String[] parts = line.split(" ");
                            for (String part : parts) {
                                if (part.matches("\\d+")) {
                                    fileSizeBytes = Long.parseLong(part);
                                    SwingUtilities.invokeLater(() -> {
                                        progressBar.setMaximum(100);
                                        updateProgressFromFile();
                                    });
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logArea.append("Error fetching file size: " + ex.getMessage() + "\n");
            }
        }).start();
    }

    private void updateProgressFromFile() {
        String savePath = saveField.getText();
        String url = urlField.getText();
        File f = new File(savePath, url.substring(url.lastIndexOf("/") + 1));
        if (f.exists() && fileSizeBytes > 0) {
            long downloaded = f.length();
            int percent = (int) ((downloaded * 100) / fileSizeBytes);
            progressBar.setValue(percent);
            progressBar.setString(f.getName() + " - " + percent + "% (" + formatFileSize(fileSizeBytes) + ")");
            if (percent >= 100) {
                JOptionPane.showMessageDialog(frame, "📂 الملف محمل بالكامل!");
            }
        }
    }
    
    private void onRefresh(ActionEvent e) {
        String newUrl = JOptionPane.showInputDialog(frame, "Paste new URL:", urlField.getText());
        if (newUrl != null && !newUrl.trim().isEmpty()) {
            urlField.setText(newUrl.trim());

            // حفظ النص والأيقونة الأصلية
            String originalText = refreshButton.getText();
            Icon originalIcon = refreshButton.getIcon();

            // تغيير الأيقونة للنشاط (loading)
            refreshButton.setText("Refreshing...");
            refreshButton.setIcon(UIManager.getIcon("OptionPane.informationIcon")); // استبدل بأيقونة Sync من عندك
            refreshButton.setEnabled(false);

            // تشغيل التحقق في thread منفصل
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return validateUrl(newUrl.trim());
                }

                @Override
                protected void done() {
                    try {
                        boolean isValid = get();
                        if (isValid) {
                            JOptionPane.showMessageDialog(frame, "✅ الرابط صالح وتم تحديثه بنجاح");
                            fetchFileSize(newUrl.trim());
                        } else {
                            JOptionPane.showMessageDialog(frame, "❌ الرابط غير صالح");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "⚠ خطأ أثناء التحقق: " + ex.getMessage());
                    }

                    // إرجاع حالة الزر
                    refreshButton.setText(originalText);
                    refreshButton.setIcon(originalIcon);
                    refreshButton.setEnabled(true);
                }
            };

            worker.execute();
        }
    }
    

    private void onClear() {
        topPanel.setVisible(false);
        logScroll.setVisible(false);
    }

    private void parseProgress(String line) {
        Pattern pattern = Pattern.compile("(\\d+)%");
        Matcher matcher = pattern.matcher(line);
        String url = urlField.getText();
        String savePath = saveField.getText();
        if (url.isEmpty() || savePath.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter URL and save location.");
            return;
        }
        if (matcher.find()) {
            int progress = Integer.parseInt(matcher.group(1));
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(progress);
                String sizeText = (fileSizeBytes > 0) ? formatFileSize(fileSizeBytes) : "Unknown";
                progressBar.setString(
                        url.substring(url.lastIndexOf("/") + 1) + " - " + progress + "% (" + sizeText + ")");
            });
        }
        if (line.contains("Length:")) {
            String[] parts = line.split(" ");
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    fileSizeBytes = Long.parseLong(part);
                    break;
                }
            }
        }
    }

    private String formatFileSize(long bytes) {
        double sizeInMB = bytes / (1024.0 * 1024.0);
        if (sizeInMB < 1024) {
            return String.format("%.2f MB", sizeInMB);
        } else {
            double sizeInGB = sizeInMB / 1024.0;
            return String.format("%.2f GB", sizeInGB);
        }
    }

    public JPanel getMiniPanel() {
        return miniPanel;
    }

    public String getFileCategory() {
        return fileCategory;
    }
}
