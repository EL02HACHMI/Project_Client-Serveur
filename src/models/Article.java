package models;

import java.io.Serializable;

public class Article implements Serializable { // Serializable pour être transmis via RMI
    private String reference;
    private int idFamille;
    private double prixUnitaire;
    private int stock;

    // 🔹 Constructeur avec tous les paramètres
    public Article(String reference, int idFamille, double prixUnitaire, int stock) {
        this.reference = reference;
        this.idFamille = idFamille;
        this.prixUnitaire = prixUnitaire;
        this.stock = stock;
    }

    @Override
    public String toString() {
        return reference + " - " + prixUnitaire + "€ - Stock: " + stock;
    }


    // 🔹 Getters et Setters
    public String getReference() { return reference; }
    public int getIdFamille() { return idFamille; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public int getStock() { return stock; }

    public void setReference(String reference) { this.reference = reference; }
    public void setIdFamille(int idFamille) { this.idFamille = idFamille; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public void setStock(int stock) { this.stock = stock; }
}
