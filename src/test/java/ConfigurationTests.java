import main.MSClient;
import objects.MSGraphConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.error.YAMLException;
import java.io.File;
import java.io.FileNotFoundException;

public class ConfigurationTests {

    @Test
    public void loadMinimalConfigurationTest() throws FileNotFoundException {
        String testFileString = new File(getClass().getClassLoader().getResource("testMinimalConfig.yaml").getFile()).getAbsolutePath();
        MSGraphConfiguration configuration = (new MSClient(testFileString)).getConfiguration();
        Assert.assertNotSame(null, configuration);

        Assert.assertEquals("sample-client-id",configuration.getAzureADClient().getClientId());
        Assert.assertEquals("sample-tenant-id",configuration.getAzureADClient().getTenantId());
        Assert.assertEquals("sample+client:secret*",configuration.getAzureADClient().getClientSecret());
        Assert.assertEquals(300 ,configuration.getAzureADClient().getPullIntervalSeconds());

        Assert.assertEquals("sampleAccountToken",configuration.getSenderParams().getAccountToken());
        Assert.assertEquals("https://listener.logz.io:8071", configuration.getSenderParams().getListenerUrl());
        Assert.assertEquals(1000, configuration.getSenderParams().getDiskSpaceCheckInterval());
    }

    @Test
    public void loadFullConfigurationTest() throws FileNotFoundException {
        String testFileString = new File(getClass().getClassLoader().getResource("testFullConfig.yaml").getFile()).getAbsolutePath();
        MSGraphConfiguration configuration = (new MSClient(testFileString)).getConfiguration();
        Assert.assertNotSame(null, configuration);
        Assert.assertEquals("sample-client-id",configuration.getAzureADClient().getClientId());
        Assert.assertEquals("sample-tenant-id",configuration.getAzureADClient().getTenantId());
        Assert.assertEquals("sample+client:secret*",configuration.getAzureADClient().getClientSecret());
        Assert.assertEquals(600 ,configuration.getAzureADClient().getPullIntervalSeconds());


        Assert.assertEquals("sampleAccountToken",configuration.getSenderParams().getAccountToken());
        Assert.assertEquals("https://listener-eu.logz.io:8071", configuration.getSenderParams().getListenerUrl());
        Assert.assertEquals(1000, configuration.getSenderParams().getDiskSpaceCheckInterval());
        Assert.assertEquals(50000, configuration.getSenderParams().getInMemoryQueueCapacityInBytes());
        Assert.assertEquals(10000, configuration.getSenderParams().getLogsCountLimit());
        Assert.assertEquals("DEBUG", configuration.getLogLevel());
        Assert.assertFalse(configuration.getSenderParams().isFromDisk());
    }

    @Test
    public void missingConfigFileTest() {
        org.testng.Assert.assertThrows(FileNotFoundException.class,() -> {
            new MSClient("imaginaryConfigFile.yaml");
        });
    }

    @Test
    public void wrongParameterConfigTest() {
        org.testng.Assert.assertThrows(YAMLException.class, () -> {
                String testFileString = new File(getClass().getClassLoader().getResource("wrongParameterNameConfig.yaml").getFile()).getAbsolutePath();
                new MSClient(testFileString).getConfiguration();
        });
    }

    @Test
    public void missingParameterConfigTest() {
        org.testng.Assert.assertThrows(NullPointerException.class, () -> {
                String testFileString = new File(getClass().getClassLoader().getResource("missingParameterConfig.yaml").getFile()).getAbsolutePath();
                new MSClient(testFileString).getConfiguration();
        });
    }



}
