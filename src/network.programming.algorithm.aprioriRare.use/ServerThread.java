package network.programming.algorithm.aprioriRare.use;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import ca.pfv.spmf.algorithms.frequentpatterns.AlgoAprioriRare;

public class ServerThread extends Thread {
    public static Socket socket;
    InputStream in;
    OutputStream out;
    String fileName = "";
    String threadName = this.getName();
    double threshold = 0;

    private final static int BUFFER_SIZE = 1024;
    final String FORMATTED_INPUT = threadName + "-formatted.txt"; // the name of the file for the algorithms input
    final String OUTPUT = threadName + "-output.txt"; // the name of the file for saving the erasable itemsets found

    public ServerThread(Socket s) {
        socket = s;
        try {
            in = s.getInputStream();
            out = s.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try (DataInputStream clientData = new DataInputStream(in);
             DataOutputStream dos = new DataOutputStream(out);
             PrintWriter writer = new PrintWriter(out, true)) {

            receiveFile(clientData);
            DataFormatter.formatInputData(fileName, FORMATTED_INPUT);
            runAprioriRareArg();
            sendResult(writer, OUTPUT);
            sendResultFile(out, dos);

        } catch (IOException e) {
            closeConnection();
        } finally {
            //clean();
            closeConnection();
        }
    }

    /**
     * Receives the initial file from the client
     *
     * @param clientData the client's DataInputStream {@link DataInputStream}
     * @throws IOException {link IOException}
     */
    void receiveFile(DataInputStream clientData) throws IOException {
        int bytesRead;
        try {
            threshold = clientData.readFloat();
            fileName = clientData.readUTF();
            System.out.println("The server received file " + fileName);
            long size = clientData.readLong();
            byte[] buffer = new byte[BUFFER_SIZE];
            try (OutputStream output = new FileOutputStream(fileName)) {
                while (size > 0
                        && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                    output.write(buffer, 0, bytesRead);
                    size -= bytesRead;
                }
            }
        } catch (IOException e) {
            System.out.println("Error occurred while receiving the file " + fileName);
        }
    }

    /**
     * Runs the AprioriRare algorithm
     *
     * @throws NumberFormatException {@link NumberFormatException}
     * @throws IOException           {@link IOException}
     */
    void runAprioriRareArg() throws NumberFormatException, IOException {
        AlgoAprioriRare algo = new AlgoAprioriRare();
        algo.runAlgorithm(threshold, FORMATTED_INPUT, OUTPUT);
    }

    /**
     * Sends the results to the client
     *
     * @param writer         client's PrintWriter
     * @param outputFileName the name of the result file
     */
    void sendResult(PrintWriter writer, String outputFileName) {
        try {
            writer.println("\n\nThe result of the algorithm on file " + fileName + " with threshold "
                    + threshold * 100 + "% is:\n");
            DataFormatter.formatResult(writer, outputFileName);
            writer.println("end");
        } catch (IOException e) {
            System.out.println("Error occurred while sending results.");
        }
    }

    /**
     * Sends the result file to the client
     *
     * @param os  client's OutputStream {@link OutputStream}
     * @param dos client's {@link DataOutputStream}
     */
    void sendResultFile(OutputStream os, DataOutputStream dos) {
        File file = new File(OUTPUT);
        byte[] mybytearray = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis);) {
            bis.read(mybytearray, 0, mybytearray.length);

            dos.writeLong(mybytearray.length);
            dos.flush();

            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
        } catch (IOException e) {
            System.out.println("Error occurred while sending the result file result_" + fileName);
        }
    }

    /**
     * Deletes all temporary files
     */
    void clean() {
        new File(fileName).delete();
        new File(FORMATTED_INPUT).delete();
        new File(OUTPUT).delete();
    }

    /**
     * Closes the socket
     */
    void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error occurred while closing the socket.");
        }
    }
}
