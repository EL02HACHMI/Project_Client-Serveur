package tests;

import serveur.StockServiceImpl;

public class TestStockService {
    public static void main(String[] args) {
        try {
            StockServiceImpl service = new StockServiceImpl();
            System.out.println("✅ Liste des articles : " + service.getArticles());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
