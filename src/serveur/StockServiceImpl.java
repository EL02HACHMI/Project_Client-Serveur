package serveur;

import models.Article;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockServiceImpl extends UnicastRemoteObject implements StockService {
    private Connection conn;

    public StockServiceImpl() throws RemoteException {
        super();
        conn = DatabaseManager.getConnection(); // Connexion MySQL via DatabaseManager
    }

    @Override
    public List<Article> getArticles() throws RemoteException {
        List<Article> articles = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Article");
            while (rs.next()) {
                articles.add(new Article(rs.getString("reference"), rs.getInt("id_famille"), rs.getDouble("prix_unitaire"), rs.getInt("stock")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }

    @Override
    public Article getArticleByReference(String reference) throws RemoteException {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Article WHERE reference = ?");
            stmt.setString(1, reference);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Article(rs.getString("reference"), rs.getInt("id_famille"), rs.getDouble("prix_unitaire"), rs.getInt("stock"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean passerCommande(int idCommande, String reference, int quantite) throws RemoteException {
        try {
            // Vérification du stock disponible
            PreparedStatement checkStock = conn.prepareStatement("SELECT stock, prix_unitaire FROM Article WHERE reference = ?");
            checkStock.setString(1, reference);
            ResultSet rs = checkStock.executeQuery();

            if (rs.next()) {
                int stockDispo = rs.getInt("stock");
                double prixUnitaire = rs.getDouble("prix_unitaire"); // On récupère le prix de la base de données

                if (stockDispo >= quantite) {
                    // Insérer la commande dans LigneCommande
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO LigneCommande (id_commande, reference, date_Commande, prix_vente) VALUES (?, ?, NOW(), ?)");
                    stmt.setInt(1, idCommande);
                    stmt.setString(2, reference);
                    stmt.setDouble(3, prixUnitaire * quantite); // Utilisation du prix de la base
                    int rowsInserted = stmt.executeUpdate();

                    // Mettre à jour le stock
                    PreparedStatement updateStock = conn.prepareStatement(
                            "UPDATE Article SET stock = stock - ? WHERE reference = ?");
                    updateStock.setInt(1, quantite);
                    updateStock.setString(2, reference);
                    int rowsUpdated = updateStock.executeUpdate();

                    return rowsInserted > 0 && rowsUpdated > 0;
                } else {
                    System.out.println("❌ Stock insuffisant !");
                }
            } else {
                System.out.println("❌ Article non trouvé !");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
