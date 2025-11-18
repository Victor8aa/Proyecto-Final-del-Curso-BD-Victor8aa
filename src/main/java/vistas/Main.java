package vistas;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                VistaLogin ventanaLogin = new VistaLogin();
                ventanaLogin.setVisible(true);
            }
        });
    }
}
