package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
        vbox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 0px; -fx-alignment: center;");

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

                        setAlignment(javafx.geometry.Pos.CENTER);
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
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Aviso de privacidad");

        // Texto del aviso
        Label lblAviso = new Label(
                "Garantizamos la seguridad de tus datos.\n" +
                        "Requerimos tu colaboración honesta para un proceso transparente.\n\n" +
                        "¿Deseas continuar con el proceso para reclamar el objeto perdido?"
        );
        lblAviso.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 15px; -fx-text-fill: black;");
        lblAviso.setWrapText(true);

        // Botones
        Button btnCancelar = new Button("Cancelar");
        Button btnContinuar = new Button("Continuar");

        String estiloNormalContinuar = "-fx-background-color: #2AAD90; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;";
        String estiloHoverContinuar = "-fx-background-color: #228F77; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;";

        String estiloNormalCancelar = "-fx-background-color: #b81414; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;";
        String estiloHoverCancelar = "-fx-background-color: #861d1d; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;";

        btnContinuar.setStyle(estiloNormalContinuar);
        btnCancelar.setStyle(estiloNormalCancelar);

        // Aplica cursor manualmente
        btnContinuar.setCursor(Cursor.HAND);
        btnCancelar.setCursor(Cursor.HAND);

        // Eventos hover
        btnContinuar.setOnMouseEntered(e -> btnContinuar.setStyle(estiloHoverContinuar));
        btnContinuar.setOnMouseExited(e -> btnContinuar.setStyle(estiloNormalContinuar));

        btnCancelar.setOnMouseEntered(e -> btnCancelar.setStyle(estiloHoverCancelar));
        btnCancelar.setOnMouseExited(e -> btnCancelar.setStyle(estiloNormalCancelar));

        btnContinuar.setOnAction(e -> {
            stage.close();
            mostrarAvisoSeguimiento(obj);
        });

        btnCancelar.setOnAction(e -> stage.close());

        HBox botones = new HBox(10, btnCancelar, btnContinuar);
        botones.setAlignment(Pos.CENTER); // Asegura que estén centrados

        VBox vbox = new VBox(20, lblAviso, botones);
        vbox.setStyle("-fx-padding: 20px; -fx-background-color: #f5f5f5;");
        vbox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();

        // Esto asegura que el cursor y hover funcionen correctamente después de agregarlos a la escena
        btnContinuar.applyCss();
        btnCancelar.applyCss();
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
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Objeto ubicado");

        // Texto del aviso
        Label lblAviso = new Label(
                "Te informamos que el objeto que reclamaste ha sido ubicado y está disponible para su entrega.\n\n" +
                        "Puedes pasar a recogerlo en el siguiente horario:\n" +
                        "📍 Lugar: Rectoria en atención a alumnos\n" +
                        "🕒 Horario de atención: Lunes a Viernes de 8:00 a.m a 3:00 p.m"
        );
        lblAviso.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 15px; -fx-text-fill: black;");
        lblAviso.setWrapText(true);

        // Botón Continuar
        Button btnContinuar = new Button("Continuar");
        String estiloNormal = "-fx-background-color: #2AAD90;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 15px;" +
                "-fx-background-radius: 5;";
        String estiloHover = "-fx-background-color: #228F77;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 15px;" +
                "-fx-background-radius: 5;";

        btnContinuar.setStyle(estiloNormal);
        btnContinuar.setCursor(Cursor.HAND);

        // Hover funcional
        btnContinuar.setOnMouseEntered(e -> btnContinuar.setStyle(estiloHover));
        btnContinuar.setOnMouseExited(e -> btnContinuar.setStyle(estiloNormal));

        btnContinuar.setOnAction(e -> stage.close());

        HBox hBoxBotones = new HBox(btnContinuar);
        hBoxBotones.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(20, lblAviso, hBoxBotones);
        vbox.setStyle("-fx-padding: 20px; -fx-background-color: #f5f5f5;");
        vbox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.showAndWait();

        // Mantener la lógica de marcar como reclamado
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