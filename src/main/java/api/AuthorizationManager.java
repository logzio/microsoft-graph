package api;

import objects.AzureADClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Authornicator;

import javax.naming.AuthenticationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AuthorizationManager implements Authornicator {

    private static final Logger logger = Logger.getLogger(AuthorizationManager.class);

    private static final String MICROSOFTONLINE_ADDRESS = "https://login.microsoftonline.com/";
    private static final String MICROSOFT_GRAPH = "https://graph.microsoft.com/.default";
    private static final String OAUTH2_TOKEN_API = "/oauth2/v2.0/token";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String REQUEST_CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String REQUEST_CONTENT_LENGTH = "Content-Length";
    private static final String JSON_ACCESS_TOKEN = "access_token";
    private static final String JSON_ACCESS_TOKEN_EXPIRE_DURATION = "expires_in";
    private static final int ONE_MINUTES_IN_MILLIS = 60 * 1000;
    private static final String GRANT_TYPE = "grant_type=";
    private static final String CLIENT_ID = "&client_id=";
    private static final String SCOPE = "&scope=";
    private static final String CLIENT_SECRET = "&client_secret=";

    private String clientId;
    private String clientSecret;
    private String apiUrl;
    private String accessToken;
    private long currentTokenExpiry = 0;


    public AuthorizationManager(AzureADClient client) throws AuthenticationException {
        this(client, MICROSOFTONLINE_ADDRESS + client.getTenantId() + OAUTH2_TOKEN_API);
    }

    public AuthorizationManager(AzureADClient client, String url) throws AuthenticationException {
        logger.info("Initializing authorization manager");
        this.clientId = client.getClientId();
        this.clientSecret = client.getClientSecret();
        this.apiUrl = url;
        retrieveToken();
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

    private void retrieveToken() throws AuthenticationException {
        try {
            byte[] postData = getTokenRequestUrlParameters().getBytes(StandardCharsets.UTF_8);
            HttpURLConnection con = executeTokenRequest(postData);
            StringBuilder response = readTokenRequestResponse(con);
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JSONObject jsonResponse = new JSONObject(response.toString());
                accessToken = jsonResponse.get(JSON_ACCESS_TOKEN).toString();
                currentTokenExpiry = System.currentTimeMillis() + jsonResponse.getInt(JSON_ACCESS_TOKEN_EXPIRE_DURATION) * 1000;
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

    private HttpURLConnection executeTokenRequest(byte[] postData) throws IOException {
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

    private String getTokenRequestUrlParameters() throws UnsupportedEncodingException {
        return GRANT_TYPE + CLIENT_CREDENTIALS
                + CLIENT_ID + clientId
                + SCOPE + MICROSOFT_GRAPH
                + CLIENT_SECRET + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8.toString());
    }
}
