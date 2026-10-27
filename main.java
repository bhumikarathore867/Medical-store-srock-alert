import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    static Map<String, Integer> stockMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Serve HTML File
        server.createContext("/", exchange -> {
            File file = new File("index.html");
            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });

        // ADD MEDICINE API
        server.createContext("/api/add", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Map<String,String> params = queryToMap(query);
            String name = params.get("name");
            String qtyStr = params.get("qty");
            if(name!=null && qtyStr!=null){
                int qty = Integer.parseInt(qtyStr);
                stockMap.put(name.toLowerCase(), qty);
                System.out.println("========== BACKEND ADD ==========");
                System.out.println("Added: " + name + " Qty: " + qty);
                System.out.println("Current Stock: " + stockMap);
                System.out.println("=================================\n");
            }
            String res = "Added Successfully";
            exchange.sendResponseHeaders(200, res.length());
            exchange.getResponseBody().write(res.getBytes());
            exchange.getResponseBody().close();
        });

        // SALE MEDICINE API
        server.createContext("/api/sale", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Map<String,String> params = queryToMap(query);
            String name = params.get("name");
            String qtyStr = params.get("qty");
            if(name!=null && qtyStr!=null){
                int saleQty = Integer.parseInt(qtyStr);
                String key = name.toLowerCase();
                if(stockMap.containsKey(key)){
                    int current = stockMap.get(key);
                    stockMap.put(key, current - saleQty);
                }
                System.out.println("========== BACKEND SALE ==========");
                System.out.println("Sold: " + name + " Qty: " + saleQty);
                System.out.println("Current Stock: " + stockMap);
                System.out.println("==================================\n");
            }
            String res = "Sale Updated";
            exchange.sendResponseHeaders(200, res.length());
            exchange.getResponseBody().write(res.getBytes());
            exchange.getResponseBody().close();
        });

        // DELETE MEDICINE API
        server.createContext("/api/delete", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Map<String,String> params = queryToMap(query);
            String name = params.get("name");
            if(name!=null){
                stockMap.remove(name.toLowerCase());
                System.out.println("========== BACKEND DELETE ==========");
                System.out.println("Deleted: " + name);
                System.out.println("Current Stock: " + stockMap);
                System.out.println("===================================\n");
            }
            String res = "Deleted";
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, res.length());
            exchange.getResponseBody().write(res.getBytes());
            exchange.getResponseBody().close();
        });

        server.start();
        System.out.println("Server Started on http://localhost:8000");
    }

    // Helper function to parse URL query
    public static Map<String,String> queryToMap(String query){
        Map<String,String> result = new ConcurrentHashMap<>();
        if(query == null) return result;
        for(String param : query.split("&")){
            String[] pair = param.split("=");
            if(pair.length==2){
                try{
                    result.put(pair[0], java.net.URLDecoder.decode(pair[1], "UTF-8"));
                } catch(Exception e){}
            }
        }
        return result;
    }
}