package org.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DB.connect();
        if(connection != null) {
            Client client = new Client("192.168.0.103", 5000, connection);
        }
        else System.out.println("Database connection Failed");
        connection.close();
    }
}