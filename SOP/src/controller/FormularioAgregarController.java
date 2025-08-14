package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.*;
import model.ObjetoPerdido;
import util.DBUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class FormularioAgregarController {

    @FXML private TextField nombreField, marcaField, colorField, descripcionField;
    @FXML private ComboBox<String> estadoCombo;
    @FXML private Label nombreImagenLabel, mensajeLabel;
    @FXML private ImageView previewImagen;

    private String rutaImagen = null;

    @FXML
    public void seleccionarImagen() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg")
        );

        File archivo = fileChooser.showOpenDialog(null);
        if (archivo != null) {
            rutaImagen = archivo.getAbsolutePath();
            nombreImagenLabel.setText(archivo.getName());
            previewImagen.setImage(new Image(archivo.toURI().toString()));
        }
    }

    @FXML
    public void guardarObjeto() {
        String nombre = nombreField.getText();
        String marca = marcaField.getText();
        String color = colorField.getText();
        String descripcion = descripcionField.getText();
        String estado = estadoCombo.getValue();

        if (nombre.isEmpty() || estado == null || rutaImagen == null || marca.isEmpty() || color.isEmpty() || descripcion.isEmpty()) {
            mensajeLabel.setText("Por favor completa todos los campos obligatorios.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // Preparamos el INSERT y pedimos que nos regrese el ID generado
            String sql = "INSERT INTO objeto_perdido (nombre, marca, color, descripcion, estado, imagen) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, marca);
            stmt.setString(3, color);
            stmt.setString(4, descripcion);
            stmt.setString(5, estado);
            stmt.setString(6, rutaImagen);
            stmt.executeUpdate();

            // Obtener el ID generado
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int objetoId = rs.getInt(1);

                // Registrar en historial
                registrarHistorial(objetoId, nombre, marca, color, "Agregado");
            }

            mensajeLabel.setText("Objeto guardado exitosamente.");
            limpiarCampos();

        } catch (Exception e) {
            mensajeLabel.setText("Error al guardar en la base de datos.");
            e.printStackTrace();
        }
    }

    private void registrarHistorial(int objetoId, String nombre, String marca, String color, String accion) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO historial (objeto_id, nombre, marca, color, accion, fecha) VALUES (?, ?, ?, ?, ?, SYSDATE)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, objetoId);
            stmt.setString(2, nombre);
            stmt.setString(3, marca);
            stmt.setString(4, color);
            stmt.setString(5, accion);

            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void limpiarCampos() {
        nombreField.clear();
        marcaField.clear();
        colorField.clear();
        descripcionField.clear();
        estadoCombo.getSelectionModel().clearSelection();
        nombreImagenLabel.setText("(Sin imagen)");
        previewImagen.setImage(null);
        rutaImagen = null;
    }
}