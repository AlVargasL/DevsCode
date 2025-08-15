package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.*;
import model.ObjetoPerdido;
import util.DBUtil;

import java.io.File;
import java.sql.*;

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

        // Método para crear un Stage personalizado con mensaje y botón "Continuar"
        java.util.function.Consumer<String> mostrarStage = mensaje -> {
            Stage modalStage = new Stage();
            modalStage.setTitle("Información");

            Label label = new Label(mensaje);
            label.setStyle("-fx-text-fill: black; -fx-font-size: 15px; -fx-font-family: 'Poppins';");

            String estiloNormal = "-fx-background-color: #2AAD90;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 15px;" +
                    "-fx-background-radius: 5;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 5;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);";

            String estiloHover = "-fx-background-color: #228F77;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 15px;" +
                    "-fx-background-radius: 5;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-radius: 5;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);";

            Button btnContinuar = new Button("Continuar");
            btnContinuar.setStyle(estiloNormal);

            btnContinuar.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                if (isHovered) {
                    btnContinuar.setStyle(estiloHover);
                } else {
                    btnContinuar.setStyle(estiloNormal);
                }
            });

            btnContinuar.setOnAction(e -> modalStage.close());

            VBox vbox = new VBox(15, label, btnContinuar);
            vbox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 20; -fx-alignment: center;");
            Scene scene = new Scene(vbox);
            modalStage.setScene(scene);
            modalStage.sizeToScene();
            modalStage.showAndWait();
        };

        // Verificar campos obligatorios
        if (nombre.isEmpty() || estado == null || rutaImagen == null || marca.isEmpty() || color.isEmpty() || descripcion.isEmpty()) {
            mostrarStage.accept("Por favor completa todos los campos obligatorios.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // Insertar el objeto sin preocuparse por el ID (el trigger lo genera)
            String sqlInsert = "INSERT INTO objeto_perdido (nombre, marca, color, descripcion, estado, imagen) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                stmt.setString(1, nombre);
                stmt.setString(2, marca);
                stmt.setString(3, color);
                stmt.setString(4, descripcion);
                stmt.setString(5, estado);
                stmt.setString(6, rutaImagen);
                stmt.executeUpdate();
            }

            // Obtener el ID generado por el trigger
            int objetoId;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT objeto_perdido_seq.CURRVAL FROM dual")) {
                if (rs.next()) {
                    objetoId = rs.getInt(1);
                } else {
                    mostrarStage.accept("Error al obtener el ID del objeto.");
                    return;
                }
            }

            // Registrar en historial con el ID correcto
            registrarHistorial(objetoId, nombre, marca, color, "Agregado");

            // Mostrar éxito
            mostrarStage.accept("Objeto guardado exitosamente.");
            limpiarCampos();
        } catch (Exception e) {
            mostrarStage.accept("Error al guardar en la base de datos.");
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
