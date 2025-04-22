
package serveur;
import models.Commande;
import models.LigneCommande;
import models.Article;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StockServiceImpl extends UnicastRemoteObject implements StockService {
    private Connection conn;

    public StockServiceImpl() throws RemoteException {
        super();
        conn = DatabaseManager.getConnection(); // Connexion MySQL via DatabaseManager
    }

    @Override
    public boolean modifierPrixArticle(String reference, double nouveauPrix) throws RemoteException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE Article SET Prix_Unitaire = ? WHERE Reference = ?")) {
            String sql = "UPDATE Article SET Prix_Unitaire = ? WHERE Reference = ?";
            ps.setDouble(1, nouveauPrix);
            ps.setString(2, reference);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean enregistrerCommande(Commande commande) throws RemoteException {
        try {
            for (LigneCommande ligne : commande.getLignes()) {
                System.out.println("🔍 Traitement de l'article : " + ligne.getReference());

                // Vérifier le stock
                PreparedStatement checkStock = conn.prepareStatement("SELECT stock FROM Article WHERE reference = ?");
                checkStock.setString(1, ligne.getReference());
                ResultSet rs = checkStock.executeQuery();

                if (rs.next() && rs.getInt("stock") >= ligne.getQuantite()) {
                    // Insérer la ligne de commande
                    PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO LigneCommande (id_commande, reference, date_commande, prix_vente) VALUES (?, ?, NOW(), ?)");
                    insert.setInt(1, commande.getIdCommande());
                    insert.setString(2, ligne.getReference());
                    insert.setDouble(3, ligne.getPrixVente());
                    insert.executeUpdate();

                    // Mettre à jour le stock
                    PreparedStatement update = conn.prepareStatement(
                            "UPDATE Article SET stock = stock - ? WHERE reference = ?");
                    update.setInt(1, ligne.getQuantite());
                    update.setString(2, ligne.getReference());
                    update.executeUpdate();
                } else {
                    System.out.println("❌ Stock insuffisant ou article non trouvé : " + ligne.getReference());
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Article> getArticlesByFamille(String nomFamille) throws RemoteException {
        List<Article> articles = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT a.*
                FROM Article a
                JOIN Famille f ON a.id_famille = f.id_famille
                WHERE f.nom_famille LIKE ?
            """);
            stmt.setString(1, "%" + nomFamille + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                articles.add(new Article(
                        rs.getString("reference"),
                        rs.getString("nom_article"),
                        rs.getInt("id_famille"),
                        rs.getDouble("prix_unitaire"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }

    @Override
    public List<Article> getArticlesByFamilleId(int idFamille) throws RemoteException {
        List<Article> articles = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT * FROM Article
                WHERE id_famille = ? AND stock > 0
            """);
            stmt.setInt(1, idFamille);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                articles.add(new Article(
                        rs.getString("reference"),
                        rs.getString("nom_article"),
                        rs.getInt("id_famille"),
                        rs.getDouble("prix_unitaire"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }

    @Override
    public String getNomFamilleById(int idFamille) throws RemoteException {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT nom_famille FROM Famille WHERE id_famille = ?");
            stmt.setInt(1, idFamille);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nom_famille");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Inconnu";
    }

    @Override
    public Map<Integer, String> getToutesLesFamilles() throws RemoteException {
        Map<Integer, String> familles = new LinkedHashMap<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_famille, nom_famille FROM Famille ORDER BY id_famille");
            while (rs.next()) {
                familles.put(rs.getInt("id_famille"), rs.getString("nom_famille"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return familles;
    }

    @Override
    public double getChiffreAffaireParDate(String date) throws RemoteException {
        double total = 0;
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT SUM(prix_vente) as total 
                FROM LigneCommande 
                WHERE DATE(date_commande) = ?
            """);
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    @Override
    public List<Article> getArticles() throws RemoteException {
        List<Article> articles = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Article"); // pas de filtre ici
            while (rs.next()) {
                articles.add(new Article(
                        rs.getString("reference"),
                        rs.getString("nom_article"),
                        rs.getInt("id_famille"),
                        rs.getDouble("prix_unitaire"),
                        rs.getInt("stock")
                ));
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
                return new Article(
                        rs.getString("reference"),
                        rs.getString("nom_article"),
                        rs.getInt("id_famille"),
                        rs.getDouble("prix_unitaire"),
                        rs.getInt("stock")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean ajouterStock(String reference, int quantite) throws RemoteException {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE Article SET stock = stock + ? WHERE reference = ?");
            stmt.setInt(1, quantite);
            stmt.setString(2, reference);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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