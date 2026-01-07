// Author: Steven Carr
// Last Edited: October 2023
// A program representing the client side of a server-client relationship.  The client is looking to download a file
// from the server.

import java.io.*;
import java.net.*;

public class HW1Client {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println(
                    "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        // Get the hostname and the port for the server
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
                // Initialize the client socket with the host name and the port number
                Socket clientSocket = new Socket(hostName, portNumber);
                // Initialize a printwriter from IO that gets the output stream from the client socket and is set to
                // auto-flush
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                // Initialize the buffer reader with an input stream reader, reading the input stream via the
                // client socket
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));
                // Same but reading the input stream from standard input system.in
                BufferedReader stdIn =
                        new BufferedReader(
                                new InputStreamReader(System.in))
        )
        {
            // String for the GET request from the client to the server
            String userInput;

            // Take the input, and output it's content to a local file (the input of client is linked to the output
            // of the server, and the input of the server is linked to the output of the client
            File localfile = new File("../newLocalFile.html");
            FileOutputStream downloadData = new FileOutputStream(localfile, true);

            // Read the line from the client input (should be the GET request) and output it to the server
            userInput = stdIn.readLine();
            out.println(userInput);

            // Initialize a variable for reading the response from the sever (the download file)
            String serverOutput = in.readLine();

            // While there is still a response from the server
            while (serverOutput != null) {
                // Put the contents of the string line into a character array
                char[] chArray = serverOutput.toCharArray();

                // For the length of the character array
                for (int i = 0; i < chArray.length; i++){
                    // Output the char array to a file
                    downloadData.write(chArray[i]);
                }

                // Write a new line separation to the file after each line
                downloadData.write('\n');

                // Read the next line of input from the server before looping back
                serverOutput = in.readLine();
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }
    }
}