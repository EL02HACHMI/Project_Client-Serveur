package models;

import java.io.Serializable;

public class Article implements Serializable {
    private String reference;
    private String nomArticle;
    private int idFamille;
    private double prixUnitaire;
    private int stock;

    public Article(String reference, String nomArticle, int idFamille, double prixUnitaire, int stock) {
        this.reference = reference;
        this.nomArticle = nomArticle;
        this.idFamille = idFamille;
        this.prixUnitaire = prixUnitaire;
        this.stock = stock;
    }

    @Override
    public String toString() {
        return reference + " - " + prixUnitaire + "€ - Stock: " + stock;
    }

    public String getReference() {
        return reference;
    }

    public String getNomArticle() {
        return nomArticle;
    }

    public int getIdFamille() {
        return idFamille;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public int getStock() {
        return stock;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setNomArticle(String nomArticle) {
        this.nomArticle = nomArticle;
    }

    public void setIdFamille(int idFamille) {
        this.idFamille = idFamille;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
