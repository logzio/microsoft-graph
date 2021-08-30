package objects;

import org.apache.log4j.Logger;

public class AccessTokenManager {
    private static final Logger logger = Logger.getLogger(AccessTokenManager.class);
    private String accessToken;
    private long currentTokenExpiry = 0;
    private  final String apiUrl;
    private  final String requestUrlParameters;

    public AccessTokenManager(String apiUrl,String requestUrlParameters) {
        this.apiUrl=apiUrl;
        this.requestUrlParameters=requestUrlParameters;
        logger.debug("Initialized new token manager, api url: "+apiUrl);
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getRequestUrlParameters() {
        return requestUrlParameters;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getCurrentTokenExpiry() {
        return currentTokenExpiry;
    }

    public void setCurrentTokenExpiry(long currentTokenExpiry) {
        this.currentTokenExpiry = currentTokenExpiry;
    }

}
