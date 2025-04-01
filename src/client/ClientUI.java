package client;
import models.Commande;
import models.LigneCommande;
import com.lowagie.text.Element;
import models.Article;
import serveur.StockService;
import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Document;

public class ClientUI extends JFrame {
    private StockService stockService;
    private JComboBox<String> articleDropdown;
    private JTextField quantiteField;
    private JButton acheterButton;
    private JTextArea resultatArea;

    public ClientUI() {
        setTitle("Client - Boutique");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 5000);
            stockService = (StockService) registry.lookup("StockService");
            System.out.println("Connexion au serveur RMI réussie !");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion au serveur !", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        setLayout(new BorderLayout());

        // Panel recherche
        JPanel panelRecherche = new JPanel();
        panelRecherche.add(new JLabel("Rechercher par famille :"));
        JTextField familleField = new JTextField(10);
        panelRecherche.add(familleField);
        JButton boutonRecherche = new JButton("Rechercher");
        JButton boutonAfficherTous = new JButton("Afficher tous");
        JButton boutonReset = new JButton("Réinitialiser");
        panelRecherche.add(boutonRecherche);
        panelRecherche.add(boutonAfficherTous);
        panelRecherche.add(boutonReset);

// Panel du haut (comboBox)
        JPanel panelHaut = new JPanel();
        panelHaut.add(new JLabel("Sélectionnez un article :"));
        articleDropdown = new JComboBox<>();
        panelHaut.add(articleDropdown);

// Panel global pour le haut
        JPanel panelGlobalHaut = new JPanel();
        panelGlobalHaut.setLayout(new BoxLayout(panelGlobalHaut, BoxLayout.Y_AXIS));
        panelGlobalHaut.add(panelRecherche);
        panelGlobalHaut.add(panelHaut);

        add(panelGlobalHaut, BorderLayout.NORTH);


        // Panel centre : quantité + bouton acheter
        JPanel panelCentre = new JPanel();
        panelCentre.add(new JLabel("Quantité :"));
        quantiteField = new JTextField(5);
        panelCentre.add(quantiteField);
        acheterButton = new JButton("Acheter");
        panelCentre.add(acheterButton);
        add(panelCentre, BorderLayout.CENTER);

        JButton boutonAjouterStock = new JButton("Ajouter stock");
        panelCentre.add(boutonAjouterStock);


        // Panel du bas : résultats
        resultatArea = new JTextArea(10, 40);
        resultatArea.setEditable(false);
        add(new JScrollPane(resultatArea), BorderLayout.SOUTH);

        chargerArticles();

        acheterButton.addActionListener(e -> passerCommande());

