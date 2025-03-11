package serveur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestJDBC {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/GestionStock"; // Remplace GestionStock par ta BD
        String user = "root"; // Remplace par ton utilisateur MySQL
        String password = "admin"; // Remplace par ton mot de passe

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connexion réussie !");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Échec de connexion !");
        }
    }
}
