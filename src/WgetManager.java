import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.tree.*;
import java.util.ArrayList;

public class WgetManager extends JFrame {
    private JPanel downloadsPanel;
    private ArrayList<WgetGuiApp> instances = new ArrayList<>();
    private File downloadsDir;

    public WgetManager() {
        setTitle("Wget Manager");
        setSize(1200, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Default OS Downloads folder
        downloadsDir = new File(System.getProperty("user.home"), "Downloads");

        // Sidebar = File Explorer (Downloads folder)
        JTree fileTree = new JTree(buildFileTree(downloadsDir));
        JScrollPane treeScroll = new JScrollPane(fileTree);
        treeScroll.setPreferredSize(new Dimension(320, 0)); // زيادة العرض
        add(treeScroll, BorderLayout.WEST);

        // Downloads panel
        downloadsPanel = new JPanel();
        downloadsPanel.setLayout(new BoxLayout(downloadsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(downloadsPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem newDownload = new JMenuItem("New Download");
        newDownload.addActionListener(e -> addDownloadInstance());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(newDownload);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Tree interaction
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());
                if (path == null)
                    return;

                File file = getFileFromPath(path);

                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    // Double click
                    if (file.isDirectory()) {
                        openInExplorer(file);
                    } else {
                        openFile(file);
                    }
                }

                if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 1) {
                    JPopupMenu menu = new JPopupMenu();

                    JMenuItem openItem = new JMenuItem(file.isDirectory() ? "Open Folder" : "Open Containing Folder");
                    openItem.addActionListener(ev -> {
                        if (file.isDirectory()) {
                            openInExplorer(file);
                        } else {
                            // افتح المجلد الذي يحتوي الملف
                            openInExplorer(file.getParentFile());
                        }
                    });
                    menu.add(openItem);

                    if (file.isFile()) {
                        JMenuItem openFileItem = new JMenuItem("Open File");
                        openFileItem.addActionListener(ev -> openFile(file));
                        menu.add(openFileItem);
                    }

                    JMenuItem renameItem = new JMenuItem("Rename");
                    renameItem.addActionListener(ev -> renameFile(file, fileTree));
                    menu.add(renameItem);

                    JMenuItem deleteItem = new JMenuItem("Delete");
                    deleteItem.addActionListener(ev -> deleteFile(file, fileTree));
                    menu.add(deleteItem);

                    menu.show(fileTree, e.getX(), e.getY());
                }
            }
        });
    }

    private DefaultMutableTreeNode buildFileTree(File dir) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    node.add(buildFileTree(f));
                } else {
                    // ملف → يظهر كورقة عادية فقط (بدون children)
                    node.add(new DefaultMutableTreeNode(f, false));
                }
            }
        }
        return node;
    }

    private File getFileFromPath(TreePath path) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        return (File) node.getUserObject();
    }

    private void openInExplorer(File file) {
        try {
            if (file.isDirectory()) {
                Desktop.getDesktop().open(file);
            } else {
                // highlight file in Explorer sidebar
                Runtime.getRuntime().exec(new String[] { "explorer", "/select,", file.getAbsolutePath() });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Failed to open: " + ex.getMessage());
        }
    }

    private void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Cannot open file: " + ex.getMessage());
        }
    }

    private void renameFile(File file, JTree tree) {
        String newName = JOptionPane.showInputDialog(this, "New name:", file.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            File renamed = new File(file.getParentFile(), newName);
            if (file.renameTo(renamed)) {
                refreshTree(tree);
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to rename file.");
            }
        }
    }

    private void deleteFile(File file, JTree tree) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete " + file.getName() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (file.delete()) {
                refreshTree(tree);
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to delete file.");
            }
        }
    }

    private void refreshTree(JTree tree) {
        tree.setModel(new DefaultTreeModel(buildFileTree(downloadsDir)));
    }

    private void addDownloadInstance() {
        WgetGuiApp instance = new WgetGuiApp("Download");
        instance.setOnCloseCallback(() -> {
            downloadsPanel.remove(instance.getMiniPanel());
            downloadsPanel.revalidate();
            downloadsPanel.repaint();
            instances.remove(instance);
        });
        instances.add(instance);
        downloadsPanel.add(instance.getMiniPanel());
        downloadsPanel.revalidate();
        downloadsPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WgetManager manager = new WgetManager();
            manager.setVisible(true);
        });
    }
}
