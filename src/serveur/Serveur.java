package serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class Serveur {
    public static void main(String[] args) {
        try {
            // Démarrage du registre RMI sur le port 1099
            Registry registry = LocateRegistry.createRegistry(5000);

            // Création et enregistrement du service RMI
            StockService stockService = new StockServiceImpl();
            registry.rebind("StockService", stockService);

            System.out.println("✅ Serveur RMI démarré avec succès !");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
