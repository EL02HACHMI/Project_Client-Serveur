package models;

import java.io.Serializable;

public class LigneCommande implements Serializable {
    private String reference;
    private int quantite;
    private double prixVente;

    public LigneCommande(String reference, int quantite, double prixVente) {
        this.reference = reference;
        this.quantite = quantite;
        this.prixVente = prixVente;
    }

    public String getReference() { return reference; }
    public int getQuantite() { return quantite; }
    public double getPrixVente() { return prixVente; }
}