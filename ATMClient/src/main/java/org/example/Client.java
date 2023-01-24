package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class Client
{
    Integer errorPercentage = 10;
    // initialize socket and input output streams
    private Socket socket = null;
    private DataInputStream userInput = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;

    public static String showMenu(DataInputStream userInput) throws IOException {
        System.out.println("Enter 1 to check balance | Enter 2 to add credit | Enter 3 to withdraw ");
//        System.out.println("Enter 2 to add credit: ");
//        System.out.println("Enter 3 to withdraw: ");
        System.out.println("Press q to exit.");

        String op = userInput.readLine();

        // Select Database Operation
        String request;
        if(Objects.equals(op, "1")) request = "b";
        else if(Objects.equals(op, "2")) request = "c";
        else if(Objects.equals(op, "3")) request = "d";
        else request = "q";

        return request;
    }
    public static String srandom(){
        int max = 100, min = 1;
        return String.valueOf((int) (Math.random()*(max-min+1)+min));
    }
    public Client(String address, int port, Connection conn) throws IOException, SQLException {

        // establish a connection
        socket = new Socket(address, port);
        System.out.println("Connected");
        // Input/Output Stream
        userInput = new DataInputStream(System.in);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());



        String request = "u", response = "";
        Integer userid = -1; Integer tid = 0;

        // keep reading until "q" "is input
        while (!request.equals("q"))
        {
            // -------- Username ---------
            if(request.startsWith("u")) {
                System.out.println("Enter Username: ");

                String username = userInput.readLine();
                request = "u " + username;
                out.writeUTF(request);

                response = in.readUTF();

                // Username found
                if(response.startsWith("200")){
                    // Response Format 200 USERID
                    userid = Integer.valueOf(response.split(" ", 2)[1]);
                    // read password
                    request = "p";
                }
                continue;
            }

            // -------- Password ---------
            else if (request.startsWith("p")){
                System.out.println("Enter password: ");

                // Request
                String password = userInput.readLine();
                request =  "p " + password;
                out.writeUTF(request);

                // Response
                response = in.readUTF();

                // Password matched
                if(response.startsWith("200")){
                    System.out.println("Logged in successfully!");

                    request = showMenu(userInput);

                }
                else{
                    System.out.println("Wrong Password! Try Again.");
                }
            }

            // -------- Check Balance ---------
            else if(request.startsWith("b")){
                request = "b";
                out.writeUTF(request);

                // Response
                response = in.readUTF();
                System.out.println("Your account balance is: " + response);

                // Menu
                request = showMenu(userInput);
            }

            // -------- Credit ---------
            else if(request.startsWith("c")){
                System.out.println("Enter amount");

                // User Input & Request Preparation
                String amount = userInput.readLine();
                int retransmission = 0;

                // Keep sending request until valid response is found
                while (true) {

                    // Request Format "c ErrorPercentage RandomValue TransactionId Amount"
                    request = "c " + String.valueOf(errorPercentage) + " " + srandom() + " " + tid + " " + amount;
                    out.writeUTF(request); // Sending Request
                    LocalDateTime oneSecondLaterOfSendingRequest = LocalDateTime.now().plusSeconds(1);

                    response = null;
                    while (LocalDateTime.now().isBefore(oneSecondLaterOfSendingRequest)) {
                        if (in.available() > 0) response = in.readUTF();
                        if (response != null) break;
                    }

                    // No Response
                    if (response == null) {
                        // Retransmit Request
                        retransmission = retransmission + 1;
                        continue;
                    }
                    // Valid Response
                    else {
                        // Expected Format "200 RandomSeed Balance Message"
                        int serverSeed = Integer.parseInt(response.split(" ", 4)[1]);

                        // If server random value is less than error percentage, then discard/retransmit request
                        if(serverSeed <= errorPercentage){
                            retransmission = retransmission + 1;
                            continue;
                        }

                        // Valid Response
                        else{
                            System.out.println(response.split(" ", 4)[3]);
                            System.out.println("Retransmission: " + retransmission);
                            System.out.println("Current Balance is: " + response.split(" ", 4)[2]);

                            // Increment Transaction
                            tid = tid + 1;
                            // Save Log
                            DB.saveLog(conn, errorPercentage, retransmission);
                            // Menu
                            request = showMenu(userInput);
                            break;
                        }
                    }

                }

            }

            // -------- Debit ---------
            else if(request.startsWith("d")){
                System.out.println("Enter amount");

                // User Input & Request Preparation
                String amount = userInput.readLine();
                int retransmission = 0;

                // Keep sending request until valid response is found
                while (true) {

                    // Request Format "c ErrorPercentage RandomValue TransactionId Amount"
                    request = "d " + String.valueOf(errorPercentage) + " " + srandom() + " " + tid + " " + amount;
                    out.writeUTF(request); // Sending Request
                    LocalDateTime oneSecondLaterOfSendingRequest = LocalDateTime.now().plusSeconds(1);

                    response = null;
                    while (LocalDateTime.now().isBefore(oneSecondLaterOfSendingRequest)) {
                        if (in.available() > 0) response = in.readUTF();
                        if (response != null) break;
                    }

                    // No Response
                    if (response == null) {
                        // Retransmit Request
                        retransmission = retransmission + 1;
                        continue;
                    }
                    // Valid Response
                    else {
                        // Expected Format "200 RandomSeed Balance Message"
                        int serverSeed = Integer.parseInt(response.split(" ", 4)[1]);

                        // If server random value is less than error percentage, then discard/retransmit request
                        if(serverSeed <= errorPercentage){
                            retransmission = retransmission + 1;
                            continue;
                        }

                        // Valid Response
                        else{
                            System.out.println(response.split(" ", 4)[3]);
                            System.out.println("Retransmission: " + retransmission);
                            System.out.println("Current Balance is: " + response.split(" ", 4)[2]);

                            // Increment Transaction
                            tid = tid + 1;
                            // Save Log
                            DB.saveLog(conn, errorPercentage, retransmission);
                            // Menu
                            request = showMenu(userInput);
                            break;
                        }
                    }

                }

            }

        }

        // close the connection
        userInput.close(); out.close(); socket.close();

    }
}