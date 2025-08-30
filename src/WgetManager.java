import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class WgetManager extends JFrame {
    private JPanel downloadsPanel;
    private ArrayList<WgetGuiApp> instances = new ArrayList<>();

    public WgetManager() {
        setTitle("Wget Manager");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

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

        // Sidebar for categories
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(5, 1, 5, 5));
        sidebar.add(createCategoryButton("All"));
        sidebar.add(createCategoryButton("Compressed"));
        sidebar.add(createCategoryButton("Doc"));
        sidebar.add(createCategoryButton("Video"));
        sidebar.add(createCategoryButton("Programs"));
        add(sidebar, BorderLayout.WEST);

        // Downloads panel
        downloadsPanel = new JPanel();
        downloadsPanel.setLayout(new BoxLayout(downloadsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(downloadsPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JButton createCategoryButton(String name) {
        JButton btn = new JButton(name);
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setBackground(new Color(30, 144, 255));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.addActionListener(e -> filterByCategory(name));
        return btn;
    }

    private void filterByCategory(String category) {
        downloadsPanel.removeAll();
        for (WgetGuiApp instance : instances) {
            if (category.equals("All") || instance.getFileCategory().equals(category)) {
                downloadsPanel.add(instance.getMiniPanel());
            }
        }
        downloadsPanel.revalidate();
        downloadsPanel.repaint();
    }

    private void addDownloadInstance() {
        WgetGuiApp instance = new WgetGuiApp("Download");
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
