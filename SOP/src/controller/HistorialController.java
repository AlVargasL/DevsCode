package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.HistorialCambio;
import util.DBUtil;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HistorialController {

    @FXML private TableView<HistorialCambio> tablaHistorial;
    @FXML private TableColumn<HistorialCambio, String> colNombre;
    @FXML private TableColumn<HistorialCambio, String> colMarca;
    @FXML private TableColumn<HistorialCambio, String> colColor;
    @FXML private TableColumn<HistorialCambio, String> colAccion;
    @FXML private TableColumn<HistorialCambio, String> colFecha;

    private final ObservableList<HistorialCambio> listaHistorial = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        tablaHistorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Asignar propiedades a cada columna
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colAccion.setCellValueFactory(new PropertyValueFactory<>("accion"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        // Cargar datos del historial

        cargarHistorial();
    }

    private void cargarHistorial() {
        listaHistorial.clear();

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT nombre, marca, color, accion, fecha FROM historial ORDER BY fecha DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            System.out.println(rs);

            // Formato que quieres mostrar
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String marca = rs.getString("marca");
                String color = rs.getString("color");
                String accion = rs.getString("accion");

                // Ajustar la fecha a la zona horaria de México
                Timestamp timestamp = rs.getTimestamp("fecha");
                ZonedDateTime fechaMexico = timestamp.toInstant().atZone(ZoneId.of("America/Mexico_City"));
                String fechaFormateada = fechaMexico.format(formatter);

                listaHistorial.add(new HistorialCambio(nombre, marca, color, accion, fechaFormateada));
            }

            tablaHistorial.setItems(listaHistorial);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
