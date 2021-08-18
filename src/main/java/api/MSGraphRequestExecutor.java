package api;

import api.authorization.Authorizer;
import objects.AzureADClient;
import objects.MSGraphConfiguration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.ApiUtil;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.*;
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
    private final Map<Class<? extends Authorizer>, Authorizer> apiTargetAuthorizers;

    /**
     * Construtor for testing purposes only
     * @param client
     * @param authorizer
     * @throws AuthenticationException
     */
    public MSGraphRequestExecutor(AzureADClient client,Authorizer authorizer) throws AuthenticationException {
        apiTargetAuthorizers = Map.of(authorizer.getClass(),authorizer);
    }

    public  MSGraphRequestExecutor(MSGraphConfiguration configuration, List<String> apiClassNames) throws AuthenticationException, ReflectiveOperationException{
        apiTargetAuthorizers = initializeAuthorizers(apiClassNames,configuration.getAzureADClient());
    }

    /**
     * Instantiate required authorizers (once each) by each API from apiClassNames
     * @param apiClassNames List of api class names, excluding package prefix
     * @param azureADClient
     * @return
     * @throws ReflectiveOperationException
     */
    private Map<Class<? extends Authorizer>, Authorizer> initializeAuthorizers(List<String> apiClassNames, AzureADClient azureADClient) throws ReflectiveOperationException {
        Map<Class<? extends Authorizer>, Authorizer> apiTargetAuthorizers= new HashMap<>(apiClassNames.size());
        Set<Class<? extends Authorizer>> authorizersTypes= new HashSet<>();

        for(String apiClassName:apiClassNames){
            Class<? extends Authorizer> managerClass = ApiUtil.getAuthorizationManagerClass(Class.forName(ApiUtil.getApisPackageName()+apiClassName));
            if(!authorizersTypes.contains(managerClass)) {
                authorizersTypes.add(managerClass);
                apiTargetAuthorizers.put(managerClass,
                        managerClass.getDeclaredConstructor(AzureADClient.class)
                                .newInstance(azureADClient));
            }
        }

        return apiTargetAuthorizers;
    }

    private Response executeRequest(String url, Authorizer authorizer) throws IOException, AuthenticationException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(DEFAULT_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
        String accessToken = authorizer.getAccessToken();
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

    public JSONArray getAllPages(String url,Class<?> authenticationManagerClass ) throws IOException, JSONException, AuthenticationException {
        logger.debug("Thread: "+Thread.currentThread().getName()+", API URL: " + url);
        Response response = executeRequest(url, apiTargetAuthorizers.get(authenticationManagerClass));
        String responseBody = response.body().string();
        JSONObject resultJson = new JSONObject(responseBody);

        if (resultJson.has(JSON_VALUE)) {
            JSONArray thisPage = resultJson.getJSONArray(JSON_VALUE);
            logger.debug(thisPage.length() + " records in this page");
            if (resultJson.has(JSON_NEXT_LINK)) {
                logger.debug("found next page = " + resultJson.getString(JSON_NEXT_LINK));
                JSONArray nextPages = getAllPages(resultJson.getString(JSON_NEXT_LINK),authenticationManagerClass);
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