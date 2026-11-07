package SariSariStore.gui;

import SariSariStore.model.SaleReport;
import SariSariStore.service.ReportService;
import SariSariStore.util.MoneyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SalesReportDialog extends JDialog {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private final ReportService reports;
    private final boolean dailyMode;

    private final JSpinner fromSpinner = createDateSpinner();
    private final JSpinner toSpinner = createDateSpinner();
    private final JLabel summaryLabel = new JLabel(" ");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] {"Sale #", "Date & Time", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // reports are read-only
        }
    };

    public SalesReportDialog(Window owner, ReportService reports, boolean dailyMode) {
        super(owner, dailyMode ? "Daily Sales Report" : "Date-Range Sales Report",
                ModalityType.APPLICATION_MODAL);
        this.reports = reports;
        this.dailyMode = dailyMode;

        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(buildFilterPanel(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSize(560, 420);
        setLocationRelativeTo(owner);

        loadReport(); // show today's sales right away
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        panel.add(new JLabel(dailyMode ? "Date:" : "From:"));
        panel.add(fromSpinner);

        if (!dailyMode) {
            panel.add(new JLabel("To:"));
            panel.add(toSpinner);
        }

        JButton showButton = new JButton("Show");
        showButton.addActionListener(e -> loadReport());
        panel.add(showButton);

        return panel;
    }

    private JPanel buildFooter() {
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        footer.add(summaryLabel, BorderLayout.WEST);
        footer.add(closeButton, BorderLayout.EAST);
        return footer;
    }

    private static JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, DATE_PATTERN));
        return spinner;
    }

    // Reads the spinner as text like "2026-11-08", making sure typed text is applied first.
    private String readDate(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (ParseException ignored) {
            // invalid text: the spinner keeps its last valid date
        }
        return new SimpleDateFormat(DATE_PATTERN).format((Date) spinner.getValue());
    }

    private void loadReport() {
        try {
            String from = readDate(fromSpinner);
            ArrayList<SaleReport> sales;

            if (dailyMode) {
                sales = reports.getDailySalesReport(from);
            } else {
                String to = readDate(toSpinner);
                if (from.compareTo(to) > 0) { // yyyy-MM-dd text sorts like dates
                    throw new IllegalArgumentException("The start date must not be after the end date.");
                }
                sales = reports.getSalesReport(from, to);
            }
            showSales(sales);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Dates", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) { // database errors from the service
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSales(ArrayList<SaleReport> sales) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_PATTERN);
        double grandTotal = 0;

        tableModel.setRowCount(0);
        for (SaleReport sale : sales) {
            tableModel.addRow(new Object[] {
                    sale.getId(),
                    dateTimeFormat.format(java.sql.Timestamp.valueOf(sale.getSoldAt())),
                    MoneyFormatter.format(sale.getTotalAmount())
            });
            grandTotal += sale.getTotalAmount();
        }

        summaryLabel.setText(sales.size() + " sale(s)  |  Total: " + MoneyFormatter.format(grandTotal));
    }
}
