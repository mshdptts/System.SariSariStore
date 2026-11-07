package SariSariStore.gui;

import SariSariStore.model.SaleRecord;
import SariSariStore.service.ReportService;
import SariSariStore.util.MoneyFormatter;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class DashboardDialog extends JDialog {

    private final ReportService reports;
    private final int lowStockThreshold;

    private final JLabel todaySalesValue = new JLabel("—");
    private final JLabel salesCountValue = new JLabel("—");
    private final JLabel productsValue = new JLabel("—");
    private final JLabel lowStockValue = new JLabel("—");
    private final JLabel outOfStockValue = new JLabel("—");

    public DashboardDialog(Window owner, ReportService reports, int lowStockThreshold) {
        super(owner, "Dashboard", ModalityType.APPLICATION_MODAL);
        this.reports = reports;
        this.lowStockThreshold = lowStockThreshold;

        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(buildCardsPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        setSize(560, 280);
        setLocationRelativeTo(owner);

        refresh();
    }

    private JPanel buildCardsPanel() {
        JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        grid.add(buildCard("Today's Sales", todaySalesValue));
        grid.add(buildCard("Sales Made Today", salesCountValue));
        grid.add(buildCard("Total Products", productsValue));
        grid.add(buildCard("Running Low (below " + lowStockThreshold + ")", lowStockValue));
        grid.add(buildCard("Out of Stock", outOfStockValue));

        return grid;
    }

    private JPanel buildCard(String title, JLabel valueLabel) {
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 24f));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);

        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildButtonPanel() {
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(closeButton);
        return buttons;
    }

    private void refresh() {
        loadSalesCards();
        loadStockCards();
    }

    private void loadSalesCards() {
        try {
            String today = LocalDate.now().toString(); // yyyy-MM-dd
            ArrayList<SaleRecord> todaysSales = reports.getDailySalesReport(today);

            double total = 0;
            for (SaleRecord sale : todaysSales) {
                total += sale.getTotal();
            }
            todaySalesValue.setText(MoneyFormatter.format(total));
            salesCountValue.setText(String.valueOf(todaysSales.size()));
        } catch (RuntimeException ex) {
            todaySalesValue.setText("—");
            salesCountValue.setText("—");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could Not Load Sales", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStockCards() {
        try {
            int outOfStock = reports.getOutofStockProducts().size();
            // "below N" also includes products at 0, so subtract them to avoid counting twice.
            int runningLow = reports.getLowStockProducts(lowStockThreshold).size() - outOfStock;

            productsValue.setText(String.valueOf(reports.getInventorySummary().size()));
            lowStockValue.setText(String.valueOf(runningLow));
            outOfStockValue.setText(String.valueOf(outOfStock));
        } catch (RuntimeException ex) {
            productsValue.setText("—");
            lowStockValue.setText("—");
            outOfStockValue.setText("—");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could Not Load Stock", JOptionPane.ERROR_MESSAGE);
        }
    }
}
