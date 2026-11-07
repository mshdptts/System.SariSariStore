package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.service.InventoryService;
import SariSariStore.service.ReportService;

import javax.swing.*;
import java.awt.*;

/** Dialog for adding stock to an existing product. */
public class RestockDialog extends JDialog {

    private final InventoryService inventory;
    private final Runnable onRestocked;

    private final JComboBox<Product> productBox = new JComboBox<>();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));

    public RestockDialog(Window owner, InventoryService inventory, ReportService reports, Runnable onRestocked) {
        super(owner, "Restock Product", ModalityType.APPLICATION_MODAL);
        this.inventory = inventory;
        this.onRestocked = onRestocked;

        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        loadProducts(reports);
        showProductNamesOnly();

        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(380, getHeight()));
        setLocationRelativeTo(owner);
    }


    private void loadProducts(ReportService reports) {
        for (Product p : reports.getInventorySummary()) {
            productBox.addItem(p);
        }
    }

    private void showProductNamesOnly() {
        productBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Product) {
                    Product p = (Product) value;
                    setText(p.getName() + " (stock: " + p.getQuantity() + ")");
                }
                return this;
            }
        });
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        form.add(new JLabel("Product:"));
        form.add(productBox);
        form.add(new JLabel("Quantity to add:"));
        form.add(quantitySpinner);

        return form;
    }

    private JPanel buildButtonPanel() {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JButton restockButton = new JButton("Restock");
        restockButton.addActionListener(e -> restock());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(restockButton);
        return buttons;
    }

    private void restock() {
        try {
            Product selected = (Product) productBox.getSelectedItem();
            if (selected == null) {
                throw new IllegalArgumentException("There are no products to restock.");
            }

            int amount = ((Number) quantitySpinner.getValue()).intValue();
            Product updated = inventory.restockProduct(selected.getName(), amount);
            if (updated == null) {
                throw new IllegalArgumentException("Product not found: " + selected.getName());
            }

            JOptionPane.showMessageDialog(this,
                    updated.getName() + " restocked. New stock: " + updated.getQuantity());

            if (onRestocked != null) {
                onRestocked.run();
            }
            dispose();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Restock Failed", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) { // database errors from the service
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
