package mains;

import webservice.HttpServer;

/**
 * Created by samuelsmith on 05/03/15.
 */
public class TempRunner {

    public static void main(String[] args) {
        HttpServer server = new HttpServer(8088);
        System.out.println("Server created");
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Server running");
    }

}
