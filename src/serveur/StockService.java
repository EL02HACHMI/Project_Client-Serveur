package serveur;

import models.Article;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface StockService extends Remote {
    List<Article> getArticles() throws RemoteException;
    Article getArticleByReference(String reference) throws RemoteException;
    boolean passerCommande(int idCommande, String reference, int quantite, double prixVente) throws RemoteException;
}
