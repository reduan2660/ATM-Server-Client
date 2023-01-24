package org.example;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

public class ATMServer {
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    public static String srandom(){
        int max = 100, min = 1;
        return String.valueOf((int) (Math.random()*(max-min+1)+min));
    }

    public ATMServer(int port, Connection conn) throws IOException, SQLException{

        // ----------------- Connection Setup ----------------------------
        server = new ServerSocket(port);
        System.out.println("Server Started");

        // Client Connection
        System.out.println("SERVER: Waiting for client.");
        socket = server.accept();
        System.out.println("SERVER: Client accepted!");

        // Input, Output Buffer
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());

        String request = "", response;
        Integer userId = -1;

        while (!request.equals("q")){
            request = in.readUTF();
            System.out.println("CLIENT: " + request);

            // ----------------- Username ----------------------------
            if(request.startsWith("u")){
                String username = request.split(" ",2)[1];

                userId = DB.isValidUser(conn, username);
                if(userId != -1)    response = "200 " + userId;
                else                response = "401";

                // Response
                out.writeUTF(response);
                System.out.println("SERVER: " + response);
            }

            // ----------------- Password ----------------------------
            else if(request.startsWith("p")){
                String password = request.split(" ",2)[1];

                boolean passwordMatched = DB.isPasswordMatched(conn, password, userId);
                if(passwordMatched)    response = "200";
                else                response = "401";

                // Response
                out.writeUTF(response);
                System.out.println("SERVER: " + response);
            }

            // ----------------- Check Balance -----------------------
            else if(request.startsWith("b")){
                response = String.valueOf(DB.getBalance(conn, userId));

                // Response
                out.writeUTF(response);
                System.out.println("SERVER: " + response);
            }

            // ----------------- Credit ------------------------------
            else if(request.startsWith("c")){
                // Expected Format "c ErrorPercentage RandomValue TransactionId Amount"

                int errorPercentage = Integer.parseInt(request.split(" ", 5)[1]);
                int randomValue = Integer.parseInt(request.split(" ", 5)[2]);
                int tid = Integer.parseInt(request.split(" ", 5)[3]);
                int amount = Integer.parseInt(request.split(" ", 5)[4]);

                // If random value is less than error percentage, then discard
                if(randomValue<=errorPercentage){
                    // no response
                    continue;
                }
                else{
                    boolean transactionExists = DB.isTransactionExists(conn, tid);

                    // Skip Transaction
                    if(transactionExists){
                        response = "200 " + srandom() + " " + DB.getBalance(conn, userId) + " Transaction Already Happened.";
                    }

                    // Make Transaction
                    else{
                        DB.makeTransaction(conn, tid, 'c', userId, amount);
                        response = "200 " + srandom() + " " + DB.getBalance(conn, userId) + " Transaction Successful.";
                    }

                    // Response
                    out.writeUTF(response);
                    System.out.println("SERVER: " + response);
                }
            }

            // ----------------- Debit ------------------------------
            else if(request.startsWith("d")){
                // Expected Format "c ErrorPercentage RandomValue TransactionId Amount"

                int errorPercentage = Integer.parseInt(request.split(" ", 5)[1]);
                int randomValue = Integer.parseInt(request.split(" ", 5)[2]);
                int tid = Integer.parseInt(request.split(" ", 5)[3]);
                int amount = Integer.parseInt(request.split(" ", 5)[4]);

                // If random value is less than error percentage, then discard
                if(randomValue<=errorPercentage){
                    // no response
                    continue;
                }
                else{
                    boolean transactionExists = DB.isTransactionExists(conn, tid);

                    // Skip Transaction
                    if(transactionExists){
                        response = "200 " + srandom() + " " + DB.getBalance(conn, userId) + " Transaction Already Happened.";
                    }

                    // Make Debit Transaction
                    else{
                        if(DB.getBalance(conn, userId) - amount < 0){
                            response = "200 " + srandom() + " " + DB.getBalance(conn, userId) + " Transaction failed. Insufficient fund.";
                        }
                        else {
                            DB.makeDebitTransaction(conn, tid, 'c', userId, amount);
                            response = "200 " + srandom() + " " + DB.getBalance(conn, userId) + " Transaction Successful.";
                        }
                    }

                    // Response
                    out.writeUTF(response);
                    System.out.println("SERVER: " + response);
                }
            }
        }

        // Close Connection
        socket.close(); in.close(); out.close();
    }
}
