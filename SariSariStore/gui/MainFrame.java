package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.service.InventoryService;
import SariSariStore.service.ReportService;
import SariSariStore.service.SalesService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public class MainFrame extends JFrame {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductPanel productPanel;
    private final InventoryService inventoryService;
    private final SalesService salesService;
    private final ReportService reportService;

    public MainFrame() {
        super("Sari-Sari Store");
        inventoryService = new InventoryService();
        salesService = new SalesService();
        reportService = new ReportService();
        productPanel = new ProductPanel(inventoryService);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setJMenuBar(buildMenuBar());
        add(buildToolbar(), BorderLayout.NORTH);
        add(productPanel, BorderLayout.CENTER);
        setSize(760, 520);
        setLocationRelativeTo(null);
    }

    // ---------- UI building ----------

    private JToolBar buildToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton recordSaleButton = new JButton("Record Sale");
        recordSaleButton.addActionListener(e -> openSalesDialog());
        toolbar.add(recordSaleButton);

        JButton restockButton = new JButton("Restock");
        restockButton.addActionListener(e -> openRestockDialog());
        toolbar.add(restockButton);

        JButton dashboardButton = new JButton("Dashboard");
        dashboardButton.addActionListener(e -> openDashboard());
        toolbar.add(dashboardButton);

        return toolbar;
    }

    private JMenuBar buildMenuBar() {
        JMenu reports = new JMenu("Reports");

        // Sales reports
        reports.add(menuItem("Daily Sales Report", () -> openSalesReport(true)));
        reports.add(menuItem("Date-Range Sales Report", () -> openSalesReport(false)));
        reports.addSeparator();

        // Stock reports
        reports.add(menuItem("Low-Stock Report", () ->
                showProductReport("Low-Stock Report (below " + LOW_STOCK_THRESHOLD + ")",
                        () -> reportService.getLowStockProducts(LOW_STOCK_THRESHOLD))));

        reports.add(menuItem("Out-of-Stock Report", () ->
                showProductReport("Out-of-Stock Report", reportService::getOutofStockProducts)));

        reports.add(menuItem("Inventory Summary", () ->
                showProductReport("Inventory Summary", reportService::getInventorySummary)));

        JMenuBar bar = new JMenuBar();
        bar.add(reports);
        return bar;
    }

    private JMenuItem menuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        return item;
    }

    private void openSalesDialog() {
        SalesDialog dialog = new SalesDialog(this, inventoryService, salesService,
                productPanel::refreshProducts);
        dialog.setVisible(true);
    }

    private void openRestockDialog() {
        try {
            RestockDialog dialog = new RestockDialog(this, inventoryService, reportService,
                    productPanel::refreshProducts);
            dialog.setVisible(true);
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void openDashboard() {
        new DashboardDialog(this, reportService, LOW_STOCK_THRESHOLD).setVisible(true);
    }

    private void openSalesReport(boolean daily) {
        new SalesReportDialog(this, reportService, daily).setVisible(true);
    }

    private void showProductReport(String title, Supplier<List<Product>> loader) {
        try {
            ReportDialog.forProducts(this, title, loader.get()).setVisible(true);
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void showError(RuntimeException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
