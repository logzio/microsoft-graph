package main;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import utils.exceptions.ConfigurationException;

import java.io.FileNotFoundException;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);
    private static final int CONFIG_FILE_INDEX = 0;

    public static void main(String[] args) throws FileNotFoundException, ConfigurationException {
        BasicConfigurator.configure();
        if (args.length > 0) {
            MSClient msClient = new MSClient(args[CONFIG_FILE_INDEX]);
            msClient.start();

        } else {
            logger.fatal("No config file supplied, quiting.");
        }
    }
}