package api.authorization;

import objects.AzureADClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.naming.AuthenticationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class Authorizer {
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected static final String MICROSOFTONLINE_ADDRESS = "https://login.microsoftonline.com/";
    protected static final String CLIENT_CREDENTIALS = "client_credentials";
    protected static final String REQUEST_CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    protected static final String REQUEST_CONTENT_LENGTH = "Content-Length";
    protected static final String JSON_ACCESS_TOKEN = "access_token";
    protected static final String JSON_ACCESS_TOKEN_EXPIRE_DURATION = "expires_in";
    protected static final int ONE_MINUTES_IN_MILLIS = 60 * 1000;
    protected static final String GRANT_TYPE = "grant_type=";
    protected static final String CLIENT_ID = "&client_id=";
    protected static final String CLIENT_SECRET = "&client_secret=";
    private final String clientId;
    private final String clientSecret;
    private final String apiUrl;
    protected String accessToken;
    protected long currentTokenExpiry = 0;

    public Authorizer(AzureADClient client, String url) throws AuthenticationException {
        logger.info("Initializing authorization manager");
        this.clientId = client.getClientId();
        this.clientSecret = client.getClientSecret();
        this.apiUrl = url;
        retrieveToken();
    }

    /**
     * Constructor for testing purposes only
     *
     * @param sampleToken
     */
    public Authorizer(String sampleToken) {
        this.accessToken = sampleToken;
        this.clientId = StringUtils.EMPTY;
        this.clientSecret = StringUtils.EMPTY;
        this.apiUrl = StringUtils.EMPTY;
    }

    public String getAccessToken() {
        if (System.currentTimeMillis() > currentTokenExpiry - ONE_MINUTES_IN_MILLIS) { // 1 minutes safety
            try {
                retrieveToken();
            } catch (AuthenticationException e) {
                logger.error("Error fetching access token: " + e);
                return null;
            }
        }
        return accessToken;
    }

    protected void retrieveToken() throws AuthenticationException {
        try {
            byte[] postData = getTokenRequestUrlParameters().getBytes(StandardCharsets.UTF_8);
            HttpURLConnection con = executeTokenRequest(postData);
            StringBuilder response = readTokenRequestResponse(con);
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JSONObject jsonResponse = new JSONObject(response.toString());
                accessToken = jsonResponse.get(JSON_ACCESS_TOKEN).toString();
                currentTokenExpiry = System.currentTimeMillis() + jsonResponse.getInt(JSON_ACCESS_TOKEN_EXPIRE_DURATION) * 1000L;
            } else {
                throw new AuthenticationException("Invalid response code while fetching access token: " + con.getResponseCode() + "\n response: " + response);
            }
        } catch (IOException | JSONException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    protected abstract String getTokenRequestUrlParameters() throws UnsupportedEncodingException;

    protected StringBuilder readTokenRequestResponse(HttpURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();
        return response;
    }

    protected HttpURLConnection executeTokenRequest(byte[] postData) throws IOException {
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
