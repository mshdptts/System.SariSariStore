package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.util.MoneyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** a simple read-only table dialog. One class serves every report. */
public class ReportDialog extends JDialog {

    public ReportDialog(Window owner, String title, String[] columns, List<Object[]> rows) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(new JScrollPane(buildTable(columns, rows)), BorderLayout.CENTER);
        add(buildFooter(rows.size()), BorderLayout.SOUTH);

        setSize(640, 400);
        setLocationRelativeTo(owner);
    }

    /** shortcut for the three stock reports, which all return a list of products. */
    public static ReportDialog forProducts(Window owner, String title, List<Product> products) {
        String[] columns = {"Name", "Buy Price", "Sell Price", "Stock", "Available"};

        List<Object[]> rows = new java.util.ArrayList<>();
        for (Product p : products) {
            rows.add(new Object[] {
                    p.getName(),
                    MoneyFormatter.format(p.getBuyPrice()),
                    MoneyFormatter.format(p.getSellPrice()),
                    p.getQuantity(),
                    p.isAvailable() ? "Yes" : "No"
            });
        }
        return new ReportDialog(owner, title, columns, rows);
    }

    private JTable buildTable(String[] columns, List<Object[]> rows) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // reports are read-only
            }
        };
        for (Object[] row : rows) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true); // click a header to sort
        return table;
    }

    private JPanel buildFooter(int rowCount) {
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        footer.add(new JLabel(rowCount + " item(s)"), BorderLayout.WEST);
        footer.add(closeButton, BorderLayout.EAST);
        return footer;
    }
}
