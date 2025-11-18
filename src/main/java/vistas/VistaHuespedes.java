package vistas;

import com.hotelReservations.datos.ConexionDB;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VistaHuespedes extends JFrame {

    private JTable tablaHuespedes;
    private JButton btnNuevoHuesped;
    private JTextField txtFiltro;
    private JPanel panelPrincipal;

    public VistaHuespedes() {
        setTitle("Gestión de Huéspedes");
        setContentPane(panelPrincipal);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        txtFiltro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { cargarDatosHuespedes(txtFiltro.getText()); }
            @Override
            public void removeUpdate(DocumentEvent e) { cargarDatosHuespedes(txtFiltro.getText()); }
            @Override
            public void changedUpdate(DocumentEvent e) { }
        });

        btnNuevoHuesped.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarDialogoNuevoHuesped();
            }
        });

        cargarDatosHuespedes("");
    }

    private void cargarDatosHuespedes(String filtro) {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Nombre Completo");
        modelo.addColumn("Ciudad");
        modelo.addColumn("Dirección");

        Connection con = ConexionDB.getConexion();
        String sql = "SELECT * FROM guests WHERE guest_name ILIKE ? OR guest_city ILIKE ? ORDER BY guest_name";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String busqueda = "%" + filtro + "%";
            ps.setString(1, busqueda);
            ps.setString(2, busqueda);

            ResultSet rs = ps.executeQuery();
            modelo.setRowCount(0);

            while (rs.next()) {
                Object[] fila = new Object[4];
                fila[0] = rs.getInt("guest_number");
                fila[1] = rs.getString("guest_name");
                fila[2] = rs.getString("guest_city");
                fila[3] = rs.getString("guest_address");
                modelo.addRow(fila);
            }

            tablaHuespedes.setModel(modelo);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //registra los datos del huesped
    private void mostrarDialogoNuevoHuesped() {
        JTextField nombreField = new JTextField();
        JTextField ciudadField = new JTextField();
        JTextField direccionField = new JTextField();

        Object[] mensaje = {
                "Nombre Completo:", nombreField,
                "Ciudad:", ciudadField,
                "Dirección:", direccionField
        };

        int opcion = JOptionPane.showConfirmDialog(this, mensaje, "Registrar Nuevo Huésped", JOptionPane.OK_CANCEL_OPTION);

        if (opcion == JOptionPane.OK_OPTION) {
            if (nombreField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            guardarHuespedEnBD(nombreField.getText(), ciudadField.getText(), direccionField.getText());
        }
    }

   //inserta al huesped en la base de datos
    private void guardarHuespedEnBD(String nombre, String ciudad, String direccion) {
        Connection con = ConexionDB.getConexion();
        String sql = "INSERT INTO guests (guest_name, guest_city, guest_address) VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, ciudad.isEmpty() ? "Desconocido" : ciudad);
            ps.setString(3, direccion);

            int afectados = ps.executeUpdate();

            if (afectados > 0) {
                JOptionPane.showMessageDialog(this, "Huésped registrado exitosamente.");
                cargarDatosHuespedes("");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }
}
