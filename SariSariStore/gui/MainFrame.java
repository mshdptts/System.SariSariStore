package SariSariStore.gui;

import SariSariStore.service.InventoryService;
import SariSariStore.service.ReportService;
import SariSariStore.service.SalesService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {
    public static final String DASHBOARD = "Dashboard";
    public static final String PRODUCTS = "Products";
    public static final String SALES = "Sales";
    public static final String REPORTS = "Reports";

    private final InventoryService inventory = new InventoryService();
    private final SalesService sales = new SalesService();
    private final ReportService reports = new ReportService();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final JLabel clock = new JLabel();
    private final JLabel status = new JLabel();
    private final DashboardPanel dashboard;
    private final ProductPanel products;
    private final SalesPanel salesPanel;
    private final ReportsPanel reportsPanel;

    public MainFrame() {
        super("Sari-Sari Store");
        dashboard = new DashboardPanel(inventory, reports, () -> switchTo(PRODUCTS),
                () -> switchTo(SALES), () -> switchTo(REPORTS));
        products = new ProductPanel(inventory, this::refreshAll);
        salesPanel = new SalesPanel(inventory, sales, this::refreshAll);
        reportsPanel = new ReportsPanel(reports);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(buildTop(), BorderLayout.NORTH);
        cards.setBackground(Theme.PAGE);
        cards.add(dashboard, DASHBOARD);
        cards.add(products, PRODUCTS);
        cards.add(salesPanel, SALES);
        cards.add(reportsPanel, REPORTS);
        add(cards, BorderLayout.CENTER);
        add(buildStatus(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        bindShortcuts();
        refreshAll();
        switchTo(DASHBOARD);
    }

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.NAV);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(12, 24, 10, 24));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("Sari-Sari Store");
        name.setForeground(Color.WHITE);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 18f));
        JLabel subtitle = new JLabel("MANAGEMENT SYSTEM");
        subtitle.setForeground(new Color(190, 220, 207));
        brand.add(name);
        brand.add(subtitle);
        header.add(brand, BorderLayout.WEST);

        JPanel account = new JPanel();
        account.setOpaque(false);
        account.setLayout(new BoxLayout(account, BoxLayout.Y_AXIS));
        clock.setForeground(Color.WHITE);
        JLabel admin = new JLabel("Store Admin");
        admin.setForeground(new Color(190, 220, 207));
        account.add(clock);
        account.add(admin);
        header.add(account, BorderLayout.EAST);
        top.add(header, BorderLayout.NORTH);

        JPanel navigation = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        navigation.setBackground(Theme.NAV);
        addNavigationButton(navigation, DASHBOARD);
        addNavigationButton(navigation, PRODUCTS);
        addNavigationButton(navigation, SALES);
        addNavigationButton(navigation, REPORTS);
        top.add(navigation, BorderLayout.SOUTH);
        return top;
    }

    private void addNavigationButton(JPanel navigation, String name) {
        JButton button = Theme.secondary(name);
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.addActionListener(e -> switchTo(name));
        navigation.add(button);
    }

    private JPanel buildStatus() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 24, 5, 24));
        status.setForeground(Theme.MUTED);
        panel.add(status, BorderLayout.WEST);
        return panel;
    }

    private void switchTo(String name) {
        cardLayout.show(cards, name);
        if (SALES.equals(name)) {
            salesPanel.refresh();
        } else if (REPORTS.equals(name)) {
            reportsPanel.refresh();
        }
    }

    public void refreshAll() {
        dashboard.refresh();
        products.refreshProducts();
        salesPanel.refresh();
        reportsPanel.refresh();
        updateStatus();
    }

    private void updateStatus() {
        status.setText("Products: " + inventory.getTotalProductCount()
                + "  |  Stock units: " + inventory.getTotalUnitsInStock());
        clock.setText(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMM d, yyyy h:mm:ss a")));
    }

    private void bindShortcuts() {
        InputMap input = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getRootPane().getActionMap();
        String[] pages = {DASHBOARD, PRODUCTS, SALES, REPORTS};
        for (int i = 0; i < pages.length; i++) {
            final String page = pages[i];
            input.put(KeyStroke.getKeyStroke("ctrl " + (i + 1)), "page" + i);
            actions.put("page" + i, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    switchTo(page);
                }
            });
        }
        new Timer(1000, e -> updateStatus()).start();
    }
}
