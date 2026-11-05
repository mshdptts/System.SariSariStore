package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.service.InventoryService;
import SariSariStore.service.SalesService;
import SariSariStore.util.MoneyFormatter;

import javax.swing.*;
import java.awt.*;

/** dialog for selecting a product and recording a sale. */
public class SalesDialog extends JDialog {

    private final SalesService sales;
    private final Runnable onSaleRecorded;

    private final JComboBox<Product> productBox = new JComboBox<>();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
    private final JLabel totalLabel = new JLabel("—");

    public SalesDialog(Window owner, InventoryService inventory, SalesService sales, Runnable onSaleRecorded) {
        super(owner, "Record a Sale", ModalityType.APPLICATION_MODAL);
        this.sales = sales;
        this.onSaleRecorded = onSaleRecorded;

        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        loadAvailableProducts(inventory);

        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        productBox.addActionListener(e -> updateTotal());
        quantitySpinner.addChangeListener(e -> updateTotal());

        updateTotal();
        pack();
        setMinimumSize(new Dimension(380, getHeight()));
        setLocationRelativeTo(owner);
    }

    // UI building

    private void loadAvailableProducts(InventoryService inventory) {
        for (Product p : inventory.getProducts()) {
            if (p.isAvailable() && p.getQuantity() > 0) {
                productBox.addItem(p);
            }
        }
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        form.add(new JLabel("Product:"));
        form.add(productBox);
        form.add(new JLabel("Quantity:"));
        form.add(quantitySpinner);
        form.add(new JLabel("Estimated total:"));
        form.add(totalLabel);

        return form;
    }

    private JPanel buildButtonPanel() {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JButton completeButton = new JButton("Complete Sale");
        completeButton.addActionListener(e -> completeSale());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(completeButton);
        return buttons;
    }

    // behaior

    private int selectedQuantity() {
        return ((Number) quantitySpinner.getValue()).intValue();
    }

    private void updateTotal() {
        Product product = (Product) productBox.getSelectedItem();
        if (product == null) {
            totalLabel.setText("—");
            return;
        }
        totalLabel.setText(MoneyFormatter.format(product.getSellPrice() * selectedQuantity()));
    }

    private void completeSale() {
        try {
            Product product = (Product) productBox.getSelectedItem();
            if (product == null) {
                throw new IllegalArgumentException("There are no products in stock.");
            }

            SalesService.Sale sale = sales.recordSale(product, selectedQuantity());
            JOptionPane.showMessageDialog(this, "Sale recorded. Total: " + MoneyFormatter.format(sale.getTotal()));

            if (onSaleRecorded != null) {
                onSaleRecorded.run();
            }
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sale Not Recorded", JOptionPane.ERROR_MESSAGE);
        }
    }
}
