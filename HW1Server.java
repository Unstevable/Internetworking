// Author: Steven Carr
// Last Edited: October 2023
// A program representing the server side of a client-server relationship.  This server program makes a HTTP request
// to the given website/file to download the file.

import java.net.*;
import java.io.*;
import java.util.Objects;

public class HW1Server {
    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }

        // Get the port number for the server
        int portNumber = Integer.parseInt(args[0]);

        try{
            // Create a server socket with the designated port number
            ServerSocket serverSocket =
                    new ServerSocket(portNumber);


            while(true){
                // Create a socket to the client using the server socket
                Socket clientSocket = serverSocket.accept();

                // Create a ClientWorker instance using the client socket and start a thread with the client
                ClientWorker w=new ClientWorker(clientSocket);
                Thread t=new Thread(w);
                t.start();
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
// Client class
class ClientWorker implements Runnable {
    private Socket client;

    //Constructor
    ClientWorker(Socket client) {
        this.client = client;
    }

    // Function to get the proper domain name of the website we're trying to access for the file
    // with this, NEEDS http:// beforehand
    String getDomainName(String URL) throws URISyntaxException {
        URI uri = new URI(URL);
        String domainName = uri.getHost();
        return domainName.startsWith("www.") ? domainName.substring(4) : domainName;
    }

    public void run() {
        // Initialize the line being read from the client, the input and the output of this server to null
        String line = null;
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            // Print the IP Address of the client
            System.out.println("Client IP Address: " + client.getInetAddress());

            // The input of the server is linked to the output of the client, and the output of the server is linked to
            // the input of the client
            in = new BufferedReader(new
                    InputStreamReader(client.getInputStream()));
            out = new
                    PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("in or out failed");
            System.exit(-1);
        }

        // Initialize these variables before the while loop
        // Remote Server variable
        Socket remoteServer;
        // Input and output for the remote server
        BufferedReader remoteIn = null;
        PrintWriter remoteOut = null;
        // Separating the input line from the client into substrings
        String[] separator = new String[2];
        // The remote server output (this server's input)
        String remoteLine = null;

        while (true) {
            try {
                // Read the incoming input from the client as a line
                line = in.readLine();

                // Separate the line into strings using any type of found whitespace as the means of separation
                separator = line.split("\\s");

            } catch (IOException e) {
                if (client.isClosed()){
                    // If the client is already closed, simply end the program (this prevents a "Read Failed" error
                    // even when the program is operating correctly)
                    System.exit(1);
                }
                System.out.println("Read failed");
                System.exit(-1);
            }
            try {
                // Establish the connection to the remote server
                remoteServer = new Socket(this.getDomainName(separator[1]), 80);

                // With the input and output from the server
                remoteIn = new BufferedReader(new InputStreamReader(remoteServer.getInputStream()));
                remoteOut = new PrintWriter(remoteServer.getOutputStream(), true);

                // First line must be the GET request, second line the HOST, and MUST have a new line separation
                // All in one line to avoid Linux Google Server Bad Request (with \r's included)
                remoteOut.println("GET " + separator[1] + " HTTP/1.1\r\n" + "HOST: " +
                        getDomainName(separator[1])+"\r\n\r\n");

            } catch (IOException e){
                System.out.println("Remote server read failed.");
                System.exit(-1);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            try {
                // Read a line from the remote server
                remoteLine = remoteIn.readLine();

                // Initialized a variable to keep track of the amount of loops accomplished, to get rid of
                // server response lines before the actual contents of the file
                int loopTracker = 1;

                // While remote server is still outputting data
                while (remoteLine != null){
                    // If the looptracker is greater than 9, then start outputting the lines of data from the server.
                    // Through trial and error I saw that after 9 lines we start to receive the actual file contents.
                    if (loopTracker > 9){
                        // Output what we receive from the remote server to the client
                        out.println(remoteLine);
                    }
                    // Increment the loop tracker
                    loopTracker++;

                    // Read the next line from the remote server before looping back
                    remoteLine = remoteIn.readLine();
                }
                // Terminate the program so it doesn't run forever
                client.close();
            } catch (IOException e) {
                System.out.println("Remote Server Read Failed.");
                System.exit(-1);
            }
        }
    }
}