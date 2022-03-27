
/** 
* HTTP Server
* @author Shikky san
* @version 1.00
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class httpServer extends Thread {
    public static String content;

    static final String HTML_END = "</body>" +
            "</html>";

    Socket connectedClient = null;
    BufferedReader inFromClient = null;
    DataOutputStream outToClient = null;

    public httpServer(Socket client) {
        connectedClient = client;
        content = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader("test.html"));
            String str;
            while ((str = in.readLine()) != null) {
                content += str;
            }
            in.close();
        } catch (IOException e) {
            System.exit(0);
        }
    }

    public void run() {

        try {

            inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
            outToClient = new DataOutputStream(connectedClient.getOutputStream());

            String requestString = inFromClient.readLine(); //"GET / HTTP/1.1"
            String headerLine = requestString;
            StringTokenizer tokenizer = new StringTokenizer(headerLine);
            String httpMethod = tokenizer.nextToken();
            String httpQueryString = tokenizer.nextToken();
            StringBuffer responseBuffer = new StringBuffer();
            responseBuffer.append("<b> This is the HTTP Server Home Page.... </b><BR>");
            responseBuffer.append("The HTTP Client request is ....<BR>");

            while (inFromClient.ready()) {
                // Read the HTTP complete HTTP Query
                responseBuffer.append(requestString + "<BR>");
                requestString = inFromClient.readLine();
            }

            if (httpMethod.equals("GET")) {
                if (httpQueryString.equals("/")) {
                    // The default home page
                    sendResponse(200, responseBuffer.toString(), false);
                } else {
                    // This is interpreted as a file name
                    String fileName = httpQueryString.replaceFirst("/", "");
                    if (new File(fileName).isFile()) {
                        sendResponse(200, fileName, true);
                    } else {
                        sendResponse(404, "<b>The Requested resource not found ...." +
                                "Usage: http://127.0.0.1:6900 or http://127.0.0.1:6900/</b>", false);
                    }
                }
            } else
                sendResponse(404, "<b>The Requested resource not found ...." +
                        "Usage: http://127.0.0.1:6900 or http://127.0.0.1:6900/</b>", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResponse(int statusCode, String responseString, boolean isFile) throws Exception {

        String statusLine = null;
        String serverdetails = "Server: Java HTTPServer";
        String contentLengthLine = null;
        String fileName = null;
        String contentTypeLine = "Content-Type: text/html" + "\r\n";
        FileInputStream fin = null;

        if (statusCode == 200)
            statusLine = "HTTP/1.1 200 OK" + "\r\n";
        else
            statusLine = "HTTP/1.1 404 Not Found" + "\r\n";

        if (isFile) {
            fileName = responseString;
            fin = new FileInputStream(fileName);
            contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
            if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
                contentTypeLine = "Content-Type: \r\n";
        } else {
            responseString = httpServer.content + "<br>" + responseString + httpServer.HTML_END;
            contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
        }

        outToClient.writeBytes(statusLine);
        outToClient.writeBytes(serverdetails);
        outToClient.writeBytes(contentTypeLine);
        outToClient.writeBytes(contentLengthLine);
        outToClient.writeBytes("Connection: close\r\n");
        outToClient.writeBytes("\r\n");

        if (isFile)
            sendFile(fin, outToClient);
        else
            outToClient.writeBytes(responseString);

        outToClient.close();
    }

    public void sendFile(FileInputStream fin, DataOutputStream out) throws Exception {
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fin.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            fin.close();
        } catch (Exception e) {
            System.exit(0);
        }
    }

    public static void main(String args[]) throws Exception {
        try {
            try (ServerSocket Server = new ServerSocket(6900, 10, InetAddress.getByName("127.0.0.1"))) {
                System.out.println("TCPServer Waiting for client on port 6900");

                while (true) {
                    Socket connected = Server.accept();
                    new httpServer(connected).start();
                }
            }
        } catch (Exception e) {
            System.exit(0);
        }
    }
}