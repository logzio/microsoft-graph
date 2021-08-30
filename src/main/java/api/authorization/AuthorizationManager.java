package api.authorization;

import objects.AccessTokenManager;
import objects.AzureADClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import utils.ApiUtil;

import javax.naming.AuthenticationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationManager {
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected static final String REQUEST_CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    protected static final String REQUEST_CONTENT_LENGTH = "Content-Length";
    protected static final String JSON_ACCESS_TOKEN = "access_token";
    protected static final String JSON_ACCESS_TOKEN_EXPIRE_DURATION = "expires_in";
    protected static final int ONE_MINUTES_IN_MILLIS = 60 * 1000;
    private final String clientId;
    private final String clientSecret;
    private final String tenantId;
    private final Map<String, AccessTokenManager> unformattedAuthorizationUrlToTokenManager;


    public AuthorizationManager(AzureADClient client) {
        logger.info("Initializing authorization manager");
        this.clientId = client.getClientId();
        this.clientSecret = client.getClientSecret();
        this.tenantId=client.getTenantId();
        unformattedAuthorizationUrlToTokenManager = new HashMap<>();
    }

    public AuthorizationManager(AzureADClient client, String url, String urlParameters) {
        this.clientId = client.getClientId();
        this.clientSecret = client.getClientSecret();
        this.tenantId=client.getTenantId();
        unformattedAuthorizationUrlToTokenManager = new HashMap<>();
        unformattedAuthorizationUrlToTokenManager.put(url,new AccessTokenManager(url,urlParameters));
    }

    public synchronized String getAccessToken(String unformattedAuthorizationUrl) throws UnsupportedEncodingException,AuthenticationException {
        AccessTokenManager accessTokenManager;
        if(unformattedAuthorizationUrlToTokenManager.containsKey(unformattedAuthorizationUrl)){
            accessTokenManager=unformattedAuthorizationUrlToTokenManager.get(unformattedAuthorizationUrl);
        }else{
            accessTokenManager=initNewTokenManager(unformattedAuthorizationUrl);
            unformattedAuthorizationUrlToTokenManager.put(unformattedAuthorizationUrl,accessTokenManager);
        }

        if (System.currentTimeMillis() > accessTokenManager.getCurrentTokenExpiry() - ONE_MINUTES_IN_MILLIS) { // 1 minutes safety
            retrieveToken(accessTokenManager);
        }
        return accessTokenManager.getAccessToken();
    }

    private AccessTokenManager initNewTokenManager(String unformattedAuthorizationUrl) throws UnsupportedEncodingException {
        String authorizationUrl=String.format(unformattedAuthorizationUrl,tenantId);
        String authorizationUrlParameters=String.format(ApiUtil.getAuthorizationUrlParameters(unformattedAuthorizationUrl), clientId,
                URLEncoder.encode(getClientSecret(), StandardCharsets.UTF_8.toString()));
        return new AccessTokenManager(authorizationUrl,authorizationUrlParameters);
    }

    private void retrieveToken(AccessTokenManager accessTokenManager) throws AuthenticationException {
        try {
            byte[] postData = accessTokenManager.getRequestUrlParameters().getBytes(StandardCharsets.UTF_8);
            HttpURLConnection con = executeTokenRequest(postData,accessTokenManager.getApiUrl());
            StringBuilder response = readTokenRequestResponse(con);
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JSONObject jsonResponse = new JSONObject(response.toString());
                accessTokenManager.setAccessToken(jsonResponse.get(JSON_ACCESS_TOKEN).toString());
                accessTokenManager.setCurrentTokenExpiry(System.currentTimeMillis() + jsonResponse.getInt(JSON_ACCESS_TOKEN_EXPIRE_DURATION) * 1000L);
            } else {
                throw new AuthenticationException("Invalid response code while fetching access token: " + con.getResponseCode() + "\n response: " + response);
            }
        } catch (IOException | JSONException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    private StringBuilder readTokenRequestResponse(HttpURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();
        return response;
    }

    private HttpURLConnection executeTokenRequest(byte[] postData, String apiUrl) throws IOException {
        URL obj = new URL(apiUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setDoOutput(true);
        con.setRequestProperty(REQUEST_CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
        con.setRequestProperty(REQUEST_CONTENT_LENGTH, Integer.toString(postData.length));

        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(postData);
        }

        return con;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
