package utils;

import io.logz.sender.SenderStatusReporter;
import org.apache.log4j.Logger;

public class StatusReporterFactory {
    public static SenderStatusReporter newSenderStatusReporter(final Logger logger) {
        return new SenderStatusReporter() {

            public void error(String s) {
                logger.error("MSGraph: " + s);
            }

            public void error(String s, Throwable throwable) {
                logger.error("MSGraph: " + s + " " + throwable.getMessage());
            }

            public void warning(String s) {
                logger.warn("MSGraph: " + s);
                logger.warn("MSGraph: " + s);
            }

            public void warning(String s, Throwable throwable) {
                logger.warn("MSGraph: " + s + " " + throwable.getMessage());
            }

            @Override
            public void info(String s) {
                logger.info("MSGraph: " + s);
            }

            @Override
            public void info(String s, Throwable throwable) {
                logger.info("MSGraph: " + s + " " + throwable.getMessage());
            }
        };
    }
}

