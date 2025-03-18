package serveur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/GestionStock";
    private static final String USER = "root"; // Remplace par ton utilisateur MySQL
    private static final String PASSWORD = "admin"; // Remplace par ton mot de passe MySQL

    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver"); // Vérifier que le driver est chargé
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion MySQL réussie !");
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                System.out.println("❌ Erreur de connexion à MySQL !");
            }
        }
        return connection;
    }
}
