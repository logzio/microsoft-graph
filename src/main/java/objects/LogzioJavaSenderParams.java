package objects;

import java.io.File;

public class LogzioJavaSenderParams {


    private static String HTTPS_PREFIX = "https://";
    private static String PORT_SUFFIX = ":8071";
    private String listenerUrl = HTTPS_PREFIX + "listener.logz.io" + PORT_SUFFIX;
    private String type = "Microsoft-Graph";
    private String accountToken;
    private int threadPoolSize = 3;
    private boolean debug = true;
    private boolean compressRequests = true;
    private boolean fromDisk = true;

    // In-memory queue parameters
    private int inMemoryQueueCapacityInBytes = 1024 * 1024 * 100;
    private int logsCountLimit = -1;

    // Disk queue parameters
    private File queueDir;
    private int fileSystemFullPercentThreshold = 98;
    private int gcPersistedQueueFilesIntervalSeconds = 30;
    private int diskSpaceCheckInterval = 1000;

    /**
     * Constructor automatically create metrics directory
     */
    public LogzioJavaSenderParams() {
        String queuePath = System.getProperty("user.dir");
        queuePath += queuePath.endsWith("/") ? "" : "/";
        queuePath += "MSGraph";
        this.queueDir = new File(queuePath);
    }

    public String getListenerUrl() {
        return listenerUrl;
    }

    public void setListenerUrl(String listenerUrl) {
        this.listenerUrl = HTTPS_PREFIX + listenerUrl + PORT_SUFFIX;
    }

    public String getType() {
        return type;
    }


    public String getAccountToken() {
        return accountToken;
    }

    public void setAccountToken(String accountToken) {
        this.accountToken = accountToken;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isCompressRequests() {
        return compressRequests;
    }

    public int getInMemoryQueueCapacityInBytes() {
        return inMemoryQueueCapacityInBytes;
    }

    public void setInMemoryQueueCapacityInBytes(int inMemoryQueueCapacityInBytes) {
        this.inMemoryQueueCapacityInBytes = inMemoryQueueCapacityInBytes;
    }

    public int getLogsCountLimit() {
        return logsCountLimit;
    }

    public void setLogsCountLimit(int logsCountLimit) {
        this.logsCountLimit = logsCountLimit;
    }

    public int getDiskSpaceCheckInterval() {
        return diskSpaceCheckInterval;
    }

    public void setDiskSpaceCheckInterval(int diskSpaceCheckInterval) {
        this.diskSpaceCheckInterval = diskSpaceCheckInterval;
    }

    public File getQueueDir() {
        return queueDir;
    }

    public void setQueueDir(File queueDir) {
        this.queueDir = queueDir;
    }

    public int getFileSystemFullPercentThreshold() {
        return fileSystemFullPercentThreshold;
    }

    public void setFileSystemFullPercentThreshold(int fileSystemFullPercentThreshold) {
        this.fileSystemFullPercentThreshold = fileSystemFullPercentThreshold;
    }

    public int getGcPersistedQueueFilesIntervalSeconds() {
        return gcPersistedQueueFilesIntervalSeconds;
    }

    public void setGcPersistedQueueFilesIntervalSeconds(int gcPersistedQueueFilesIntervalSeconds) {
        this.gcPersistedQueueFilesIntervalSeconds = gcPersistedQueueFilesIntervalSeconds;
    }

    public boolean isFromDisk() {
        return fromDisk;
    }

    public void setFromDisk(boolean fromDisk) {
        this.fromDisk = fromDisk;
    }
}