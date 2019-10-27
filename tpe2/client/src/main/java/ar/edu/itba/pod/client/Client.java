package ar.edu.itba.pod.client;

import ar.edu.itba.pod.model.Airport;
import ar.edu.itba.pod.model.Movement;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private static String[] serverAddresses;
    private static Path inputDirectory;
    private static Path outputDirectory;
    private static final String AP_INPUT_FILENAME = "aeropuertos.csv";
    private static final String MOV_INPUT_FILENAME = "movimientos.csv";
    private static List<Airport> airports;
    private static List<Movement> movements;

    public static void main(String[] args) {
        LOGGER.info("tpe2 Client Starting ...");
        if(!parseArguments())
            System.exit(1);

        //Example from the documentation

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName("g5").setPassword("12345678");
        clientConfig.getNetworkConfig().addAddress(serverAddresses);

        HazelcastInstance client = HazelcastClient.newHazelcastClient( clientConfig );

        parseInputFiles();
        System.out.println(airports.size() + "  " + airports.get(0));
        System.out.println("  " + airports.get(1));
        System.out.println(movements.size() + "  " + movements.get(0));
        System.out.println("  " + movements.get(1));

        IMap map = client.getMap( "customers" );
        System.out.println( "Map Size:" + map.size() );
    }

    private static boolean parseArguments() {
        boolean success;
        success = parseServerAddress();
        success &= parseInputDirectory();
        success &= parseOutputDirectory();
        return success;
    }

    private static boolean parseServerAddress() {
        String serverAddress = System.getProperty("addresses");
        if(serverAddress == null) {
            LOGGER.error("serverAddresses must be present.");
            return false;
        }
        serverAddresses = serverAddress.split(";");
        return true;
    }

    private static boolean parseInputDirectory() {
        String inputDirectoryStr = System.getProperty("inPath");
        if(inputDirectoryStr == null) {
            LOGGER.error("input directory must be present");
            return false;
        }
        inputDirectory = Paths.get(inputDirectoryStr);
        return true;
    }

    private static boolean parseOutputDirectory() {
        String outputDirectoryStr = System.getProperty("outPath");
        if(outputDirectoryStr == null) {
            LOGGER.error("output directory must be present");
            return false;
        }
        outputDirectory = Paths.get(outputDirectoryStr);
        return true;
    }

    private static void parseInputFiles() {
        try {
            airports = new AirportsParser().parseCsv(concatPath(inputDirectory, AP_INPUT_FILENAME));
            movements = new MovementsParser().parseCsv(concatPath(inputDirectory, MOV_INPUT_FILENAME));
        } catch(IOException e) {
            LOGGER.error("Error reading input file.");
            e.printStackTrace();
            System.exit(1);
        } catch(InvalidCsvException e) {
            LOGGER.error("Error reading {} file at line {} (got {})", e.filename, e.lineNumber, e.line);
            System.exit(1);
        }
    }

    private static String concatPath(Path path, String childPath) {
        return path.resolve(childPath).toString();
    }
}
