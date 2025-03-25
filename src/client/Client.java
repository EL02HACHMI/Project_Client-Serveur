package client;

import models.Article;
import serveur.StockService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static StockService stockService; // Interface RMI

    public static void main(String[] args) {
        try {
            // Connexion au registre RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 5000);
            stockService = (StockService) registry.lookup("StockService");
            System.out.println("✅ Connexion au serveur RMI réussie !");

            // Affichage du menu pour le client
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n===== MENU CLIENT =====");
                System.out.println("1. Afficher la liste des articles");
                System.out.println("2. Acheter un article");
                System.out.println("3. Quitter");
                System.out.print("Votre choix : ");
                int choix = scanner.nextInt();
                scanner.nextLine();

                switch (choix) {
                    case 1:
                        afficherArticles();
                        break;
                    case 2:
                        passerCommande(scanner);
                        break;
                    case 3:
                        System.out.println("👋 Au revoir !");
                        scanner.close();
                        return;
                    default:
                        System.out.println("❌ Choix invalide !");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour afficher les articles disponibles
    private static void afficherArticles() {
        try {
            List<Article> articles = stockService.getArticles();
            System.out.println("\n📦 Liste des articles disponibles :");
            for (Article article : articles) {
                System.out.println("🔹 Référence: " + article.getReference() +
                        " | Prix: " + article.getPrixUnitaire() +
                        "€ | Stock: " + article.getStock());
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la récupération des articles !");
            e.printStackTrace();
        }
    }

    // Méthode pour passer une commande
    private static void passerCommande(Scanner scanner) {
        try {
            System.out.print("📌 Entrez la référence de l'article : ");
            String reference = scanner.nextLine();
            System.out.print("📌 Entrez la quantité souhaitée : ");
            int quantite = scanner.nextInt();
            scanner.nextLine();

            // Appel au service RMI pour passer la commande
            boolean success = stockService.passerCommande(1, reference, quantite);
            if (success) {
                System.out.println("✅ Commande passée avec succès ! Le stock a été mis à jour.");
            } else {
                System.out.println("❌ La commande a échoué (vérifiez le stock ou la référence).");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du passage de la commande !");
            e.printStackTrace();
        }
    }

}