        boutonRecherche.addActionListener(e -> {
            try {
                String nomFamille = familleField.getText().trim();
                if (nomFamille.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Veuillez entrer un nom de famille !", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                List<Article> articles = stockService.getArticlesByFamille(nomFamille);
                articleDropdown.removeAllItems();
                for (Article article : articles) {
                    articleDropdown.addItem(article.getReference() + " - " + article.getPrixUnitaire() + "€ - Stock: " + article.getStock());
                }
                if (articles.isEmpty()) {
                    resultatArea.append("🔍 Aucun article trouvé pour la famille : " + nomFamille + "\n");
                } else {
                    resultatArea.append("🔍 Articles de la famille '" + nomFamille + "' chargés !\n");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la recherche !", "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        boutonReset.addActionListener(e -> {
            articleDropdown.removeAllItems();
            chargerArticles();
            resultatArea.append("🔄 Liste des articles réinitialisée.\n");
        });

        boutonAjouterStock.addActionListener(e -> {
            try {
                String selection = (String) articleDropdown.getSelectedItem();
                if (selection == null) {
                    JOptionPane.showMessageDialog(this, "❌ Aucun article sélectionné !", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String reference = selection.split(" - ")[0];
                String input = JOptionPane.showInputDialog(this, "Quantité à ajouter :", "Ajout de stock", JOptionPane.PLAIN_MESSAGE);
                if (input == null || input.trim().isEmpty()) return;

                int quantiteAjout = Integer.parseInt(input.trim());
                if (quantiteAjout <= 0) {
                    JOptionPane.showMessageDialog(this, "❌ Quantité invalide !", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success = stockService.ajouterStock(reference, quantiteAjout);
                if (success) {
                    resultatArea.append("✅ Stock ajouté à l'article " + reference + " (+ " + quantiteAjout + ")\n");
                    chargerArticles(); // met à jour la liste
                } else {
                    resultatArea.append("❌ Échec de l'ajout de stock à " + reference + "\n");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Erreur lors de l'ajout de stock !", "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });


        boutonAfficherTous.addActionListener(e -> {
            try {
                articleDropdown.removeAllItems();
                List<Article> articles = stockService.getArticles();
                System.out.println("🔍 Nombre d'articles reçus : " + articles.size());
                for (Article article : articles) {
                    System.out.println("▶️ " + article.getReference() + " - Stock: " + article.getStock());
                    if (article.getStock() > 0) {
                        articleDropdown.addItem(article.getReference() + " - " + article.getPrixUnitaire() + "€ - Stock: " + article.getStock());
                    }
                }
                articleDropdown.revalidate();
                articleDropdown.repaint();

                if (articleDropdown.getItemCount() == 0) {
                    resultatArea.append("⚠️ Aucun article en stock à afficher.\n");
                } else {
                    resultatArea.append("📋 Tous les articles disponibles ont été affichés.\n");
                    resultatArea.append("📋 Liste des articles disponibles :\n");
                    resultatArea.append("------------------------------------\n");

                    for (Article article : articles) {
                        if (article.getStock() > 0) {
                            String ligne = "Réf: " + article.getReference() +
                                    " | Prix: " + article.getPrixUnitaire() + "€" +
                                    " | Stock: " + article.getStock() + "\n";
                            resultatArea.append(ligne);
                        }
                    }
                    resultatArea.append("------------------------------------\n");

                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Erreur lors de l'affichage des articles !", "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }

    private void genererFacturePDF(String reference, int quantite, double prixUnitaire, double total) {
        try {
            String nomFichier = "facture_" + reference + "_" + System.currentTimeMillis() + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(nomFichier));
            document.open();
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("TICKET DE CAISSE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            Font textFont = new Font(Font.HELVETICA, 12);
            document.add(new Paragraph("Date           : " + java.time.LocalDateTime.now(), textFont));
            document.add(new Paragraph("Article        : " + reference, textFont));
            document.add(new Paragraph("Quantité       : " + quantite, textFont));
            document.add(new Paragraph("Prix unitaire  : " + String.format("%.2f €", prixUnitaire), textFont));
            document.add(new Paragraph("--------------------------------------", textFont));
            document.add(new Paragraph("TOTAL          : " + String.format("%.2f €", total), textFont));
            document.add(new Paragraph("Mode de paiement : Carte Bancaire", textFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Merci pour votre achat !", textFont));
            document.close();
            resultatArea.append("🧾 Facture PDF générée : " + nomFichier + "\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Erreur lors de la génération du PDF !", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void chargerArticles() {
        try {
            articleDropdown.removeAllItems();
            List<Article> articles = stockService.getArticles();
            for (Article article : articles) {
                articleDropdown.addItem(article.getReference() + " - " + article.getPrixUnitaire() + "€ - Stock: " + article.getStock());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Erreur lors du chargement des articles !", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void rafraichirArticlesDisponibles() {
        articleDropdown.removeAllItems();
        try {
            List<Article> articles = stockService.getArticles();
            for (Article article : articles) {
                if (article.getStock() > 0) {
                    articleDropdown.addItem(article.getReference() + " - " + article.getPrixUnitaire() + "€ - Stock: " + article.getStock());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du rechargement des articles disponibles.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void passerCommande() {
        try {
            String selection = (String) articleDropdown.getSelectedItem();
            if (selection == null) {
                JOptionPane.showMessageDialog(this, "❌ Aucun article sélectionné !", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String reference = selection.split(" - ")[0];
            int quantite = Integer.parseInt(quantiteField.getText());
            if (quantite <= 0) {
                JOptionPane.showMessageDialog(this, "❌ Veuillez entrer une quantité valide !", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String[] parts = selection.split(" - ");
            double prixUnitaire = Double.parseDouble(parts[1].replace("€", "").trim());
            Commande commande = new Commande(1);
            LigneCommande ligne = new LigneCommande(reference, quantite, prixUnitaire);
            commande.ajouterLigne(ligne);
            boolean success = stockService.enregistrerCommande(commande);
            if (success) {
                double total = prixUnitaire * quantite;
                resultatArea.append("✅ Commande réussie pour " + quantite + " x " + reference + "\n");
                genererFacturePDF(reference, quantite, prixUnitaire, total);
                rafraichirArticlesDisponibles();
            } else {
                resultatArea.append("❌ Commande échouée pour " + reference + " (stock insuffisant ou erreur)\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Erreur lors du passage de la commande !", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientUI::new);
    }
}
