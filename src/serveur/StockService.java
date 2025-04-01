package serveur;

import models.Article;
import models.Commande;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface StockService extends Remote {
    List<Article> getArticles() throws RemoteException;
    Article getArticleByReference(String reference) throws RemoteException;
    boolean passerCommande(int idCommande, String reference, int quantite) throws RemoteException;
    boolean enregistrerCommande(Commande commande) throws RemoteException;
    List<Article> getArticlesByFamille(String nomFamille) throws RemoteException;
    boolean ajouterStock(String reference, int quantite) throws RemoteException;

}
