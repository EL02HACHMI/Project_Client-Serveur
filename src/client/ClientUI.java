package client;

import com.formdev.flatlaf.FlatDarkLaf;
import models.Article;
import models.Commande;
import models.LigneCommande;
import serveur.StockService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Font;
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

    public ClientUI() {
        setTitle("🛒 Boutique RMI - Client");
        setSize(900, 600);
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

        JPanel sidebar = new JPanel(new GridLayout(0, 1, 10, 10));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        sidebar.setBackground(new Color(40, 40, 40));
        JTextField familleField = new JTextField();
        JButton btnRechercher = new JButton("🔍 Rechercher");
        JButton btnAfficherTous = new JButton("📋 Afficher tous");
        JButton btnReset = new JButton("🔄 Réinitialiser");
        JButton btnAjouterStock = new JButton("➕ Ajouter Stock");

        sidebar.add(new JLabel("Famille :"));
        sidebar.add(familleField);
        sidebar.add(btnRechercher);
        sidebar.add(btnAfficherTous);
        sidebar.add(btnReset);
        sidebar.add(btnAjouterStock);

        add(sidebar, BorderLayout.WEST);

        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBorder(new EmptyBorder(20, 20, 20, 20));

        articleDropdown = new JComboBox<>();
        quantiteField = new JTextField(5);
        JButton btnAcheter = new JButton("🛒 Acheter");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Sélectionnez un article :"));
        topPanel.add(articleDropdown);

        JPanel quantitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantitePanel.add(new JLabel("Quantité :"));
        quantitePanel.add(quantiteField);
        quantitePanel.add(btnAcheter);

        centre.add(topPanel);
        centre.add(Box.createVerticalStrut(10));
        centre.add(quantitePanel);

        add(centre, BorderLayout.CENTER);

        resultatArea = new JTextArea(12, 50);
        resultatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultatArea.setMargin(new Insets(10, 10, 10, 10));
        resultatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultatArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📜 Journal"));

        add(scrollPane, BorderLayout.SOUTH);

        chargerArticles();

        btnAcheter.addActionListener(e -> passerCommande());
        btnRechercher.addActionListener(e -> rechercherParFamille(familleField.getText().trim()));
        btnAfficherTous.addActionListener(e -> chargerArticles());
        btnReset.addActionListener(e -> {
            familleField.setText("");
            chargerArticles();
            resultatArea.append("🔁 Réinitialisation terminée.\n");
        });
        btnAjouterStock.addActionListener(e -> ajouterStock());

        setVisible(true);
    }

    private void rechercherParFamille(String nomFamille) {
        try {
            if (nomFamille.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Entrez une famille à rechercher !");
                return;
            }
            List<Article> articles = stockService.getArticlesByFamille(nomFamille);
            articleDropdown.removeAllItems();
            for (Article article : articles) {
                articleDropdown.addItem(formatArticle(article));
            }
            resultatArea.append("🔍 Articles de la famille '" + nomFamille + "' affichés.\n");
        } catch (Exception e) {
            e.printStackTrace();
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
            String reference = selection.split(" - ")[0];
            int quantite = Integer.parseInt(quantiteField.getText());
            double prixUnitaire = Double.parseDouble(selection.split(" - ")[1].replace("€", "").trim());

            Commande commande = new Commande(1);
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
        return a.getReference() + " - " + a.getPrixUnitaire() + "€ - Stock: " + a.getStock();
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