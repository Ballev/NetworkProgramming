package network.programming.algorithm.aprioriRare.use;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {

        int port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running.");

            while (true) {
                Socket socket = serverSocket.accept();

                ServerThread t = new ServerThread(socket);
                t.start();
            }

        } catch (BindException e) {
            System.out.println("The server is already running.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
