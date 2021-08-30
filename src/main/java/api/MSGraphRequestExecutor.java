package api;

import api.authorization.AuthorizationManager;
import objects.AzureADClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MSGraphRequestExecutor {
    private static final Logger logger = Logger.getLogger(MSGraphRequestExecutor.class);
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String JSON_VALUE = "value";
    private static final String JSON_NEXT_LINK = "@odata.nextLink";
    private static final String JSON_ERROR = "error";
    private static final String JSON_MESSAGE = "message";
    private static final int DEFAULT_READ_TIMEOUT_SEC = 20;
    private final AuthorizationManager authorizationManager;

    public MSGraphRequestExecutor(AzureADClient client) throws AuthenticationException {
        authorizationManager=new AuthorizationManager(client);
    }

    public MSGraphRequestExecutor(AzureADClient client,String authorizationUrl,String urlParameters) throws AuthenticationException {
        authorizationManager=new AuthorizationManager(client,authorizationUrl,urlParameters);
    }

    private Response executeRequest(String url, String unformattedAuthorizationUrl) throws IOException, AuthenticationException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(DEFAULT_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
        String accessToken = authorizationManager.getAccessToken(unformattedAuthorizationUrl);
        Request request = new Request.Builder()
                .addHeader(AUTHORIZATION, BEARER_PREFIX + accessToken)
                .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                .url(url)
                .get()
                .build();
        return client.newCall(request).execute();
    }

    public JSONArray getAllPages(String url, String unformattedAuthorizationUrl ) throws IOException, JSONException, AuthenticationException {
        logger.debug("Thread: "+Thread.currentThread().getName()+", API URL: " + url);
        Response response = executeRequest(url, unformattedAuthorizationUrl);
        String responseBody = response.body().string();
        JSONObject resultJson = new JSONObject(responseBody);

        if (resultJson.has(JSON_VALUE)) {
            JSONArray thisPage = resultJson.getJSONArray(JSON_VALUE);
            logger.debug(thisPage.length() + " records in this page");
            if (resultJson.has(JSON_NEXT_LINK)) {
                logger.debug("found next page = " + resultJson.getString(JSON_NEXT_LINK));
                JSONArray nextPages = getAllPages(resultJson.getString(JSON_NEXT_LINK),unformattedAuthorizationUrl);
                for (int i = 0; i < thisPage.length(); i++) {
                    nextPages.put(thisPage.get(i));
                }
                return nextPages;
            }
            return thisPage;
        } else if (resultJson.has(JSON_ERROR)) {
            throw new IOException(resultJson.getJSONObject(JSON_ERROR).getString(JSON_MESSAGE));
        }
        return new JSONArray();
    }
}