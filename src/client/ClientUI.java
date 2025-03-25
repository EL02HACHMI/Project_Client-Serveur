package client;
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

        // Connexion au serveur RMI
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 5000);
            stockService = (StockService) registry.lookup("StockService");
            System.out.println("✅ Connexion au serveur RMI réussie !");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Erreur de connexion au serveur !", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // UI Layout
        setLayout(new BorderLayout());

        // Panel du haut : Liste des articles
        JPanel panelHaut = new JPanel();
        panelHaut.add(new JLabel("Sélectionnez un article :"));
        articleDropdown = new JComboBox<>();
        panelHaut.add(articleDropdown);
        add(panelHaut, BorderLayout.NORTH);

        // Panel central : Quantité et bouton acheter
        JPanel panelCentre = new JPanel();
        panelCentre.add(new JLabel("Quantité :"));
        quantiteField = new JTextField(5);
        panelCentre.add(quantiteField);
        acheterButton = new JButton("Acheter");
        panelCentre.add(acheterButton);
        add(panelCentre, BorderLayout.CENTER);

        // Panel du bas : Zone de résultat
        resultatArea = new JTextArea(10, 40);
        resultatArea.setEditable(false);
        add(new JScrollPane(resultatArea), BorderLayout.SOUTH);

        // Charger la liste des articles
        chargerArticles();

        // Action du bouton Acheter
        acheterButton.addActionListener(e -> passerCommande());

        setVisible(true);
    }




    private void genererFacture(String reference, int quantite, double prixUnitaire, double total) {
        try {
            String nomFichier = "facture_" + reference + "_" + System.currentTimeMillis() + ".txt";
            String contenu = """
                ================================
                     TICKET DE CAISSE
                ================================
                Date         : %s
                Article      : %s
                Quantité     : %d
                Prix unitaire: %.2f €
                -------------------------------
                TOTAL        : %.2f €
                Mode de paiement : Carte Bancaire
                ================================
                Merci pour votre achat !
                """.formatted(
                    java.time.LocalDateTime.now(),
                    reference,
                    quantite,
                    prixUnitaire,
                    total
            );

            java.nio.file.Files.writeString(java.nio.file.Path.of(nomFichier), contenu);
            resultatArea.append("🧾 Facture générée : " + nomFichier + "\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Erreur lors de la génération de la facture !", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Charger les articles depuis le serveur RMI
    private void chargerArticles() {
        try {
            List<Article> articles = stockService.getArticles();
            for (Article article : articles) {
                articleDropdown.addItem(article.getReference() + " - " + article.getPrixUnitaire() + "€ - Stock: " + article.getStock());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Erreur lors du chargement des articles !", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Passer une commande
    private void passerCommande() {
        try {
            String selection = (String) articleDropdown.getSelectedItem();
            if (selection == null) {
                JOptionPane.showMessageDialog(this, "❌ Aucun article sélectionné !", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String reference = selection.split(" - ")[0];
            int quantite = Integer.parseInt(quantiteField.getText());

            // Vérification de la quantité
            if (quantite <= 0) {
                JOptionPane.showMessageDialog(this, "❌ Veuillez entrer une quantité valide !", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Passer la commande via RMI
            boolean success = stockService.passerCommande(1, reference, quantite);
            if (success) {
                // On extrait le prix depuis la description du ComboBox
                String[] parts = selection.split(" - ");
                double prixUnitaire = Double.parseDouble(parts[1].replace("€", "").trim());
                double total = prixUnitaire * quantite;

                resultatArea.append("✅ Commande réussie pour " + quantite + " x " + reference + "\n");
                genererFacturePDF(reference, quantite, prixUnitaire, total);
                chargerArticles(); // Mise à jour du stock
            }
            else {
                resultatArea.append("❌ Commande échouée pour " + reference + " (stock insuffisant ou erreur)\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Erreur lors du passage de la commande !", "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void genererFacturePDF(String reference, int quantite, double prixUnitaire, double total) {
        try {
            String nomFichier = "facture_" + reference + "_" + System.currentTimeMillis() + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(nomFichier));
            document.open();

            // Titre
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("TICKET DE CAISSE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // ligne vide

            // Corps
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


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientUI::new);
    }
}
