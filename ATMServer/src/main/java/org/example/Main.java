package org.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {


    public static void main(String args[]) throws SQLException, IOException {
        Connection connection = DB.connect();
        if(connection != null){
            ATMServer server = new ATMServer(5000, connection);
        }
        else System.out.println("Database connection Failed");
        connection.close();
    }
}