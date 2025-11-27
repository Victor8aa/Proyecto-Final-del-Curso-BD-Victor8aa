package vistas;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("No se pudo cargar el diseño. Se usará el por defecto.");
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                VistaLogin ventanaLogin = new VistaLogin();
                ventanaLogin.pack();
                ventanaLogin.setLocationRelativeTo(null);
                ventanaLogin.setVisible(true);
            }
        });
    }
}
