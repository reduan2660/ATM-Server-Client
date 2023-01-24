package org.example;

import java.sql.*;
import java.util.Objects;

public class DB {
    public static Connection conn = null;

    public static Connection connect() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:db.sqlite");

        // GENERATING DATA
        if(conn != null){

            String sql = null;
            Statement sqlSt = conn.createStatement();

            // CLEAR DATABASE
            sql = "DROP TABLE Users ;";
            sqlSt.execute(sql);
            sql = "DROP TABLE Transactions ;";
            sqlSt.execute(sql);

            // User
            sql = "CREATE TABLE Users( " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username VARCHAR(255) NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "balance INTEGER NOT NULL" +
                    ");";

            sqlSt.execute(sql);

            sql = "INSERT INTO Users(username, password, balance) VALUES ('jawad', 'password', 100);"; sqlSt.execute(sql);
            sql = "INSERT INTO Users(username, password, balance) VALUES ('shoeb', 'password', 100);"; sqlSt.execute(sql);
            sql = "INSERT INTO Users(username, password, balance) VALUES ('alve', 'password', 100);"; sqlSt.execute(sql);

            // Transaction

            sql = "CREATE TABLE Transactions( " +
                    "tid INTEGER PRIMARY KEY, " +
                    "type CHAR(1) NOT NULL, " +
                    "amount INTEGER NOT NULL" +
                    ");";

            sqlSt.execute(sql);
            return conn;
        }
        else throw new SQLException("Database Connection Failed");
    }

    public static int isValidUser(Connection conn, String username) throws SQLException {
        Statement sqlSt = conn.createStatement();
        String sql = "SELECT id FROM Users WHERE username = '" + username + "';";

        try {
            ResultSet rs = sqlSt.executeQuery(sql);
            return rs.getInt("id");
        }
        catch (SQLException sqlException){
            return -1;
        }
    }

    public static boolean isPasswordMatched(Connection conn, String password, Integer userId) throws SQLException{
        Statement sqlSt = conn.createStatement();
        String sql = "SELECT password FROM Users WHERE id = " + userId + ";";

        try {
            ResultSet rs = sqlSt.executeQuery(sql);
            return Objects.equals(password, rs.getString("password"));
        }
        catch (SQLException sqlException){
            return false;
        }
    }

    public static int getBalance(Connection conn, Integer userId) throws SQLException {
        Statement sqlSt = conn.createStatement();
        String sql = "SELECT balance FROM Users WHERE id = " + userId + ";";

        try {
            ResultSet rs = sqlSt.executeQuery(sql);
            return rs.getInt("balance");
        }
        catch (SQLException sqlException){
            return -1;
        }
    }

    public static boolean isTransactionExists(Connection conn, Integer tid) throws SQLException{
        Statement sqlSt = conn.createStatement();
        String sql = "SELECT COUNT(tid) as tidCount FROM Transactions WHERE tid = " + tid + ";";

        try {
            ResultSet rs = sqlSt.executeQuery(sql);
            return rs.getInt("tidCount") > 0;
        }
        catch (SQLException sqlException){
            return false;
        }
    }

    public static void makeTransaction(Connection conn, Integer tid, char ttype, Integer uid, Integer amount) throws SQLException{
        Statement sqlSt = conn.createStatement();

        try {
            // New Transaction
            String sql = "INSERT INTO Transactions (tid, type, amount) VALUES (" + tid + ", '" + ttype + "', " + amount + ");";
            sqlSt.execute(sql);

            int currentBalance = getBalance(conn, uid);
            int newBalance = currentBalance + amount;

            // Update User
            sql = "UPDATE Users SET balance = " + newBalance + " WHERE id = " + uid + " ;";
            sqlSt.execute(sql);
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }

    public static void makeDebitTransaction(Connection conn, Integer tid, char ttype, Integer uid, Integer amount) throws SQLException{
        Statement sqlSt = conn.createStatement();

        try {
            // New Transaction
            String sql = "INSERT INTO Transactions (tid, type, amount) VALUES (" + tid + ", '" + ttype + "', " + amount + ");";
            sqlSt.execute(sql);

            int currentBalance = getBalance(conn, uid);
            int newBalance = currentBalance - amount;

            // Update User
            sql = "UPDATE Users SET balance = " + newBalance + " WHERE id = " + uid + " ;";
            sqlSt.execute(sql);
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }


}
