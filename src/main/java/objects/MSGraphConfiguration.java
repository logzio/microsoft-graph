package objects;

import java.util.HashMap;

public class MSGraphConfiguration {
    private AzureADClient azureADClient;
    private LogzioJavaSenderParams senderParams;
    private String logLevel = "INFO";
    private TargetApi targetApi;
    private HashMap<String,String> additionalFields;

    public MSGraphConfiguration() {
    }

    public void setAzureADClient(AzureADClient azureADClient) {
        this.azureADClient = azureADClient;
    }

    public AzureADClient getAzureADClient() {
        return azureADClient;
    }

    public LogzioJavaSenderParams getSenderParams() {
        return senderParams;
    }

    public void setSenderParams(LogzioJavaSenderParams senderParams) {
        this.senderParams = senderParams;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public TargetApi getTargetApi() {
        return targetApi;
    }

    public void setTargetApi(TargetApi targetApi) {
        this.targetApi = targetApi;
    }

    public HashMap<String, String> getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(HashMap<String, String> additionalFields) {
        this.additionalFields = additionalFields;
    }
}