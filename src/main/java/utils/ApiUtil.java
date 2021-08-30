package utils;

import api.office365Apis.*;

import java.util.HashMap;
import java.util.Map;

public class ApiUtil {

    protected static final String MICROSOFTONLINE_ADDRESS = "https://login.microsoftonline.com/";
    protected static final String CLIENT_CREDENTIALS = "client_credentials";
    protected static final String GRANT_TYPE = "grant_type=";
    protected static final String CLIENT_ID = "&client_id=";
    protected static final String CLIENT_SECRET = "&client_secret=";
    private static final String MICROSOFT_GRAPH = "https://graph.microsoft.com/.default";
    private static final String OAUTH2_TOKEN_API = "/oauth2/v2.0/token";
    private static final String SCOPE = "&scope=";
    private static final String OAUTH2_MANAGEMENT_TOKEN_API = "/oauth2/token";
    private static final String RESOURCE="&resource=";
    private static final String AZURE_MANAGEMENT_ENDPOINT="https://management.azure.com/";
    private static final Map<Class<? extends Office365Api>,String> apiTargetToAuthenticationUrl;
    private static final Map<String,String> authorizationUrlToUrlParamteres;
    private static final String apiPackageName=Office365Api.class.getPackageName();
    private static final String AZURE_AD_TOKEN_URL=MICROSOFTONLINE_ADDRESS+"%1$s"+OAUTH2_TOKEN_API;
    private static final String AZURE_MANAGEMENT_TOKEN_URL =MICROSOFTONLINE_ADDRESS+"%1$s"+OAUTH2_MANAGEMENT_TOKEN_API;
    private static final String AZURE_AD_URL_PARAMETERS =GRANT_TYPE + CLIENT_CREDENTIALS
            + CLIENT_ID + "%1$s"
            + SCOPE + MICROSOFT_GRAPH
            + CLIENT_SECRET + "%2$s";
    private static final String AZURE_MANAGEMENT_URL_PARAMETERS =GRANT_TYPE + CLIENT_CREDENTIALS
            + CLIENT_ID + "%1$s"
            + RESOURCE + AZURE_MANAGEMENT_ENDPOINT
            + CLIENT_SECRET + "%2$s";

    static{
        apiTargetToAuthenticationUrl = new HashMap<>();
        authorizationUrlToUrlParamteres= new HashMap<>();
        apiTargetToAuthenticationUrl.put(SignIns.class, AZURE_AD_TOKEN_URL);
        apiTargetToAuthenticationUrl.put(RiskySignIns.class, AZURE_AD_TOKEN_URL);
        apiTargetToAuthenticationUrl.put(DirectoryAudits.class, AZURE_AD_TOKEN_URL);
        apiTargetToAuthenticationUrl.put(ASCAlerts.class, AZURE_AD_TOKEN_URL);
        apiTargetToAuthenticationUrl.put(ASCTasks.class, AZURE_MANAGEMENT_TOKEN_URL);
        authorizationUrlToUrlParamteres.put(AZURE_AD_TOKEN_URL,AZURE_AD_URL_PARAMETERS);
        authorizationUrlToUrlParamteres.put(AZURE_MANAGEMENT_TOKEN_URL,AZURE_MANAGEMENT_URL_PARAMETERS);

    }

    public static String getAuthorizationUrl(Class<?> apiTargetClass){
        return apiTargetToAuthenticationUrl.get(apiTargetClass);
    }

    public static String getAuthorizationUrlParameters(String authorizationUrl){
        return authorizationUrlToUrlParamteres.get(authorizationUrl);
    }

    public static String getApisPackageName(){
        return apiPackageName+".";
    }
}
