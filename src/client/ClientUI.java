package client;

import com.formdev.flatlaf.FlatDarkLaf;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import models.Article;
import models.Commande;
import models.LigneCommande;
import serveur.StockService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.io.FileOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientUI extends JFrame {
    private StockService stockService;
    private JComboBox<String> articleDropdown;
    private JTextField quantiteField;
    private JTextArea resultatArea;
    private JComboBox<String> comboFamilles;
    private Map<String, Integer> mapFamilles;
    private List<LigneCommande> panier;
    private DefaultTableModel panierModel;
    private JTable panierTable;
    private ButtonGroup paymentGroup;
    private JRadioButton cbButton, especeButton;

    public ClientUI() {
        setTitle("🛒 Boutique RMI - Client");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ignored) {}

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 5000);
            stockService = (StockService) registry.lookup("StockService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion au serveur !", "Erreur", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        JPanel sidebar = new JPanel(new GridLayout(0, 1, 10, 10));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        sidebar.setBackground(new Color(40, 40, 40));

        JTextField familleField = new JTextField();
        JButton btnRechercher = new JButton("🔍 Rechercher");
        JButton btnAfficherTous = new JButton("📋 Afficher tous");
        JButton btnReset = new JButton("🔄 Réinitialiser");
        JButton btnAjouterStock = new JButton("➕ Ajouter Stock");
        JButton btnChiffreAffaire = new JButton("💰 Chiffre d'affaires");
        sidebar.add(btnChiffreAffaire);
        sidebar.add(new JLabel("Famille :"));
        sidebar.add(familleField);
        sidebar.add(btnRechercher);
        sidebar.add(btnAfficherTous);
        sidebar.add(btnReset);
        sidebar.add(btnAjouterStock);

        comboFamilles = new JComboBox<>();
        mapFamilles = new HashMap<>();
        chargerFamilles();
        sidebar.add(new JLabel("📂 Familles disponibles :"));
        sidebar.add(comboFamilles);

        add(sidebar, BorderLayout.WEST);

        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBorder(new EmptyBorder(20, 20, 20, 20));

        articleDropdown = new JComboBox<>();
        quantiteField = new JTextField(5);
        JButton btnAjouterPanier = new JButton("➕ Ajouter au panier");
        JButton btnValiderPanier = new JButton("✅ Valider le panier");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Sélectionnez un article :"));
        topPanel.add(articleDropdown);

        JPanel quantitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantitePanel.add(new JLabel("Quantité :"));
        quantitePanel.add(quantiteField);
        quantitePanel.add(btnAjouterPanier);

        panierModel = new DefaultTableModel(new String[]{"Référence", "Nom", "Quantité", "Prix", "Total"}, 0);
        panierTable = new JTable(panierModel);
        JScrollPane panierScroll = new JScrollPane(panierTable);
        panierScroll.setPreferredSize(new Dimension(600, 120));

        cbButton = new JRadioButton("CB");
        especeButton = new JRadioButton("Espèce");
        paymentGroup = new ButtonGroup();
        paymentGroup.add(cbButton);
        paymentGroup.add(especeButton);

        JPanel paiementPanel = new JPanel();
        paiementPanel.add(new JLabel("Mode de paiement :"));
        paiementPanel.add(cbButton);
        paiementPanel.add(especeButton);
        paiementPanel.add(btnValiderPanier);

        centre.add(topPanel);
        centre.add(Box.createVerticalStrut(10));
        centre.add(quantitePanel);
        centre.add(Box.createVerticalStrut(10));
        centre.add(panierScroll);
        centre.add(Box.createVerticalStrut(10));
        centre.add(paiementPanel);

        add(centre, BorderLayout.CENTER);

        resultatArea = new JTextArea(10, 50);
        resultatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultatArea.setMargin(new Insets(10, 10, 10, 10));
        resultatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultatArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📜 Journal"));
        add(scrollPane, BorderLayout.SOUTH);

        panier = new ArrayList<>();
        chargerArticles();

        btnAjouterPanier.addActionListener(e -> ajouterAuPanier());
        btnValiderPanier.addActionListener(e -> validerPanier());
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
                } catch (Exception ignored) {}
            }
        });

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
        } catch (Exception ignored) {}
    }

    private void ajouterAuPanier() {
        try {
            String selection = (String) articleDropdown.getSelectedItem();
            if (selection == null) return;
            String[] parts = selection.split(" - ");
            String reference = parts[0].trim();
            String nom = parts[1].trim();
            double prix = Double.parseDouble(parts[2].replace("€", "").trim());
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            double total = prix * quantite;
            panier.add(new LigneCommande(reference, quantite, prix));
            panierModel.addRow(new Object[]{reference, nom, quantite, prix, total});
        } catch (Exception ignored) {}
    }

    private void validerPanier() {
        try {
            if (panier.isEmpty()) return;

            if (!cbButton.isSelected() && !especeButton.isSelected()) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un mode de paiement");
                return;
            }

            // 👉 Déterminer le mode de paiement
            String modePaiement = cbButton.isSelected() ? "Carte Bancaire" : "Espèce";

            Commande commande = new Commande(1);
            for (LigneCommande ligne : panier) commande.ajouterLigne(ligne);

            boolean success = stockService.enregistrerCommande(commande);
            if (success) {
                resultatArea.append("✔ Panier validé avec succès.\n");

                Map<String, String> nomsArticles = new HashMap<>();
                for (Article article : stockService.getArticles()) {
                    nomsArticles.put(article.getReference(), article.getNomArticle());
                }

                genererFacturePDF(commande, modePaiement, nomsArticles);
                panier.clear();
                panierModel.setRowCount(0);
                chargerArticles();
            } else {
                resultatArea.append("Échec de la commande (stock insuffisant ou erreur).\n");
            }

        } catch (Exception ignored) {}
    }


    private void rechercherParFamille(String input) {
        try {
            if (input.isEmpty()) return;
            List<Article> articles;
            String familleAffichee;
            try {
                int idFamille = Integer.parseInt(input);
                articles = stockService.getArticlesByFamilleId(idFamille);
                familleAffichee = stockService.getNomFamilleById(idFamille);
            } catch (NumberFormatException e) {
                articles = stockService.getArticlesByFamille(input);
                familleAffichee = input;
            }
            articleDropdown.removeAllItems();
            for (Article article : articles) {
                articleDropdown.addItem(formatArticle(article));
            }
            if (articles.isEmpty()) {
                resultatArea.append("❌ Aucun article trouvé pour la famille : " + familleAffichee + "\n");
            } else {
                resultatArea.append("🔍 Articles de la famille \"" + familleAffichee + "\" trouvés (" + articles.size() + ")\n");
            }
        } catch (Exception ignored) {}
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
        } catch (Exception ignored) {}
    }

    private void chargerArticles() {
        try {
            articleDropdown.removeAllItems();
            List<Article> articles = stockService.getArticles();
            for (Article article : articles) {
                articleDropdown.addItem(formatArticle(article));
            }
            resultatArea.append("📦 Articles chargés avec succès.\n");
        } catch (Exception ignored) {}
    }

    private String formatArticle(Article a) {
        return a.getReference() + " - " + a.getNomArticle() + " - " + a.getPrixUnitaire() + "€ - Stock: " + a.getStock();
    }

    private void genererFacturePDF(Commande commande, String modePaiement, Map<String, String> nomArticles) {
        try {
            String nomFichier = "facture_panier_" + System.currentTimeMillis() + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(nomFichier));
            document.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font subTitleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font textFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12);

            Paragraph title = new Paragraph("🧾 FACTURE - Boutique RMI", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Date : " + java.time.LocalDateTime.now(), textFont));
            document.add(new Paragraph("Commande N° : " + commande.getIdCommande(), textFont));
            document.add(new Paragraph("Mode de paiement : " + modePaiement, textFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("DÉTAILS DE LA COMMANDE :", subTitleFont));
            document.add(new Paragraph(" "));

            double totalGeneral = 0;

            for (LigneCommande ligne : commande.getLignes()) {
                double totalLigne = ligne.getPrixVente() * ligne.getQuantite();
                totalGeneral += totalLigne;

                String nomArticle = nomArticles.getOrDefault(ligne.getReference(), "Article inconnu");

                document.add(new Paragraph("▶ Article : " + nomArticle, textFont));
                document.add(new Paragraph("    Référence : " + ligne.getReference(), textFont));
                document.add(new Paragraph("    Quantité : " + ligne.getQuantite(), textFont));
                document.add(new Paragraph("    Prix unitaire : " + ligne.getPrixVente() + " €", textFont));
                document.add(new Paragraph("    Sous-total : " + totalLigne + " €", textFont));
                document.add(new Paragraph(" "));
            }

            document.add(new Paragraph("TOTAL À PAYER : " + totalGeneral + " €", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Merci pour votre achat et à bientôt !", textFont));
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
