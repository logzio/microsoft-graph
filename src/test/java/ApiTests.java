
import api.authorization.AuthorizationManager;
import objects.AzureADClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.log4j.BasicConfigurator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.naming.AuthenticationException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ApiTests {

    private static MockWebServer mockWebServer = new MockWebServer();

    @BeforeClass
    public static void setup() {
        BasicConfigurator.configure();
    }

    @Test
    public void accessTokenRequestTest() throws InterruptedException, UnsupportedEncodingException {
        AzureADClient client = getSampleAzureADClient();
        mockWebServer.enqueue(new MockResponse());
        Assert.assertThrows(AuthenticationException.class, () -> {
            new AuthorizationManager(client,"http://localhost:" + mockWebServer.getPort(),"client_id=" + client.getClientId()
            +"&client_secret=" + URLEncoder.encode(client.getClientSecret(), StandardCharsets.UTF_8.toString()))
                    .getAccessToken("http://localhost:" + mockWebServer.getPort());
        });

        verifyRequest(client);
    }

    @Test
    public void wrongBodyAccessTokenRequest() throws InterruptedException, UnsupportedEncodingException{
        AzureADClient client = getSampleAzureADClient();
        mockWebServer.enqueue(new MockResponse());
        Assert.assertThrows(AuthenticationException.class, () -> {
            new AuthorizationManager(client,"http://localhost:" + mockWebServer.getPort(),"client_id=" + "fakeClientId"
                    +"&client_secret=" + URLEncoder.encode("fakeClientSecret"))
                    .getAccessToken("http://localhost:" + mockWebServer.getPort());
        });

        verifyFalseRequest(client);
    }

    private void verifyFalseRequest(AzureADClient client) throws InterruptedException, UnsupportedEncodingException {
        String body;
        RecordedRequest request;
        request = mockWebServer.takeRequest();
        body = request.getBody().readUtf8();
        Assert.assertFalse(body.contains("client_id=" + client.getClientId()));
        Assert.assertFalse(body.contains("client_secret=" + URLEncoder.encode(client.getClientSecret(), StandardCharsets.UTF_8.toString())));
    }

    private void verifyRequest(AzureADClient client) throws InterruptedException, UnsupportedEncodingException {
        String body;
        RecordedRequest request;
        request = mockWebServer.takeRequest();
        body = request.getBody().readUtf8();
        Assert.assertTrue(body.contains("client_id=" + client.getClientId()));
        Assert.assertTrue(body.contains("client_secret=" + URLEncoder.encode(client.getClientSecret(), StandardCharsets.UTF_8.toString())));
    }


    public static AzureADClient getSampleAzureADClient() {
        String clientID = "1234-5678";
        String clientSecret = "shh don't tell";
        String subscriptionId= "1234id";
        AzureADClient client = new AzureADClient();
        client.setClientId(clientID);
        client.setClientSecret(clientSecret);
        client.setSubscriptionId(subscriptionId);
        return client;
    }
}
