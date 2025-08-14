package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import util.DBUtil;

import java.sql.*;

public class LoginController {

    @FXML private TextField correoField;
    @FXML private PasswordField contrasenaField;
    @FXML private Label mensajeError;

    @FXML
    private void iniciarSesion() {
        String correo = correoField.getText();
        String contrasena = contrasenaField.getText();

        try {
            // ← Aquí obtienes la conexión y pruebas que sea la correcta
            Connection conn = DBUtil.getConnection();
            System.out.println("URL de conexión: " + conn.getMetaData().getURL());
            System.out.println("Usuario de conexión: " + conn.getMetaData().getUserName());

            // ← Luego tu query de login
            String sql = "SELECT * FROM admin WHERE correo = ? AND contrasena = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, correo.trim());
            stmt.setString(2, contrasena.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) correoField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Panel de Administrador");
            } else {
                mensajeError.setText("Credenciales incorrectas.");
            }

            conn.close();

        } catch (SQLException e) {
            // ← Este catch va justo aquí, dentro de iniciarSesion()
            mensajeError.setText("Error al conectar a la base de datos.");
            e.printStackTrace();  // Para ver en consola el error real de la BD
        } catch (Exception e) {
            mensajeError.setText("Ocurrió un error inesperado.");
            e.printStackTrace();
        }
    }
}