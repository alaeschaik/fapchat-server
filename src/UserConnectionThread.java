import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UserConnectionThread extends Thread {
    private final Socket socket;
    private final Server server;
    private PrintWriter writer;
    private String chat = "";
    private static List<String> messageList = new ArrayList<>();

    public UserConnectionThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            printUsers();

            String userName = reader.readLine();
            server.addUserName(userName);

            String serverMessage = "[Server]: New user connected: " + userName;
            //writer.println(messageList);
            server.broadcast(serverMessage, this);

            String clientMessage;

            do {
                clientMessage = reader.readLine();

                //check if Message is empty, if not message is going to be broadcast
                if(!clientMessage.isEmpty() && clientMessage != null) {
                    messageList.add(clientMessage);

                    System.out.println("[Server]: " + clientMessage);

                    server.broadcast(clientMessage, this);
                }

            } while (!clientMessage.equals("bye"));

            server.removeUser(userName, this);
            socket.close();

            serverMessage = "[Server]: " + userName + " has quitted.";
            server.broadcast(serverMessage, this);

        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Sends a list of online users to the newly connected user.
     */
    void printUsers() {
        if (server.hasUsers()) {
            writer.println("[Server]: Connected users: " + server.getUserNames());
        } else {
            writer.println("[Server]: No other users connected");
        }
    }

    /**
     * Sends a message to the client.
     */
    void sendMessage(String message) {
        writer.println(message);
    }
}