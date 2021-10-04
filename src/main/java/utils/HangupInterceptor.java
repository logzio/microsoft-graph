package utils;
import org.apache.log4j.Logger;

/**
 * A class for intercepting the hang up signal and do a graceful shutdown of the Camel.
 */
public final class HangupInterceptor extends Thread {
    private static Logger logger = Logger.getLogger(HangupInterceptor.class);
    private Shutdownable main;

    public HangupInterceptor(Shutdownable main) {
        this.main = main;
}

    @Override
    public void run() {
        logger.info("Received hang up - stopping...");
            main.shutdown();
    }
}