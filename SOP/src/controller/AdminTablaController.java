package controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.ObjetoPerdido;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminTablaController {


    @FXML private TextField txtBuscar;
    @FXML
    private TableView<ObjetoPerdido> tablaObjetos;
    @FXML
    private TableColumn<ObjetoPerdido, String> colNombre;
    @FXML
    private TableColumn<ObjetoPerdido, String> colMarca;
    @FXML
    private TableColumn<ObjetoPerdido, String> colColor;
    @FXML
    private TableColumn<ObjetoPerdido, String> colDescripcion;
    @FXML
    private TableColumn<ObjetoPerdido, String> colEstado;
    @FXML
    private TableColumn<ObjetoPerdido, ImageView> colImagen;
    @FXML
    private TableColumn<ObjetoPerdido, Void> colAcciones;

    private ObservableList<ObjetoPerdido> listaObjetos = FXCollections.observableArrayList();

    @FXML
    private void buscarFiltro(String newValue) {
        ObservableList<ObjetoPerdido> listObjetosPerdidos=FXCollections.observableArrayList(listaObjetos);
        FilteredList<ObjetoPerdido> listaFiltrada = new FilteredList<>(listObjetosPerdidos, p -> true);

        listaFiltrada.setPredicate(objeto -> {
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = newValue.toLowerCase();
            return objeto.getNombre().toLowerCase().contains(lowerCaseFilter);
        });

        System.out.println(listaFiltrada.toString());

        SortedList<ObjetoPerdido> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaObjetos.comparatorProperty());
        tablaObjetos.setItems(listaOrdenada);
    }

    @FXML
    public void initialize() {
        cargarDatos();

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            buscarFiltro(newValue);
        });

        tablaObjetos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colImagen.setCellValueFactory(cellData -> {
            String path = cellData.getValue().getImagen();
            Image img = new Image("file:" + path, 60, 60, true, true);
            ImageView imgView = new ImageView(img);
            return new SimpleObjectProperty<>(imgView);
        });

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");

            {
                btnEditar.setOnAction(e -> {
                    ObjetoPerdido op = getTableView().getItems().get(getIndex());
                    mostrarFormularioEdicion(op);
                });

                btnEliminar.setOnAction(e -> {
                    ObjetoPerdido op = getTableView().getItems().get(getIndex());
                    eliminarObjeto(op.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5, btnEditar, btnEliminar);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER); // <-- centramos los botones dentro del HBox
                    setGraphic(hbox);

                    // Centramos el HBox dentro de la celda
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
    }

    void cargarDatos() {
        listaObjetos.clear();
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM objeto_perdido WHERE estado <> 'ELIMINADO'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                listaObjetos.add(new ObjetoPerdido(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("marca"),
                        rs.getString("color"),
                        rs.getString("descripcion"),
                        rs.getString("estado"),
                        rs.getString("imagen")
                ));
            }
            tablaObjetos.setItems(listaObjetos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarFormularioEdicion(ObjetoPerdido objeto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formulario_editar.fxml"));
            Parent root = loader.load();

            FormularioEditarController controller = loader.getController();
            controller.setObjeto(objeto);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(null);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            cargarDatos(); // Recarga la tabla después de editar
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void eliminarObjeto(int id) {
        Stage stageConfirm = new Stage();
        stageConfirm.setTitle("Confirmación");

        Label label = new Label("¿Deseas eliminar este objeto?");
        label.setStyle("-fx-font-size: 15px; -fx-font-family: 'Poppins';");

        Button btnCancelar = new Button("Cancelar");
        Button btnContinuar = new Button("Continuar");

        String estiloNormalContinuar = "-fx-background-color: #2AAD90; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;";
        String estiloHoverContinuar = "-fx-background-color: #228F77; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;";

        String estiloNormalCancelar = "-fx-background-color: #b81414; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;";
        String estiloHoverCancelar = "-fx-background-color: #861d1d; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;";

        btnContinuar.setStyle(estiloNormalContinuar);
        btnCancelar.setStyle(estiloNormalCancelar);

        btnContinuar.setCursor(Cursor.HAND);
        btnCancelar.setCursor(Cursor.HAND);

        btnContinuar.setOnMouseEntered(e -> btnContinuar.setStyle(estiloHoverContinuar));
        btnContinuar.setOnMouseExited(e -> btnContinuar.setStyle(estiloNormalContinuar));

        btnCancelar.setOnMouseEntered(e -> btnCancelar.setStyle(estiloHoverCancelar));
        btnCancelar.setOnMouseExited(e -> btnCancelar.setStyle(estiloNormalCancelar));

        HBox hbox = new HBox(10, btnCancelar, btnContinuar);
        hbox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, label, hbox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-background-color: #f5f5f5; -fx-font-size: 15px; -fx-font-family: 'Poppins';");

        stageConfirm.setScene(new Scene(root, 350, 150));
        stageConfirm.initModality(Modality.APPLICATION_MODAL);

        btnContinuar.setOnAction(ev -> {
            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);

                PreparedStatement obtener = conn.prepareStatement(
                        "SELECT nombre, marca, color FROM objeto_perdido WHERE id = ?"
                );
                obtener.setInt(1, id);
                ResultSet rs = obtener.executeQuery();

                String nombre = "", marca = "", color = "";
                if (rs.next()) {
                    nombre = rs.getString("nombre");
                    marca = rs.getString("marca");
                    color = rs.getString("color");
                }

                PreparedStatement marcarEliminado = conn.prepareStatement(
                        "UPDATE objeto_perdido SET estado = 'ELIMINADO' WHERE id = ?"
                );
                marcarEliminado.setInt(1, id);
                marcarEliminado.executeUpdate();

                registrarHistorial(id, nombre, marca, color, "Eliminado");

                conn.commit();
                cargarDatos();
                stageConfirm.close();

                // ✅ Mostrar Stage de éxito dentro de Platform.runLater
                javafx.application.Platform.runLater(() -> {
                    Stage stageExito = new Stage();
                    stageExito.setTitle("Éxito");

                    Label exitoLabel = new Label("El objeto se eliminó exitosamente.");
                    exitoLabel.setStyle("-fx-font-size: 15px; -fx-font-family: 'Poppins';");

                    Button btnCerrar = new Button("Continuar");
                    btnCerrar.setStyle("-fx-background-color: #2AAD90; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;");
                    btnCerrar.setCursor(Cursor.HAND);

                    btnCerrar.setOnMouseEntered(e -> btnCerrar.setStyle("-fx-background-color: #228F77; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;"));
                    btnCerrar.setOnMouseExited(e -> btnCerrar.setStyle("-fx-background-color: #2AAD90; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;"));

                    btnCerrar.setOnAction(e -> stageExito.close());

                    VBox rootExito = new VBox(20, exitoLabel, btnCerrar);
                    rootExito.setAlignment(Pos.CENTER);
                    rootExito.setStyle("-fx-padding: 20; -fx-background-color: #f5f5f5; -fx-font-size: 15px; -fx-font-family: 'Poppins';");

                    Scene sceneExito = new Scene(rootExito, 350, 150);
                    stageExito.setScene(sceneExito);
                    stageExito.initModality(Modality.APPLICATION_MODAL);
                    stageExito.showAndWait();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mostrarMensaje("Error al eliminar el objeto.", "#f5f5f5");
            }
        });

        btnCancelar.setOnAction(ev -> stageConfirm.close());
        stageConfirm.showAndWait();
    }

    // Método reusable para mostrar mensajes con botón Continuar
    private void mostrarMensaje(String mensaje, String colorFondo) {
        Stage stage = new Stage();
        stage.setTitle("Información");

        Label label = new Label(mensaje);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Button btn = new Button("Continuar");
        btn.setOnAction(e -> stage.close());

        VBox root = new VBox(15, label, btn);
        root.setStyle("-fx-background-color: " + colorFondo + "; -fx-padding: 20; -fx-alignment: center;");

        stage.setScene(new Scene(root, 350, 150));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }


    private void registrarHistorial(int objetoId, String nombre, String marca, String color, String accion) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO HISTORIAL (objeto_id, nombre, marca, color, accion, fecha) VALUES (?, ?, ?, ?, ?, SYSDATE)";
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
}
