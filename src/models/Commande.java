package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Commande implements Serializable {
    private int idCommande;
    private LocalDate dateCommande;
    private List<LigneCommande> lignes;

    public Commande(int idCommande) {
        this.idCommande = idCommande;
        this.dateCommande = LocalDate.now();
        this.lignes = new ArrayList<>();
    }

    public void ajouterLigne(LigneCommande ligne) {
        lignes.add(ligne);
    }

    public int getIdCommande() { return idCommande; }
    public LocalDate getDateCommande() { return dateCommande; }
    public List<LigneCommande> getLignes() { return lignes; }

    public double getTotal() {
        return lignes.stream().mapToDouble(LigneCommande::getPrixVente).sum();
    }
}
