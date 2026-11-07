package SariSariStore.gui;

import SariSariStore.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public final class ReportDialog extends JDialog {
    private ReportDialog(Window owner, String title, List<Product> products) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Price", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Product product : products) {
            model.addRow(new Object[]{product.getId(), product.getName(),
                    product.getSellPrice(), product.getQuantity()});
        }
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(close);
        add(footer, BorderLayout.SOUTH);
        setSize(560, 420);
        setLocationRelativeTo(owner);
    }

    public static ReportDialog forProducts(Window owner, String title,
                                           List<Product> products) {
        return new ReportDialog(owner, title, products);
    }
}
