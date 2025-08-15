package util;

import java.sql.*;

public class DBUtil {
    private static final String URL = "jdbc:oracle:thin:@kl2dxkgaxlmk2gdb_high?TNS_ADMIN=/Users/alanvargas/Downloads/Wallet_KL2DXKGAXLMK2GDB";
    private static final String USER = "ADMIN";
    private static final String PASSWORD = "Alanvargas_07"; // Cambia si usas MySQL

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);

    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("¡Conexión exitosa!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}