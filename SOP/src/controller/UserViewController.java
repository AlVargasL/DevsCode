package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import model.ObjetoPerdido;
import util.DBUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class UserViewController {

    @FXML private TableView<ObjetoPerdido> tablaObjetos;
    @FXML private TableColumn<ObjetoPerdido, String> colNombre, colMarca, colColor, colDescripcion, colEstado;
    @FXML private TextField txtBuscar;

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
       // });
        System.out.println(listaFiltrada.toString());

    SortedList<ObjetoPerdido> listaOrdenada = new SortedList<>(listaFiltrada);
    listaOrdenada.comparatorProperty().bind(tablaObjetos.comparatorProperty());
        tablaObjetos.setItems(listaOrdenada);
    }

    @FXML
    private void initialize() {

        tablaObjetos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            buscarFiltro(newValue);
        });

        agregarColumnaReclamar();
        cargarDatos();

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
    }

    private void mostrarVentanaReclamo(ObjetoPerdido obj) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle(null);

        Image imagen = new Image("file:" + obj.getImagen(), 350, 350, true, true);
        ImageView imageView = new ImageView(imagen);

        // Estilos para el botón
        String estiloNormal = "-fx-background-color: #2AAD90;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 20px;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: transparent;" +
                "-fx-border-radius: 5;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);";

        String estiloHover = "-fx-background-color: #228F77;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 20px;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: transparent;" +
                "-fx-border-radius: 5;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);";

        Button btnReclamar = new Button("Reclamar");
        btnReclamar.setPrefWidth(350);
        btnReclamar.setPrefHeight(60);
        btnReclamar.setStyle(estiloNormal);

        // Cambiar estilo cuando el mouse pasa sobre el botón
        btnReclamar.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
            if (isHovered) {
                btnReclamar.setStyle(estiloHover);
            } else {
                btnReclamar.setStyle(estiloNormal);
            }
        });

        btnReclamar.setOnAction(event -> {
            modalStage.close(); // Cierra la vista previa
            mostrarAvisoPrivacidad(obj); // Abre aviso de privacidad
        });

        VBox vbox = new VBox(0, imageView, btnReclamar);
        vbox.setStyle("-fx-background-color: white; -fx-padding: 0px; -fx-alignment: center;");

        Scene scene = new Scene(vbox);
        modalStage.setScene(scene);
        modalStage.sizeToScene();
        modalStage.showAndWait();
    }



    private void agregarColumnaReclamar() {
        TableColumn<ObjetoPerdido, Void> colReclamar = new TableColumn<>("Acción");
        colReclamar.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Ver más");

            {
                btn.setOnAction(event -> {
                    ObjetoPerdido obj = getTableView().getItems().get(getIndex());
                    mostrarVentanaReclamo(obj);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ObjetoPerdido obj = getTableView().getItems().get(getIndex());
                    if (obj == null || "Reclamado".equalsIgnoreCase(obj.getEstado())) {
                        setGraphic(null);  // No mostrar botón si ya reclamado o sin objeto
                    } else {
                        setGraphic(btn);
                        btn.setDisable(false);
                    }
                }
            }
        });
        tablaObjetos.getColumns().add(colReclamar);
    }

    public void insertarHistorial(int objetoId, String accion) {
        String sql = "INSERT INTO HISTORIAL (ID, OBJETO_ID, ACCION, FECHA) VALUES (SEQ_HISTORIAL.NEXTVAL, ?, ?, SYSDATE)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, objetoId);
            stmt.setString(2, accion);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mostrarAvisoPrivacidad(ObjetoPerdido obj) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Aviso de privacidad");
        alert.setHeaderText(null);
        alert.setContentText("Garantizamos la seguridad de tus datos. Requerimos tu colaboración honesta para un proceso transparente.");

// Cambiar fondo y fuente del diálogo completo
        alert.getDialogPane().setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-font-family: 'Poppins';" +
                        "-fx-font-size: 15px;" +
                        "-fx-text-fill: white;"
        );

// Cambiar botones del Alert
        ButtonType btnContinuar = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnContinuar, btnCancelar);

// Después de crear el Alert, aplicar estilo a los botones internos
        Button btnOk = (Button) alert.getDialogPane().lookupButton(btnContinuar);
        Button btnCancel = (Button) alert.getDialogPane().lookupButton(btnCancelar);

        String estiloNormal = "-fx-background-color: #2AAD90;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 15px;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;";

        String estiloHover = "-fx-background-color: #228F77;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 15px;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;";

        btnOk.setStyle(estiloNormal);
        btnCancel.setStyle(estiloNormal);

        btnOk.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
            btnOk.setStyle(isHovered ? estiloHover : estiloNormal);
        });
        btnCancel.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
            btnCancel.setStyle(isHovered ? estiloHover : estiloNormal);
        });

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnContinuar) {
            mostrarAvisoSeguimiento(obj);
        }
    }


        private void marcarComoReclamado(int idObjeto) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE OBJETO_PERDIDO SET ESTADO='Reclamado' WHERE ID=" + idObjeto;
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void mostrarAvisoSeguimiento(ObjetoPerdido obj) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Objeto ubicado");
        alert.setHeaderText(null);
        alert.setContentText(
                "Te informamos que el objeto que reclamaste ha sido ubicado y está disponible para su entrega.\n\n" +
                        "Puedes pasar a recogerlo en el siguiente horario:\n" +
                        "📍 Lugar: Rectoria en atención a alumnos\n" +
                        "🕒 Horario de atención: Lunes a Viernes de 8:00 a.m a 3:00 p.m"
                );

        alert.getDialogPane().setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-font-size: 15px;" +
                        "-fx-text-fill: white;"
        );

        ButtonType btnContinuar = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnContinuar);

        Button btnOk = (Button) alert.getDialogPane().lookupButton(btnContinuar);

        String estiloNormal = "-fx-background-color: #2AAD90;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 15px;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;";

        String estiloHover = "-fx-background-color: #228F77;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 15px;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;";

        btnOk.setStyle(estiloNormal);

        btnOk.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
            btnOk.setStyle(isHovered ? estiloHover : estiloNormal);
        });

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnContinuar) {
            alert.close();
        }

        obj.setEstado("Reclamado");
        marcarComoReclamado(obj.getId());
        insertarHistorial(obj.getId(), "Reclamado");
        tablaObjetos.refresh();
    }

    private void cargarDatos() {
        listaObjetos.clear();
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM objeto_perdido WHERE estado <> 'ELIMINADO' AND estado <> 'Entregado'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                listaObjetos.add(new ObjetoPerdido(
                        rs.getInt("ID"),
                        rs.getString("NOMBRE"),
                        rs.getString("MARCA"),
                        rs.getString("COLOR"),
                        rs.getString("DESCRIPCION"),
                        rs.getString("ESTADO"),
                        rs.getString("IMAGEN")
                ));
            }
            tablaObjetos.setItems(listaObjetos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void irLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtBuscar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Iniciar sesión - Administrador");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
