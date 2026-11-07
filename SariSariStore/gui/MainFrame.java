package SariSariStore.gui;

import SariSariStore.service.InventoryService;
import SariSariStore.service.SalesService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final ProductPanel productPanel;
    private final InventoryService inventoryService;
    private final SalesService salesService;

    public MainFrame() {
        super("Sari-Sari Store");
        inventoryService = new InventoryService();
        salesService = new SalesService();
        productPanel = new ProductPanel(inventoryService);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(buildToolbar(), BorderLayout.NORTH);
        add(productPanel, BorderLayout.CENTER);
        setSize(760, 520);
        setLocationRelativeTo(null);
    }

    private JToolBar buildToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton recordSaleButton = new JButton("Record Sale");
        recordSaleButton.addActionListener(e -> openSalesDialog());
        toolbar.add(recordSaleButton);

        return toolbar;
    }

    private void openSalesDialog() {
        SalesDialog dialog = new SalesDialog(this, inventoryService, salesService,
                productPanel::refreshProducts);
        dialog.setVisible(true);
    }
}
