package client;

import java.awt.Font;
import java.util.Map;
import java.util.HashMap;
import com.formdev.flatlaf.FlatDarkLaf;
import models.Article;
import models.Commande;
import models.LigneCommande;
import serveur.StockService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

public class ClientUI extends JFrame {
    private StockService stockService;
    private JComboBox<String> articleDropdown;
    private JTextField quantiteField;
    private JTextArea resultatArea;
    private JComboBox<String> comboFamilles;
    private Map<String, Integer> mapFamilles;

    public ClientUI() {
        setTitle("🛒 Boutique RMI - Client");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 5000);
            stockService = (StockService) registry.lookup("StockService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion au serveur !", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // ==== Barre latérale gauche ====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        sidebar.setBackground(new Color(40, 40, 40));

        JTextField familleField = new JTextField();
        JButton btnRechercher = new JButton("🔍 Rechercher");
        JButton btnAfficherTous = new JButton("📋 Afficher tous");
        JButton btnReset = new JButton("🔄 Réinitialiser");
        JButton btnAjouterStock = new JButton("➕ Ajouter Stock");
        JButton btnChiffreAffaire = new JButton("💰 Chiffre d'affaires");

        mapFamilles = new HashMap<>();
        comboFamilles = new JComboBox<>();
        chargerFamilles();

        sidebar.add(new JLabel("Famille :"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(familleField);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnRechercher);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnAfficherTous);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnReset);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnAjouterStock);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(new JLabel("📂 Familles disponibles :"));
        sidebar.add(comboFamilles);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnChiffreAffaire);

        add(sidebar, BorderLayout.WEST);

        // ==== Panel central principal ====
        JPanel centre = new JPanel();
        centre.setLayout(new BorderLayout());
        centre.setBorder(new EmptyBorder(20, 20, 20, 20));

        // === Zone sélection article ===
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        articleDropdown = new JComboBox<>();
        selectionPanel.add(new JLabel("Sélectionnez un article :"));
        selectionPanel.add(articleDropdown);

        // === Zone quantité + achat ===
        JPanel achatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantiteField = new JTextField(5);
        JButton btnAcheter = new JButton("🛒 Acheter");
        achatPanel.add(new JLabel("Quantité :"));
        achatPanel.add(quantiteField);
        achatPanel.add(btnAcheter);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.add(selectionPanel);
        actionsPanel.add(Box.createVerticalStrut(10));
        actionsPanel.add(achatPanel);

        centre.add(actionsPanel, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);

        // === Zone de résultat (log) ===
        resultatArea = new JTextArea(12, 50);
        resultatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultatArea.setMargin(new Insets(10, 10, 10, 10));
        resultatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultatArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📜 Journal"));
        add(scrollPane, BorderLayout.SOUTH);

        // === Actions ===
        btnAcheter.addActionListener(e -> passerCommande());
        btnRechercher.addActionListener(e -> rechercherParFamille(familleField.getText().trim()));
        btnAfficherTous.addActionListener(e -> chargerArticles());
        btnReset.addActionListener(e -> {
            familleField.setText("");
            resultatArea.setText("");
            chargerArticles();
            resultatArea.append("🔁 Réinitialisation terminée.\n");
        });
        btnAjouterStock.addActionListener(e -> ajouterStock());
        btnChiffreAffaire.addActionListener(e -> {
            String date = JOptionPane.showInputDialog(this, "Entrez une date (YYYY-MM-DD) :");
            if (date != null && !date.trim().isEmpty()) {
                try {
                    double chiffre = stockService.getChiffreAffaireParDate(date.trim());
                    resultatArea.append("💰 Chiffre d'affaires du " + date + " : " + chiffre + " €\n");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la récupération du chiffre d'affaires !");
                    ex.printStackTrace();
                }
            }
        });

        chargerArticles();
        setVisible(true);
    }


    private void chargerFamilles() {
        try {
            Map<Integer, String> familles = stockService.getToutesLesFamilles();
            comboFamilles.removeAllItems();
            mapFamilles.clear();
            for (Map.Entry<Integer, String> entry : familles.entrySet()) {
                String label = entry.getValue() + " (ID: " + entry.getKey() + ")";
                comboFamilles.addItem(label);
                mapFamilles.put(label, entry.getKey());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rechercherParFamille(String input) {
        try {
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Entrez un nom ou un ID de famille !");
                return;
            }

            List<Article> articles;
            String familleAffichee;

            try {
                int idFamille = Integer.parseInt(input); // si chiffre
                articles = stockService.getArticlesByFamilleId(idFamille);
                familleAffichee = stockService.getNomFamilleById(idFamille);
            } catch (NumberFormatException e) {
                articles = stockService.getArticlesByFamille(input); // sinon nom
                familleAffichee = input;
            }

            articleDropdown.removeAllItems();
            for (Article article : articles) {
                articleDropdown.addItem(article.getReference() + " - " + article.getNomArticle() + " - " + article.getPrixUnitaire() + "€ - Stock: " + article.getStock());
            }

            if (articles.isEmpty()) {
                resultatArea.append("❌ Aucun article trouvé pour la famille : " + familleAffichee + "\n");
            } else {
                resultatArea.append("🔍 Articles de la famille \"" + familleAffichee + "\" trouvés (" + articles.size() + " résultats)\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la recherche !");
        }
    }



    private void ajouterStock() {
        try {
            String selection = (String) articleDropdown.getSelectedItem();
            if (selection == null) return;
            String reference = selection.split(" - ")[0];
            String input = JOptionPane.showInputDialog(this, "Quantité à ajouter :");
            if (input == null || input.trim().isEmpty()) return;
            int qte = Integer.parseInt(input.trim());
            boolean success = stockService.ajouterStock(reference, qte);
            if (success) {
                resultatArea.append("✅ Stock ajouté à " + reference + " (+ " + qte + ")\n");
                chargerArticles();
            } else {
                resultatArea.append("❌ Échec de l'ajout de stock.\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void passerCommande() {
        try {
            String selection = (String) articleDropdown.getSelectedItem();
            if (selection == null) return;

            String[] parts = selection.split(" - ");

            // Toujours au début : référence
            String reference = parts[0].trim();

            // Le prix est toujours celui avant "Stock"
            String prixStr = parts[parts.length - 2].replace("€", "").trim();
            double prixUnitaire = Double.parseDouble(prixStr);

            // Quantité
            int quantite = Integer.parseInt(quantiteField.getText().trim());

            // Création de la commande
            Commande commande = new Commande(1); // ID fictif
            commande.ajouterLigne(new LigneCommande(reference, quantite, prixUnitaire));
            boolean success = stockService.enregistrerCommande(commande);

            if (success) {
                double total = prixUnitaire * quantite;
                resultatArea.append("✅ Commande passée pour " + quantite + " x " + reference + "\n");
                genererFacturePDF(reference, quantite, prixUnitaire, total);
                chargerArticles();
            } else {
                resultatArea.append("❌ Commande échouée : stock insuffisant ?\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de commande !");
        }
    }


    private void chargerArticles() {
        try {
            articleDropdown.removeAllItems();
            List<Article> articles = stockService.getArticles();
            for (Article article : articles) {
                articleDropdown.addItem(formatArticle(article));
            }
            resultatArea.append("📦 Articles chargés avec succès.\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatArticle(Article a) {
        return a.getReference() + " - " + a.getNomArticle() + " - " + a.getPrixUnitaire() + "€ - Stock: " + a.getStock();
    }

    private void genererFacturePDF(String reference, int quantite, double prixUnitaire, double total) {
        try {
            String nomFichier = "facture_" + reference + "_" + System.currentTimeMillis() + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(nomFichier));
            document.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font textFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12);

            Paragraph title = new Paragraph("TICKET DE CAISSE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Date : " + java.time.LocalDateTime.now(), textFont));
            document.add(new Paragraph("Article : " + reference, textFont));
            document.add(new Paragraph("Quantité : " + quantite, textFont));
            document.add(new Paragraph("Prix unitaire : " + prixUnitaire + "€", textFont));
            document.add(new Paragraph("TOTAL : " + total + "€", textFont));
            document.add(new Paragraph("Merci pour votre achat !", textFont));
            document.close();

            resultatArea.append("🧾 Facture PDF générée : " + nomFichier + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new ClientUI();
    }
}