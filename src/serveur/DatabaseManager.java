package serveur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/GestionStock";
    private static final String USER = "root"; 
    private static final String PASSWORD = "admin";

    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println(" Connexion MySQL réussie !");
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                System.out.println(" Erreur de connexion à MySQL !");
            }
        }
        return connection;
    }
}
