import java.io.*;
import java.net.Socket;

public class UserConnectionThread extends Thread {
    private final Socket socket;
    private final Server server;
    private PrintWriter writer;

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
            server.broadcast(serverMessage, this);

            String clientMessage;

            do {
                clientMessage = reader.readLine();

                //check if Message is statusUpdateRequest for sending counter
                //check if Message is empty, if not message is going to be broadcast
                if(clientMessage.equals("statusUpdateRequest")) {

                    //send usercounter and userlist
                    writer.println("USER_ONLINE: " + Server.counter);
                    writer.println("LIST_ONLINE: " + server.getUserNames());

                } else if(clientMessage.contains("CHANGEUSERNAME")) {

                    System.out.print(userName + " changed in ");
                    String currentUserName = userName;
                    userName = clientMessage.substring(clientMessage.lastIndexOf("CHANGEUSERNAME") + 14);
                    System.out.println(userName);
                    server.changeUsername(currentUserName, userName);


                } else if(!clientMessage.isEmpty() && !clientMessage.equals("bye") && !clientMessage.equals(userName)) {

                    System.out.println("[Server]: " + clientMessage);

                    server.broadcast(clientMessage, this);

                }

            } while (!clientMessage.equals("bye"));

            //client ends when "byebye" received
            server.whisper(this);

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