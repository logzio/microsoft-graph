package objects;

public class AzureADClient {

    private String tenantId;
    private String clientId;
    private String clientSecret;
    private int pullIntervalSeconds = 300;

    public AzureADClient() {
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setPullIntervalSeconds(int pullIntervalSeconds) {
        this.pullIntervalSeconds = pullIntervalSeconds;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public int getPullIntervalSeconds() {
        return pullIntervalSeconds;
    }

}