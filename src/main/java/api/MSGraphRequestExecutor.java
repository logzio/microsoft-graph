package api;

import objects.AzureADClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Authornicator;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MSGraphRequestExecutor {
    private static final Logger logger = Logger.getLogger(MSGraphRequestExecutor.class);

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String JSON_VALUE = "value";
    private static final String JSON_NEXT_LINK = "@odata.nextLink";
    private static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String UTC = "UTC";
    private static final String FILTER_PREFIX = "?$filter=";
    private static final String GREATER_OR_EQUEAL = " ge ";
    private static final String JSON_ERROR = "error";
    private static final String JSON_MESSAGE = "message";
    private static final int DEFAULT_READ_TIMEOUT_SEC = 20;
    private final Authornicator auth;
    private final int interval; // in millis

    public MSGraphRequestExecutor(AzureADClient client) throws AuthenticationException {
        auth = new AuthorizationManager(client);
        this.interval = client.getPullIntervalSeconds() * 1000;
    }

    public MSGraphRequestExecutor(AzureADClient client, Authornicator authornicator) {
        this.auth = authornicator;
        this.interval = client.getPullIntervalSeconds();
    }

    private Response executeRequest(String url) throws IOException, AuthenticationException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(DEFAULT_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
        String accessToken = auth.getAccessToken();
        if (accessToken == null) {
            throw new AuthenticationException("couldn't get access token, will try at the next pull");
        }
        Request request = new Request.Builder()
                .addHeader(AUTHORIZATION, BEARER_PREFIX + accessToken)
                .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                .url(url)
                .get()
                .build();
        return client.newCall(request).execute();
    }

    public JSONArray getAllPages(String api, String timeField,String optionalFilter) throws IOException, JSONException, AuthenticationException {
        return getAllPages(  api + timeFilterSuffix(timeField)+optionalFilter);
    }

    public JSONArray getAllPages(String url) throws IOException, JSONException, AuthenticationException {
        logger.debug("Thread: "+Thread.currentThread().getName()+", API URL: " + url);
        Response response = executeRequest(url);
        String responseBody = response.body().string();
        JSONObject resultJson = new JSONObject(responseBody);

        if (resultJson.has(JSON_VALUE)) {
            JSONArray thisPage = resultJson.getJSONArray(JSON_VALUE);
            logger.debug(thisPage.length() + " records in this page");
            if (resultJson.has(JSON_NEXT_LINK)) {
                logger.debug("found next page = " + resultJson.getString(JSON_NEXT_LINK));
                JSONArray nextPages = getAllPages(resultJson.getString(JSON_NEXT_LINK));
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

    public String timeFilterSuffix(String timeField) {
        DateFormat df = new SimpleDateFormat(ISO_8601);
        df.setTimeZone(TimeZone.getTimeZone(UTC));
        Date fromDate = new Date();
        fromDate.setTime(fromDate.getTime() - interval);
        return FILTER_PREFIX + timeField + GREATER_OR_EQUEAL + df.format(fromDate);
    }
}