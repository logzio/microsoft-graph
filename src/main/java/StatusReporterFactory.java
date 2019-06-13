import io.logz.sender.SenderStatusReporter;
import org.slf4j.Logger;

public class StatusReporterFactory {
    public static SenderStatusReporter newSenderStatusReporter(final Logger logger) {
        return new SenderStatusReporter() {

            public void error(String s) {
                logger.error("Office365" + s);
            }

            public void error(String s, Throwable throwable) {
                logger.error("Office365" + s + " " + throwable.getMessage());
            }

            public void warning(String s) {
                logger.warn("Office365" + s);
            }

            public void warning(String s, Throwable throwable) {
                logger.warn("Office365" + s + " " + throwable.getMessage());
            }

            @Override
            public void info(String s) {
                logger.debug("Office365" + s);
            }

            @Override
            public void info(String s, Throwable throwable) {
                logger.debug("Office365" + s + " " + throwable.getMessage());
            }
        };
    }
}