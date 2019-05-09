package network.programming.algorithm.aprioriRare.use;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {

    private Socket s;

    private final static int BUFFER_SIZE = 1024;

    public Client() {
        this.s = new Socket();
    }

    /**
     * Connects to the server
     *
     * @param host server's host
     * @param port server's port
     * @throws IOException {@link IOException}
     */
    public void connect(String host, int port) throws IOException {
        SocketAddress address = new InetSocketAddress(host, port);
        this.s.connect(address);

        System.out.println("You connected to the server");

        try (OutputStream os = s.getOutputStream();
             DataOutputStream dos = new DataOutputStream(os);
             Scanner sc = new Scanner(System.in);
             BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
             DataInputStream clientData = new DataInputStream(s.getInputStream())) {

            File file = enterValidFileName(sc);
            if (file == null) {

                return;
            }
            float threshold = enterValidThreshold(sc);

            sendFile(file, threshold, os, dos);

            listen(br);

            receiveFile(clientData, file.getName());

        } finally {

            closeConnection();
        }
    }

    /**
     * Closes the socket
     *
     * @throws IOException {@link IOException}
     */
    private void closeConnection() throws IOException {

        if (this.s != null) {

            this.s.close();
        }
    }

    /**
     * Sends the given file and threshold to the server
     *
     * @param file      the file to be sent
     * @param threshold the threshold
     * @param os        the server OutputStream {@link OutputStream}
     * @param dos       the server DataOutputStream{@link DataOutputStream}
     */
    void sendFile(File file, float threshold, OutputStream os, DataOutputStream dos) {
        byte[] mybytearray = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             FileInputStream in = new FileInputStream(file);) {

            bis.read(mybytearray, 0, mybytearray.length);

            dos.writeFloat(threshold);
            dos.writeUTF(file.getName());
            dos.writeLong(mybytearray.length);
            dos.flush();

            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
        } catch (IOException e) {
            System.out.println("Error occurred while sending the input file.");
        }
    }

    /**
     * Receives the result file from the server and saves it locally.
     *
     * @param clientData the data that is sent from the server
     * @param fileName   the name of the file that was processed by the algorithm
     */
    void receiveFile(DataInputStream clientData, String fileName) {
        int bytesRead;
        try {
            String resultFileName = "result_" + fileName;
            System.out.println("\nThe result file is received: " + resultFileName);
            long size = clientData.readLong();
            byte[] buffer = new byte[BUFFER_SIZE];
            try (OutputStream output = new FileOutputStream(resultFileName)) {
                while (size > 0
                        && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                    output.write(buffer, 0, bytesRead);
                    size -= bytesRead;
                }
            }
        } catch (IOException e) {
            System.out.println("Error occurred while receiving the result file.");
            e.getStackTrace();
        }
    }

    /**
     * Prints the results that are sent from the server.
     *
     * @param br {@link BufferedReader}
     * @throws IOException
     */
    void listen(BufferedReader br) throws IOException {
        String line = null;
        while (!(line = br.readLine()).equals("end")) {
            System.out.println(line);
        }
    }

    /**
     * Returns existing file.
     *
     * @param sc {@link Scanner}
     * @return valid file
     * @throws IOException {@link IOException}
     */
    private File enterValidFileName(Scanner sc) throws IOException {
        int maxAttempts = 3;
        do {
            System.out.print("Enter the name of the input file: ");
            String filename = sc.nextLine();
            File file = new File(filename);
            if (file.exists()){// && Files.probeContentType(file.toPath()).equals("text/plain")) {
                return file;
            }
            System.out.println("There is no file with that name!");
        } while (--maxAttempts > 0);
        System.out.println("You have reached the maximum number of attempts.");
        return null;
    }

    /**
     * Returns threshold that is a floating number between 0.0 and 1.0
     *
     * @param sc {@link Scanner}
     * @return valid threshold
     */
    private float enterValidThreshold(Scanner sc) {
        int maxAttempts = 3;
        do {
            System.out.print("Enter the threshold: ");
            try {
                float threshold = sc.nextFloat();
                if (threshold < 0.0 || threshold > 1.0) {
                    throw new InputMismatchException();
                }
                return threshold;
            } catch (InputMismatchException e) {
                System.out.println("The threshold must be a floating number between 0.0 and 1.0");
                sc.next();
            }
        } while (--maxAttempts > 0);
        System.out.println("The default value of 1.0 will be assigned to the threshold.");
        return 1.0f;
    }

    public static void main(String[] args) {

        try {

            Client c = new Client();

            String host = "127.0.0.1";
            int port = 8080;
            c.connect(host, port);

        } catch (IOException e) {

            System.out.println("Error occurred.");
        }
    }
}
