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

public class VistaHoteles extends JFrame {

    private JPanel panelPrincipal;
    private JTable tablaHoteles;
    private JButton btnNuevo;
    private JButton btnModificar;
    private JButton btnEliminar;
    private JTextField txtFiltro;
    private JComboBox comboFiltroPais;

    // Constructor
    public VistaHoteles() {
        setTitle("Gestión de Hoteles");
        setContentPane(panelPrincipal);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(itemSalir);

        JMenu menuReservas = new JMenu("Reservaciones");

        JMenuItem itemVerReservas = new JMenuItem("Ver Historial Completo");
        itemVerReservas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VistaReservaciones ventanaReservas = new VistaReservaciones();
                ventanaReservas.setVisible(true);
            }
        });

        JMenuItem itemNuevaReserva = new JMenuItem("Nueva Reserva");
        itemNuevaReserva.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Abrimos el diálogo directamente.
                // Como ya le pusimos el ComboBox de hoteles, ¡funciona perfecto desde aquí!
                DialogoReserva dialogo = new DialogoReserva(VistaHoteles.this);
                dialogo.setVisible(true);
            }
        });

        JMenu menuHuespedes = new JMenu("Huéspedes");
        JMenuItem itemVerHuespedes = new JMenuItem("Gestionar Huéspedes");
        itemVerHuespedes.addActionListener(e -> {
            new VistaHuespedes().setVisible(true);
        });

        menuReservas.add(itemVerReservas);
        menuReservas.addSeparator();
        menuReservas.add(itemNuevaReserva);

        menuHuespedes.add(itemVerHuespedes);

        menuBar.add(menuArchivo);
        menuBar.add(menuReservas);
        menuBar.add(menuHuespedes);

        setJMenuBar(menuBar);

        itemVerReservas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Abrimos la ventana de la tabla de reservaciones
                VistaReservaciones ventanaReservas = new VistaReservaciones();
                ventanaReservas.setVisible(true);
            }
        });


        btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleseleccionada = tablaHoteles.getSelectedRow();

                if (filaSeleseleccionada == -1) {
                    JOptionPane.showMessageDialog(
                            VistaHoteles.this,
                            "Debe seleccionar un hotel de la lista.",
                            "Advertencia",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int idHotel = (int) tablaHoteles.getValueAt(filaSeleseleccionada, 0);

                int confirmacion = JOptionPane.showConfirmDialog(
                        VistaHoteles.this,
                        "¿Está seguro de que desea eliminar el hotel con ID " + idHotel + "?",
                        "Confirmar Eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (confirmacion == JOptionPane.YES_OPTION) {

                    Connection con = null;
                    PreparedStatement ps = null;
                    String sql = "DELETE FROM hotels WHERE hotel_id = ?";

                    try {
                        con = ConexionDB.getConexion();
                        ps = con.prepareStatement(sql);
                        ps.setInt(1, idHotel);

                        int filasAfectadas = ps.executeUpdate();

                        if (filasAfectadas > 0) {
                            JOptionPane.showMessageDialog(VistaHoteles.this, "Hotel eliminado exitosamente.");
                            cargarDatosHoteles("");
                        } else {
                            JOptionPane.showMessageDialog(VistaHoteles.this, "No se pudo eliminar el hotel.", "Error", JOptionPane.ERROR_MESSAGE);
                        }

                    } catch (SQLException ex) {
                        if (ex.getSQLState().equals("23503")) {
                            JOptionPane.showMessageDialog(VistaHoteles.this, "No se puede eliminar el hotel porque tiene reservas u otros datos asociados.", "Error de Integridad", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(VistaHoteles.this, "Error al eliminar: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
                        }
                        ex.printStackTrace();
                    } finally {
                        try {
                            if (ps != null) ps.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        btnNuevo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                DialogoHotel dialogo = new DialogoHotel(VistaHoteles.this);
                dialogo.setVisible(true);
                cargarDatosHoteles("");
            }
        });

        btnModificar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaHoteles.getSelectedRow();

                if (filaSeleccionada == -1) {
                    JOptionPane.showMessageDialog(
                            VistaHoteles.this,
                            "Debe seleccionar un hotel de la lista para modificar.",
                            "Advertencia",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int idHotel = (int) tablaHoteles.getValueAt(filaSeleccionada, 0);
                String nombre = (String) tablaHoteles.getValueAt(filaSeleccionada, 1);
                String ciudad = (String) tablaHoteles.getValueAt(filaSeleccionada, 2);
                String codigo = (String) tablaHoteles.getValueAt(filaSeleccionada, 3);


                DialogoHotel dialogo = new DialogoHotel(VistaHoteles.this, idHotel);

                dialogo.setVisible(true);

                cargarDatosHoteles("");
            }
        });

        txtFiltro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarTablaConFiltro();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarTablaConFiltro();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

        });

        comboFiltroPais.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarTablaConFiltro();
            }
        });


        cargarComboPaises();
        cargarDatosHoteles("");

    }

    private void actualizarTablaConFiltro() {
        String filtro = txtFiltro.getText();
        cargarDatosHoteles(filtro);
    }


    private void cargarDatosHoteles(String filtroTexto) {

        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Nombre");
        modelo.addColumn("Ciudad");
        modelo.addColumn("Código Hotel");

        String filtroPais = null;
        if (comboFiltroPais.getSelectedItem() != null) {
            filtroPais = (String) comboFiltroPais.getSelectedItem();
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql = "SELECT hotel_id, hotel_name, hotel_city, hotel_code FROM hotels WHERE 1=1 ";
        String busquedaTexto = "%" + filtroTexto.trim() + "%";

        if (filtroTexto != null && !filtroTexto.trim().isEmpty()) {
            sql += " AND (hotel_name ILIKE ? OR hotel_city ILIKE ? OR hotel_code ILIKE ?) ";
        }

        if (filtroPais != null && !filtroPais.equals("--- Todos los Países ---")) {
            sql += " AND country_code = ? ";
        }

        sql += " ORDER BY hotel_name";

        try {
            con = ConexionDB.getConexion();
            ps = con.prepareStatement(sql);

            int paramIndex = 1;

            if (filtroTexto != null && !filtroTexto.trim().isEmpty()) {
                ps.setString(paramIndex++, busquedaTexto);
                ps.setString(paramIndex++, busquedaTexto);
                ps.setString(paramIndex++, busquedaTexto);
            }

            if (filtroPais != null && !filtroPais.equals("--- Todos los Países ---")) {
                ps.setString(paramIndex++, filtroPais);
            }

            rs = ps.executeQuery();
            modelo.setRowCount(0);

            while (rs.next()) {
                Object[] fila = new Object[4];
                fila[0] = rs.getInt("hotel_id");
                fila[1] = rs.getString("hotel_name");
                fila[2] = rs.getString("hotel_city");
                fila[3] = rs.getString("hotel_code");
                modelo.addRow(fila);
            }

            tablaHoteles.setModel(modelo);

        } catch (SQLException ex) {
            System.err.println("Error al cargar los datos de hoteles.");
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar datos", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    private void cargarComboPaises() {
        Connection con = ConexionDB.getConexion();

        String sql = "SELECT DISTINCT country_code FROM ref_countries ORDER BY country_code";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            comboFiltroPais.addItem("--- Todos los Países ---");

            while (rs.next()) {
                comboFiltroPais.addItem(rs.getString("country_code"));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar filtro de países", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
