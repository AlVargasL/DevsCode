package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.ObjetoPerdido;
import util.DBUtil;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class AdminDashboardController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnVerObjeto;
    @FXML
    private Button btnAgregarObjeto;
    @FXML
    private Button btnVerHistorial;

    private List<Button> botonesMenu;

    @FXML
    public void initialize() {
        botonesMenu = Arrays.asList(btnVerObjeto, btnAgregarObjeto, btnVerHistorial);

        cambiarVista("/fxml/admin_tabla.fxml");

    }

    private void cambiarVista(String rutaFXML) {
        Parent vista = cargarVista(rutaFXML);
        if (vista != null) {
            contentArea.getChildren().setAll(vista);
        }
    }

    private Parent cargarVista(String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void mostrarAdminDashboard() {
        resaltarBoton(btnVerObjeto);
        cambiarVista("/fxml/admin_tabla.fxml");
    }

    @FXML
    private void mostrarFormularioAgregar() {
        resaltarBoton(btnAgregarObjeto);
        cambiarVista("/fxml/formulario_agregar.fxml");
    }

    @FXML
    private void mostrarHistorial() {
        resaltarBoton(btnVerHistorial);
        cambiarVista("/fxml/historial.fxml");
    }

    private void resaltarBoton(Button botonActivo) {
        for (Button b : botonesMenu) {
            if (b == botonActivo) {
                b.setStyle("-fx-font-size: 15px; -fx-background-color: #f5f5f5; -fx-text-fill: black;");
            } else {
                b.setStyle("-fx-font-size: 15px; -fx-background-color: #dcdcdc; -fx-border-radius: 0; -fx-background-radius: 0; -fx-text-fill: black;");
            }
        }
    }

    public void mostrarFormularioCredencial(ObjetoPerdido obj) {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(10));

        Label label = new Label("Sube la imagen de tu credencial:");
        Button btnSubir = new Button("Seleccionar archivo");
        Label archivoSeleccionado = new Label("Ningún archivo seleccionado");

        final File[] archivoSeleccionadoFile = new File[1];

        btnSubir.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                archivoSeleccionadoFile[0] = selectedFile;
                archivoSeleccionado.setText(selectedFile.getName());
            }
        });

        Button btnReclamar = new Button("Reclamar");
        btnReclamar.setOnAction(e -> {
            // Acción al reclamar
        });

        root.getChildren().addAll(label, btnSubir, archivoSeleccionado, btnReclamar);

        Scene scene = new Scene(root, 350, 200);
        stage.setScene(scene);
        stage.setTitle("Formulario de credencial");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    @FXML
    private void irLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/admin_dashboard.css").toExternalForm());

            for (String sheet : scene.getStylesheets()) {
                System.out.println(sheet);
            }

            stage.setScene(scene);
            stage.setTitle("Objetos Perdidos");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Resto de métodos (cargarDatos, eliminarObjeto, etc.) igual que antes
}
