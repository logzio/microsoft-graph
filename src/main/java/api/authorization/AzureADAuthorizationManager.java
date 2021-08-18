package api.authorization;

import objects.AzureADClient;
import javax.naming.AuthenticationException;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AzureADAuthorizationManager extends Authorizer {
    private static final String MICROSOFT_GRAPH = "https://graph.microsoft.com/.default";
    private static final String OAUTH2_TOKEN_API = "/oauth2/v2.0/token";
    private static final String SCOPE = "&scope=";

    /**
     * Invoked with reflection
     * @param client contains client info
     * @throws AuthenticationException
     */
   public AzureADAuthorizationManager(AzureADClient client) throws AuthenticationException {
       this(client, MICROSOFTONLINE_ADDRESS + client.getTenantId() + OAUTH2_TOKEN_API);
    }

    public AzureADAuthorizationManager(AzureADClient client, String url) throws AuthenticationException {
        super(client,url);
    }

    protected String getTokenRequestUrlParameters() throws UnsupportedEncodingException {
        return GRANT_TYPE + CLIENT_CREDENTIALS
                + CLIENT_ID + getClientId()
                + SCOPE + MICROSOFT_GRAPH
                + CLIENT_SECRET + URLEncoder.encode(getClientSecret(), StandardCharsets.UTF_8.toString());
    }
}
