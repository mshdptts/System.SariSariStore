package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.model.SaleReport;
import SariSariStore.service.ReportService;
import SariSariStore.util.MoneyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class ReportsPanel extends JPanel {
    private final ReportService reports;
    private final DefaultTableModel model = new DefaultTableModel();
    private final JTable table = new JTable(model);
    private final JTextField from = new JTextField(LocalDate.now().toString());
    private final JTextField to = new JTextField(LocalDate.now().toString());
    private final JLabel message = Theme.muted("");

    public ReportsPanel(ReportService reports) {
        this.reports = reports;
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));
        build();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(Theme.title("Reports"));
        heading.add(Theme.muted("Analyze sales and inventory"));
        header.add(heading, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton sales = Theme.primary("Sales");
        sales.addActionListener(e -> showSales());
        JButton inventory = Theme.secondary("Inventory");
        inventory.addActionListener(e -> showInventory());
        JButton low = Theme.secondary("Low Stock");
        low.addActionListener(e -> showProducts(reports.getLowStockProducts(5)));
        JButton out = Theme.secondary("Out of Stock");
        out.addActionListener(e -> showProducts(reports.getOutofStockProducts()));
        actions.add(sales);
        actions.add(inventory);
        actions.add(low);
        actions.add(out);
        header.add(actions, BorderLayout.EAST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        from.setPreferredSize(new Dimension(120, 32));
        to.setPreferredSize(new Dimension(120, 32));
        filters.add(new JLabel("From"));
        filters.add(from);
        filters.add(new JLabel("To"));
        filters.add(to);
        JButton apply = Theme.primary("Apply");
        apply.addActionListener(e -> showSales());
        filters.add(apply);

        table.setRowHeight(36);
        table.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel card = Theme.card(new BorderLayout(8, 8));
        card.add(filters, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(message, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    private void showSales() {
        try {
            LocalDate start = LocalDate.parse(from.getText().trim());
            LocalDate end = LocalDate.parse(to.getText().trim());
            if (start.isAfter(end)) {
                showError("The start date cannot be after the end date.");
                return;
            }
            ArrayList<SaleReport> sales = reports.getSalesReport(start.toString(), end.toString());
            setColumns("Receipt", "Date and Time", "Total");
            for (SaleReport sale : sales) {
                model.addRow(new Object[]{"#" + sale.getId(), sale.getSoldAt(),
                        MoneyFormatter.format(sale.getTotalAmount())});
            }
            message.setForeground(Theme.MUTED);
            message.setText(sales.isEmpty() ? "No sales found for this date range." : "");
        } catch (DateTimeParseException ex) {
            showError("Use dates in YYYY-MM-DD format.");
        } catch (RuntimeException ex) {
            showError("Could not load sales report: " + ex.getMessage());
        }
    }

    private void showInventory() {
        try {
            ArrayList<Product> products = reports.getInventorySummary();
            setColumns("ID", "Name", "Price", "Stock", "Status");
            for (Product product : products) {
                model.addRow(new Object[]{product.getId(), product.getName(),
                        MoneyFormatter.format(product.getSellPrice()), product.getQuantity(),
                        product.getQuantity() == 0 ? "Out of stock"
                                : product.getQuantity() < 5 ? "Low" : "In stock"});
            }
            message.setForeground(Theme.MUTED);
            message.setText(products.isEmpty() ? "No products found." : "");
        } catch (RuntimeException ex) {
            showError("Could not load inventory report: " + ex.getMessage());
        }
    }

    private void showProducts(ArrayList<Product> products) {
        setColumns("ID", "Name", "Price", "Stock");
        for (Product product : products) {
            model.addRow(new Object[]{product.getId(), product.getName(),
                    MoneyFormatter.format(product.getSellPrice()), product.getQuantity()});
        }
        message.setForeground(Theme.MUTED);
        message.setText(products.isEmpty() ? "No products found." : "");
    }

    private void setColumns(Object... columns) {
        model.setColumnIdentifiers(columns);
        model.setRowCount(0);
    }

    private void showError(String text) {
        model.setRowCount(0);
        message.setForeground(Theme.DANGER);
        message.setText(text);
    }

    public void refresh() {
        showSales();
    }
}
