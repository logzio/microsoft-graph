package api.authorization;

import objects.AzureADClient;
import javax.naming.AuthenticationException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AzureManagementAuthorizationManager extends Authorizer {
    private static final String OAUTH2_MANAGEMENT_TOKEN_API = "/oauth2/token";
    private static final String RESOURCE="&resource=";
    private static final String AZURE_MANAGEMENT_ENDPOINT="https://management.azure.com/";

    public AzureManagementAuthorizationManager(AzureADClient client) throws AuthenticationException {
        this(client, MICROSOFTONLINE_ADDRESS + client.getTenantId() + OAUTH2_MANAGEMENT_TOKEN_API);
    }

    public AzureManagementAuthorizationManager(AzureADClient client, String url) throws AuthenticationException {
        super(client, url);
    }

    @Override
    protected String getTokenRequestUrlParameters() throws UnsupportedEncodingException {
        return GRANT_TYPE + CLIENT_CREDENTIALS
                + CLIENT_ID + getClientId()
                + RESOURCE + AZURE_MANAGEMENT_ENDPOINT
                + CLIENT_SECRET + URLEncoder.encode(getClientSecret(), StandardCharsets.UTF_8.toString());
    }
}
