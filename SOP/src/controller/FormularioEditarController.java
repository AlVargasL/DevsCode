package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.ObjetoPerdido;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

//...

public class FormularioEditarController {

    @FXML
    private TextField campoNombre;
    @FXML private TextField campoMarca;
    @FXML private TextField campoColor;
    @FXML private TextField campoDescripcion;
    @FXML private ComboBox<String> estadoCombo;
    @FXML private Label mensajeLabel;
    @FXML
    private ImageView imagenView;

    private String rutaImagen; // Guarda la ruta actual o nueva

    private ObjetoPerdido objeto;

    // Lista fija de estados para el ComboBox (puedes ajustar según tus estados reales)
    private final ObservableList<String> estados = FXCollections.observableArrayList(
            "Nuevo", "En Proceso", "Archivado", "Entregado"
    );

    public void setObjeto(ObjetoPerdido objeto) {
        this.objeto = objeto;
        campoNombre.setText(objeto.getNombre());
        campoMarca.setText(objeto.getMarca());
        campoColor.setText(objeto.getColor());
        campoDescripcion.setText(objeto.getDescripcion());

        rutaImagen = objeto.getImagen(); // ruta guardada en el objeto
        if (rutaImagen != null && !rutaImagen.isEmpty()) {
            imagenView.setImage(new Image("file:" + rutaImagen));
        }

        // Asignamos las opciones al ComboBox
        estadoCombo.setItems(estados);

        // Seleccionamos el estado actual
        estadoCombo.getSelectionModel().select(objeto.getEstado());
    }

    @FXML
    private void cambiarImagen() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Seleccionar nueva imagen");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        java.io.File archivo = fileChooser.showOpenDialog(campoNombre.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.getAbsolutePath();
            imagenView.setImage(new javafx.scene.image.Image("file:" + rutaImagen));
        }
    }

    @FXML
    private void guardarCambios() {

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE objeto_perdido SET nombre = ?, marca = ?, color = ?, descripcion = ?, estado = ?, imagen = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, campoNombre.getText());
            stmt.setString(2, campoMarca.getText());
            stmt.setString(3, campoColor.getText());
            stmt.setString(4, campoDescripcion.getText());

            // Aquí enviamos solo el estado seleccionado
            String estadoSeleccionado = estadoCombo.getSelectionModel().getSelectedItem();
            stmt.setString(5, estadoSeleccionado);
            stmt.setString(6, rutaImagen);

            stmt.setInt(7, objeto.getId());
            stmt.executeUpdate();

            // Registrar en historial
            registrarHistorial(
                    objeto.getId(),
                    campoNombre.getText(),
                    campoMarca.getText(),
                    campoColor.getText(),
                    "Editado"
            );

            mensajeLabel.setText("Objeto guardado exitosamente.");
            limpiarCampos();

        } catch (Exception e) {
            mensajeLabel.setText("Error al guardar en la base de datos.");
            e.printStackTrace();
        }

        ((Stage) campoNombre.getScene().getWindow()).close();
    }

    @FXML
    private void cancelar() {
        ((Stage) campoNombre.getScene().getWindow()).close();
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
        campoNombre.clear();
        campoMarca.clear();
        campoColor.clear();
        campoDescripcion.clear();
    }
}