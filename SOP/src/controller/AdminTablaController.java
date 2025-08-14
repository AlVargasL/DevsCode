package controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
                    setGraphic(new HBox(5, btnEditar, btnEliminar));
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
        // 1. Crear alerta de confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle(null);
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Deseas eliminar este objeto?");

        ButtonType botonContinuar = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
        ButtonType botonCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(botonContinuar, botonCancelar);

        // 2. Esperar la respuesta del usuario
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == botonContinuar) {
                // Si elige continuar, se ejecuta la eliminación
                try (Connection conn = DBUtil.getConnection()) {
                    conn.setAutoCommit(false);

                    // Obtener datos del objeto
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

                    // Cambiar estado a eliminado
                    PreparedStatement marcarEliminado = conn.prepareStatement(
                            "UPDATE objeto_perdido SET estado = 'ELIMINADO' WHERE id = ?"
                    );
                    marcarEliminado.setInt(1, id);
                    marcarEliminado.executeUpdate();

                    // Registrar en historial
                    registrarHistorial(id, nombre, marca, color, "Eliminado");

                    conn.commit();
                    cargarDatos(); // Refrescar tabla

                    // Mostrar mensaje de éxito
                    Alert exito = new Alert(Alert.AlertType.INFORMATION);
                    exito.setTitle(null);
                    exito.setHeaderText(null);
                    exito.setContentText("El objeto se eliminó exitosamente.");
                    exito.showAndWait();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Si elige cancelar, no pasa nada (sale del método)
        });
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

    @FXML
    private void buscar() {

    }
}