import api.MSGraphRequestExecutor;
import main.FetchSendManager;
import objects.JsonArrayRequest;
import objects.LogzioJavaSenderParams;
import objects.RequestDataResult;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.RequestDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.naming.AuthenticationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class FetchSendTests {

    private static final Logger logger = LoggerFactory.getLogger(FetchSendTests.class.getName());
    private static MockServerClient mockServerClient = null;
    private static ClientAndServer mockServer;
    private static LogzioJavaSenderParams senderParams = new LogzioJavaSenderParams();
    private int retries = 0;
    private static Path tempDir;

    @BeforeClass
    public static void startMockServer() throws JSONException {
        BasicConfigurator.configure();
        logger.info("starting mock server");
        JSONObject requestResponseBody = new JSONObject();
        requestResponseBody.put("@odata.nextLink", "http://localhost:8070/nextlink1");
        requestResponseBody.put("value", new JSONArray("[{\"key\":1}]"));

        JSONObject link1ResponseBody = new JSONObject();
        link1ResponseBody.put("@odata.nextLink", "http://localhost:8070/nextlink2");
        link1ResponseBody.put("value", new JSONArray("[{\"key\":2}]"));

        JSONObject link2ResponseBody = new JSONObject();
        link2ResponseBody.put("value", new JSONArray("[{\"key\":3}]"));

        mockServer = startClientAndServer(8070);

        mockServerClient = new MockServerClient("localhost", 8070);
        mockServerClient
                .when(request().withMethod("POST"))
                .respond(response().withStatusCode(200));
        mockServer
                .when(request().withMethod("GET").withPath("/chainRequest"))
                .respond(response().withStatusCode(200).withBody(requestResponseBody.toString()));
        mockServer
                .when(request().withMethod("GET").withPath("/nextlink1"))
                .respond(response().withStatusCode(200).withBody(link1ResponseBody.toString()));
        mockServer
                .when(request().withMethod("GET").withPath("/nextlink2"))
                .respond(response().withStatusCode(200).withBody(link2ResponseBody.toString()));
    }

    @BeforeClass
    public static void setup() throws IOException {
        senderParams.setFullListenerUrl("http://127.0.0.1:8070");
        senderParams.setAccountToken("not-a-real-token");
        senderParams.setSenderDrainIntervals(1);
        tempDir = Files.createTempDirectory(null);
        senderParams.setQueueDir(tempDir.toFile());
    }

    @After
    public void clean() throws IOException {
        try {
            tempDir.toFile().delete();
            tempDir = Files.createTempDirectory(null);
        } catch (FileNotFoundException e) {

        }
    }

    @AfterClass
    public static void close() {
        mockServer.stop();
    }
    private RequestDataResult getSampleResult() {
        File signinsFile = new File(getClass().getClassLoader().getResource("sampleSignins.json").getFile());
        try {
            String content = FileUtils.readFileToString(signinsFile, "utf-8");
            JSONTokener tokener = new JSONTokener(content);
            JSONObject object = new JSONObject(tokener);
            return new RequestDataResult(object.getJSONArray("value"));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void FetchSendTest() throws InterruptedException, JSONException {
        int initialRequestsLength = mockServerClient.retrieveRecordedRequests(request().withMethod("POST")).length;
        ArrayList<JsonArrayRequest> requests = new ArrayList<>();
        requests.add(this::getSampleResult);
        FetchSendManager manager = new FetchSendManager(requests, senderParams, 10);
        manager.start();
        manager.pullAndSendData();
        Thread.sleep(2000);
        RequestDefinition[] recordedRequests = mockServerClient.retrieveRecordedRequests(request().withMethod("POST"));
        Assert.assertEquals(initialRequestsLength + 1, recordedRequests.length);
        Map<String,String> paramsMap=mapFromJSONObject(new JSONObject(recordedRequests[initialRequestsLength].toString()).getJSONObject("body"));
        Assert.assertEquals("aaaaa-bbbb-cccc-dddd-123456789", paramsMap.get("id"));
        Assert.assertEquals("John Smith", paramsMap.get("userDisplayName"));
        Assert.assertEquals("john.s@gmail.com", paramsMap.get("userPrincipalName"));
        manager.shutdown();
    }

    @Test
    public void awaitAndRetryTest() throws InterruptedException, JSONException {
        int initialRequestsLength = mockServerClient.retrieveRecordedRequests(request().withMethod("POST")).length;
        ArrayList<JsonArrayRequest> requests = new ArrayList<>();
        requests.add(this::getResultAfter2Retries);
        FetchSendManager manager = new FetchSendManager(requests, senderParams, 10);
        manager.start();
        manager.pullAndSendData();
        Thread.sleep(2000);
        RequestDefinition[] recordedRequests = mockServerClient.retrieveRecordedRequests(request().withMethod("POST"));
        Assert.assertEquals(initialRequestsLength + 1, recordedRequests.length);
        Map<String,String> paramsMap=mapFromJSONObject(new JSONObject(recordedRequests[initialRequestsLength].toString()).getJSONObject("body"));
        Assert.assertEquals(1, Integer.parseInt(paramsMap.get("key")));
        manager.shutdown();
    }


    @Test
    public void alwaysFalseRetryTest() throws InterruptedException {
        int initialRequestsLength = mockServerClient.retrieveRecordedRequests(request().withMethod("POST")).length;
        ArrayList<JsonArrayRequest> requests = new ArrayList<>();
        requests.add(this::getAlwaysFalseStatus);
        FetchSendManager manager = new FetchSendManager(requests, senderParams, 10);
        manager.start();
        manager.pullAndSendData();
        Thread.sleep(2000);
        RequestDefinition[] recordedRequests = mockServerClient.retrieveRecordedRequests(request().withMethod("POST"));
        Assert.assertEquals(initialRequestsLength, recordedRequests.length);
        manager.shutdown();
    }

    private RequestDataResult getResultAfter2Retries() {
        if (retries < 2) {
            retries++;
            RequestDataResult result = new RequestDataResult();
            result.setSucceed(false);
            return result;
        }

        try {
            JSONArray dataResult = new JSONArray("[{\"key\":1}]");
            return  new RequestDataResult(dataResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new RequestDataResult(new JSONArray());
    }

    private RequestDataResult getAlwaysFalseStatus() {
        RequestDataResult result = new RequestDataResult();
        result.setSucceed(false);
        return result;
    }

    @Test
    public void paginationTest() throws IOException, JSONException, AuthenticationException {
        MSGraphRequestExecutor requestExecutor = new MSGraphRequestExecutor(ApiTests.getSampleAzureADClient()
                , () -> "sampleAccessToken");
        JSONArray jsonArray = requestExecutor.getAllPages("http://localhost:8070/chainRequest");
        Assert.assertEquals(3, jsonArray.length());
    }

    @Test
    public void interruptMidSendTest() {
        int initialRequestsCount = mockServerClient.retrieveRecordedRequests(request().withMethod("POST")).length;
        ArrayList<JsonArrayRequest> requests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            requests.add(this::getSampleResult);
        }

        FetchSendManager manager = new FetchSendManager(requests, senderParams, 10);
        manager.start();
        Thread storageThread = new Thread(manager::pullAndSendData);
        storageThread.start();
        try {
            sleep(1000);
            storageThread.interrupt();
            sleep(3000);
            RequestDefinition[] recordedRequests = mockServerClient.retrieveRecordedRequests(request().withMethod("POST"));
            Assert.assertEquals(initialRequestsCount + 100, recordedRequests.length);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    private Map<String,String> mapFromJSONObject(JSONObject json){
        String[] paramString= json.getString("string").replaceAll("[{}\\\u0000\"]","")
                .split("\\r?\\n")[0].split(",");
        Map<String,String> paramsMap= new HashMap<>();
        for(String string: paramString){
            String[] params=string.split(":",2);
            paramsMap.put(params[0],params[1]);
        }

        return paramsMap;
    }
}
