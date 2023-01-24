package org.example;

import java.sql.*;
import java.util.Objects;

public class DB {
    public static Connection conn = null;

    public static Connection connect() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:db.sqlite");

        // GENERATING DATA
        if(conn != null){

//            String sql = null;
//            Statement sqlSt = conn.createStatement();

            // CLEAR DATABASE
//            sql = "DROP TABLE IF EXISTS Logs ;"; sqlSt.execute(sql);
//
//            // Log Table
//            sql = "CREATE TABLE Logs( " +
//                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    "errorPercentage INTEGER NOT NULL, " +
//                    "retransmission INTEGER NOT NULL " +
//                    ");";
//
//            sqlSt.execute(sql);
            return conn;
        }
        else throw new SQLException("Database Connection Failed");
    }

    public static void saveLog(Connection conn, Integer errorPercentage, Integer retransmission ) throws SQLException {
        Statement sqlSt = conn.createStatement();

        try {
            // New Transaction
            String sql = "INSERT INTO Logs (errorPercentage, retransmission) VALUES (" + errorPercentage + ", " + retransmission + ");";
            sqlSt.execute(sql);
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }


}
