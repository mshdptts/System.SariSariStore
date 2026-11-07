package SariSariStore;

import SariSariStore.gui.MainFrame;
import SariSariStore.gui.Theme;
import SariSariStore.util.DatabaseInitializer;

public class Main {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        Theme.install();
        javax.swing.SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}