package network.programming.algorithm.aprioriRare.use;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataFormatter {

    static final int EVENT_INDEX = 4;
    static final int IP_INDEX = 7;
    static private final String PROFIT = "1 ";

    /**
     * Gathers in a map the ip addresses or the event names and a number to
     * represent it
     *
     * @param fileName       the name of the input file
     * @param isForEventName whether we collect data for the event name or for the
     *                       ip
     * @return map with the data and its number
     * @throws IOException           {@link IOException}
     * @throws FileNotFoundException {@link FileNotFoundException}
     */
    static Map<String, Integer> getNumeratedData(String fileName, boolean isForEventName)
            throws FileNotFoundException, IOException {
        int index = isForEventName ? EVENT_INDEX : IP_INDEX;
        Map<String, Integer> numeratedData = new HashMap<>();
        int number = 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine(); // ignoring the first line
            while ((line = reader.readLine()) != null) {
                String[] splittedData = line.split(",");
                if (splittedData.length != 8) {
                    continue;
                }
                String data = splittedData[index];
                if (!numeratedData.containsKey(data)) {
                    numeratedData.put(data, number);
                    number++;
                }
            }
        }
        return numeratedData;
    }

    /**
     * Collects in a sorted list the events made by the given IP address
     *
     * @param filename the name of the input file
     * @param ip       the IP address
     * @return a sorted list with numbers representing events
     * @throws IOException {@link IOException}
     */
    static List<Integer> getEventsOfAnIP(String filename, String ip) throws IOException {
        Map<String, Integer> allEvents = getNumeratedData(filename, true);
        Set<Integer> events = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine(); // ignoring the first line
            while ((line = reader.readLine()) != null) {
                String[] splittedData = line.split(",");
                if (splittedData.length != 8) {
                    continue;
                }
                String currentIP = splittedData[IP_INDEX];
                String currentEvent = splittedData[EVENT_INDEX];
                int numberRepresententionOfEvent = allEvents.get(currentEvent);
                if (currentIP.equals(ip) && !events.contains(numberRepresententionOfEvent)) {
                    events.add(numberRepresententionOfEvent);
                }
            }
        }
        return events.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Formats the input file to be suitable for the VME algorithm and writes the
     * result in another file
     *
     * @param fileName         the name of the input file
     * @param formatedFileName the name of the formatted file
     * @throws IOException           {@link IOException}
     * @throws FileNotFoundException {@link FileNotFoundException}
     */
    static void formatInputData(String fileName, String formatedFileName) throws FileNotFoundException, IOException {
        try (//BufferedReader reader = new BufferedReader(new FileReader(fileName));
             BufferedWriter writer = new BufferedWriter(new FileWriter(new File(formatedFileName)))) {

            LinkedHashMap<String, Integer> ipAddresses = new LinkedHashMap<>(getNumeratedData(fileName, false));
            for (Map.Entry<String, Integer> ip : ipAddresses.entrySet()) {
                writeToFile(fileName, writer, ip);
            }
        }
    }

    /**
     * Writes to the file with the given file name one transaction
     *
     * @param filename the name of the input file
     * @param writer   writer to the formatted file {@link BufferedWriter}
     * @param ip       the IP address and its number representention
     * @throws IOException {@link IOException}
     */
    static private void writeToFile(String filename, BufferedWriter writer, Map.Entry<String, Integer> ip)
            throws IOException {

        StringBuilder sb = new StringBuilder();
        List<Integer> eventsOfIP = getEventsOfAnIP(filename, ip.getKey());
        for (Integer event : eventsOfIP) {
            sb.append(event).append(" ");
        }
        String events = sb.toString().trim();
        sb.setLength(0);
        sb.append(PROFIT).append(events).append(System.lineSeparator());
        writer.write(sb.toString());
        writer.flush();
    }

    /**
     * Writes the data in a representive way from the file with the given name to a
     * writer
     *
     * @param writer         {@link PrintWriter}
     * @param outputFileName the name of the file
     * @throws IOException {@link IOException}
     */
    static void formatResult(PrintWriter writer, String outputFileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFileName));) {
            String line = null;
            StringBuilder buildResult = new StringBuilder();
            while (reader.ready()) {
                line = reader.readLine();
                buildResult.append(formatResultLine(line)).append(System.lineSeparator());
                writer.print(buildResult.toString());
                writer.flush();
                buildResult.setLength(0);
            }
        }
    }

    /**
     * Formats one line
     *
     * @param line the given line
     * @return a formatted line
     */
    static String formatResultLine(String line) {
        String[] splitted = line.split(" #LOSS: ");
        String[] events = splitted[0].split(" ");
        String lost = splitted[1];
        String info = (Integer.parseInt(lost) == 1) ? " IP address." : " IP addresses.";
        return new StringBuilder().append("Removing the events { ").append(String.join(", ", events))
                .append(" } will lead to lost of ").append(lost).append(info).toString();
    }

}
