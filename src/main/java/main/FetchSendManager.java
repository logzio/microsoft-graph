package main;

import io.logz.sender.HttpsRequestConfiguration;
import io.logz.sender.LogzioSender;
import io.logz.sender.SenderStatusReporter;
import io.logz.sender.exceptions.LogzioParameterErrorException;
import objects.JsonArrayRequest;
import objects.LogzioJavaSenderParams;
import objects.RequestDataResult;
import org.apache.log4j.Logger;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.json.JSONException;
import utils.exceptions.ConfigurationException;
import utils.HangupInterceptor;
import utils.Shutdownable;
import utils.StatusReporterFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.pollinterval.FibonacciPollInterval.fibonacci;

public class FetchSendManager implements Shutdownable {
    private static final Logger logger = Logger.getLogger(FetchSendManager.class);
    private static final int NO_DELAY = 0;
    private static final int DEFAULT_POLLING_INTERVAL = 3;
    private static final int RETRY_TIMEOUT_DURATION_SEC = 60;
    private static final int TERMINATION_TIMEOUT_SEC = 20;
    private static final int FIBONACCI_OFFSET = 4;

    private final ScheduledExecutorService taskScheduler;
    private final ArrayList<JsonArrayRequest> dataRequests;
    private final LogzioJavaSenderParams logzioSenderParams;
    private final LogzioSender sender;
    private final int interval;
    private ScheduledExecutorService senderExecutors;


    public FetchSendManager(ArrayList<JsonArrayRequest> dataRequests, LogzioJavaSenderParams senderParams, int interval) {
        this.taskScheduler = Executors.newScheduledThreadPool(dataRequests.size());
        this.logzioSenderParams = senderParams;
        this.dataRequests = dataRequests;
        this.sender = getLogzioSender();
        this.interval = interval;
    }

    public void start() {
        logger.info("starting fetch-send scheduled operation");
        enableHangupSupport();
        dataRequests.forEach(request -> taskScheduler.scheduleAtFixedRate(() -> {
            try {
                pullAndSendData(request);
            } catch (ConfigurationException exception) {
                exception.printStackTrace();
            }
        }, NO_DELAY, interval, SECONDS));
        sender.start();
    }

    public void pullAndSendData(JsonArrayRequest request) throws ConfigurationException {
        RequestDataResult dataResult = request.getResult();
        if (!dataResult.isSucceed()) {
            try {
                Awaitility.with()
                        .pollDelay(DEFAULT_POLLING_INTERVAL, SECONDS)
                        .pollInterval(fibonacci(FIBONACCI_OFFSET,TimeUnit.SECONDS))
                        .atMost(RETRY_TIMEOUT_DURATION_SEC, SECONDS)
                        .await()
                        .until(() -> {
                            logger.warn("Couldn't complete the request, retrying..");
                            dataResult.setRequestDataResult(request.getResult());
                            return dataResult.isSucceed();
                        });
            } catch (ConditionTimeoutException e) {
                logger.error("All retries failed, ignoring request");
            }
        }

        convertAndSendResults(dataResult);
    }

    private void convertAndSendResults(RequestDataResult dataResult) {
        for (int i = 0; i < dataResult.getData().length(); i++) {
            try {
                byte[] jsonAsBytes = StandardCharsets.UTF_8.encode(dataResult.getData().getJSONObject(i).toString()).array();
                synchronized (this) {
                    sender.send(jsonAsBytes);
                }
            } catch (JSONException e) {
                logger.error("error extracting json object from response: " + e.getMessage(), e);
            }
        }
    }

    private LogzioSender getLogzioSender() {
        senderExecutors = Executors.newScheduledThreadPool(logzioSenderParams.getThreadPoolSize());
        try {
            HttpsRequestConfiguration requestConf = getSenderRequestConfig();
            SenderStatusReporter statusReporter = StatusReporterFactory.newSenderStatusReporter(Logger.getLogger(LogzioJavaSenderParams.class));
            LogzioSender.Builder senderBuilder = LogzioSender
                    .builder()
                    .setTasksExecutor(senderExecutors)
                    .setReporter(statusReporter)
                    .setHttpsRequestConfiguration(requestConf)
                    .setDebug(logger.isDebugEnabled())
                    .setDrainTimeoutSec(logzioSenderParams.getSenderDrainIntervals());
            if (logzioSenderParams.isFromDisk()) {
                setFromDiskParams(senderBuilder);
            } else {
                setInMemoryParams(senderBuilder);
            }
            return senderBuilder.build();
        } catch (LogzioParameterErrorException e) {
            logger.error("problem in one or more parameters with error " + e.getMessage(), e);
        }
        return null;
    }

    private HttpsRequestConfiguration getSenderRequestConfig() throws LogzioParameterErrorException {
        return HttpsRequestConfiguration
                .builder()
                .setLogzioListenerUrl(logzioSenderParams.getListenerUrl())
                .setLogzioType(logzioSenderParams.getType())
                .setLogzioToken(logzioSenderParams.getAccountToken())
                .setCompressRequests(logzioSenderParams.isCompressRequests())
                .build();
    }

    private void setInMemoryParams(LogzioSender.Builder senderBuilder) {
        senderBuilder.withInMemoryQueue()
                .setCapacityInBytes(logzioSenderParams.getInMemoryQueueCapacityInBytes())
                .setLogsCountLimit(logzioSenderParams.getLogsCountLimit())
                .endInMemoryQueue();
    }

    private void setFromDiskParams(LogzioSender.Builder senderBuilder) {
        senderBuilder.withDiskQueue()
                .setQueueDir(logzioSenderParams.getQueueDir())
                .setCheckDiskSpaceInterval(logzioSenderParams.getDiskSpaceCheckInterval())
                .setFsPercentThreshold(logzioSenderParams.getFileSystemFullPercentThreshold())
                .setGcPersistedQueueFilesIntervalSeconds(logzioSenderParams.getGcPersistedQueueFilesIntervalSeconds())
                .endDiskQueue();
    }

    private void enableHangupSupport() {
        HangupInterceptor interceptor = new HangupInterceptor(this);
        Runtime.getRuntime().addShutdownHook(interceptor);
    }

    @Override
    public void shutdown() {
        logger.info("requesting data fetcher to stop");
        try {
            taskScheduler.shutdown();
            if (!taskScheduler.awaitTermination(TERMINATION_TIMEOUT_SEC, SECONDS)) {
                taskScheduler.shutdownNow();
            }
            logger.info("stopping data sender");
            sender.stop();
            senderExecutors.shutdown();
            if (!senderExecutors.awaitTermination(TERMINATION_TIMEOUT_SEC, SECONDS)) {
                senderExecutors.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("final request was interrupted: " + e.getMessage(), e);
        } catch (SecurityException ex) {
            logger.error("can't submit final request: " + ex.getMessage(), ex);
        }
        logger.info("Shutting down...");
    }
}