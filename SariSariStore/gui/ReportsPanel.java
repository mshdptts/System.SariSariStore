package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.model.SaleReport;
import SariSariStore.service.ReportService;
import SariSariStore.util.MoneyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ReportsPanel extends JPanel {
    private final ReportService reports;
    private final DefaultTableModel model = new DefaultTableModel();
    private final JTable table = new JTable(model);
    private final DatePickerField from = new DatePickerField();
    private final DatePickerField to = new DatePickerField();
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
        from.setPreferredSize(new Dimension(150, 34));
        to.setPreferredSize(new Dimension(150, 34));
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
            LocalDate start = parseDate(from);
            LocalDate end = parseDate(to);
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
        } catch (RuntimeException ex) {
            showError("Could not load sales report: " + ex.getMessage());
        }
    }

    private LocalDate parseDate(DatePickerField field) {
        return field.getDate();
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

    private static final class DatePickerField extends JPanel {
        private final JTextField text = new JTextField();
        private final JButton calendar = new JButton("\u25BC");
        private LocalDate date = LocalDate.now();
        private YearMonth month = YearMonth.from(date);
        private final JPopupMenu popup = new JPopupMenu();

        private DatePickerField() {
            super(new BorderLayout(4, 0));
            setOpaque(false);
            text.setEditable(false);
            text.setText(date.toString());
            text.setToolTipText("Choose a date");
            calendar.setToolTipText("Open calendar");
            calendar.setMargin(new Insets(0, 8, 0, 8));
            calendar.addActionListener(e -> showCalendar());
            add(text, BorderLayout.CENTER);
            add(calendar, BorderLayout.EAST);
        }

        private LocalDate getDate() {
            return date;
        }

        private void showCalendar() {
            popup.removeAll();
            JPanel panel = new JPanel(new BorderLayout(6, 6));
            panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JPanel heading = new JPanel(new BorderLayout());
            JButton previous = new JButton("<");
            JButton next = new JButton(">");
            JLabel title = new JLabel(month.format(DateTimeFormatter.ofPattern("MMMM uuuu")),
                    SwingConstants.CENTER);
            previous.addActionListener(e -> {
                month = month.minusMonths(1);
                showCalendar();
            });
            next.addActionListener(e -> {
                month = month.plusMonths(1);
                showCalendar();
            });
            heading.add(previous, BorderLayout.WEST);
            heading.add(title, BorderLayout.CENTER);
            heading.add(next, BorderLayout.EAST);
            panel.add(heading, BorderLayout.NORTH);

            JPanel days = new JPanel(new GridLayout(0, 7, 2, 2));
            String[] weekdayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (String weekday : weekdayNames) {
                JLabel label = new JLabel(weekday, SwingConstants.CENTER);
                label.setForeground(Theme.MUTED);
                days.add(label);
            }
            LocalDate first = month.atDay(1);
            int offset = first.getDayOfWeek().getValue() - 1;
            for (int i = 0; i < offset; i++) {
                days.add(new JLabel());
            }
            for (int day = 1; day <= month.lengthOfMonth(); day++) {
                LocalDate selected = month.atDay(day);
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setMargin(new Insets(3, 3, 3, 3));
                if (selected.equals(date)) {
                    dayButton.setBackground(Theme.PRIMARY);
                    dayButton.setForeground(Color.WHITE);
                }
                dayButton.addActionListener(e -> {
                    date = selected;
                    text.setText(date.toString());
                    popup.setVisible(false);
                });
                days.add(dayButton);
            }
            panel.add(days, BorderLayout.CENTER);
            popup.add(panel);
            popup.show(calendar, 0, calendar.getHeight() + 2);
        }
    }
}
