package SariSariStore;

import SariSariStore.gui.MainFrame;
import SariSariStore.util.DatabaseInitializer;

public class Main {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        javax.swing.SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}