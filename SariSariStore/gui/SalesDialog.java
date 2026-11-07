package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.model.SaleItem;
import SariSariStore.service.InventoryService;
import SariSariStore.service.SalesService;
import SariSariStore.util.MoneyFormatter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SalesDialog extends JDialog {
    private final SalesService sales;
    private final Runnable onSaleRecorded;
    private final JComboBox<Product> productBox = new JComboBox<>();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    private final JLabel totalLabel = new JLabel("—");

    public SalesDialog(Window owner, InventoryService inventory, SalesService sales,
                       Runnable onSaleRecorded) {
        super(owner, "Record a Sale", ModalityType.APPLICATION_MODAL);
        this.sales = sales;
        this.onSaleRecorded = onSaleRecorded;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        setResizable(false);
        loadProducts(inventory);
        productBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value instanceof Product) {
                    Product product = (Product) value;
                    setText(product.getName() + " (" + product.getQuantity() + " in stock)");
                }
                return this;
            }
        });
        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        productBox.addActionListener(e -> updateQuantityLimit());
        quantitySpinner.addChangeListener(e -> updateTotal());
        updateQuantityLimit();
        updateTotal();
        pack();
        setMinimumSize(new Dimension(430, getHeight()));
        setLocationRelativeTo(owner);
    }

    private void loadProducts(InventoryService inventory) {
        for (Product product : inventory.getProducts()) {
            if (product.isAvailable() && product.getQuantity() > 0) {
                productBox.addItem(product);
            }
        }
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addRow(form, constraints, 0, "Product:", productBox);
        addRow(form, constraints, 1, "Quantity:", quantitySpinner);
        addRow(form, constraints, 2, "Estimated total:", totalLabel);
        return form;
    }

    private void addRow(JPanel panel, GridBagConstraints constraints, int row,
                        String label, Component field) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        panel.add(new JLabel(label), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(field, constraints);
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton complete = new JButton("Complete Sale");
        complete.addActionListener(e -> completeSale());
        buttons.add(cancel);
        buttons.add(complete);
        return buttons;
    }

    private Product selectedProduct() {
        return (Product) productBox.getSelectedItem();
    }

    private int selectedQuantity() {
        return ((Number) quantitySpinner.getValue()).intValue();
    }

    private void updateQuantityLimit() {
        Product product = selectedProduct();
        int maximum = product == null ? 1 : product.getQuantity();
        SpinnerNumberModel model = (SpinnerNumberModel) quantitySpinner.getModel();
        model.setMaximum(Math.max(1, maximum));
        if (maximum > 0 && selectedQuantity() > maximum) {
            model.setValue(maximum);
        }
        updateTotal();
    }

    private void updateTotal() {
        Product product = selectedProduct();
        totalLabel.setText(product == null ? "—"
                : MoneyFormatter.format(product.getSellPrice() * selectedQuantity()));
    }

    private void completeSale() {
        Product product = selectedProduct();
        if (product == null) {
            showError("There are no available products in stock.");
            return;
        }
        try {
            ArrayList<SaleItem> items = new ArrayList<>();
            items.add(new SaleItem(product.getId(), selectedQuantity()));
            int saleId = sales.recordSale(items);
            JOptionPane.showMessageDialog(this,
                    "Sale #" + saleId + " recorded. Total: " + totalLabel.getText(),
                    "Sale Recorded", JOptionPane.INFORMATION_MESSAGE);
            if (onSaleRecorded != null) {
                onSaleRecorded.run();
            }
            dispose();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (RuntimeException ex) {
            showError("Could not record sale: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Sale Not Recorded",
                JOptionPane.ERROR_MESSAGE);
    }
}