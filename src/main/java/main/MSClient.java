package main;

import api.MSGraphRequestExecutor;
import api.Office365Apis;
import objects.JsonArrayRequest;
import objects.MSGraphConfiguration;
import objects.RequestDataResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import utils.exceptions.ConfigurationException;

import javax.naming.AuthenticationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class MSClient {

    private static final Logger logger = Logger.getLogger(MSClient.class);
    private MSGraphConfiguration configuration;

    public MSClient(String configFile) throws FileNotFoundException {
        configuration = loadMSGraphConfig(configFile);
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        root.setLevel(Level.toLevel(configuration.getLogLevel()));
    }

    public void start() throws ConfigurationException {
        if (getConfiguration() == null) {
            return;
        }

        MSGraphRequestExecutor executor;
        try {

            executor = new MSGraphRequestExecutor(configuration.getAzureADClient());
        } catch (AuthenticationException e) {
            logger.error(e.getMessage(), e);
            return;
        }
        Office365Apis officeApis = new Office365Apis(executor);
        ArrayList<JsonArrayRequest> requests = null;
        requests = new ArrayList(getApiTargets(officeApis));
        FetchSendManager manager = new FetchSendManager(requests, configuration.getSenderParams(), configuration.getAzureADClient().getPullIntervalSeconds());
        manager.start();
    }

    /**
     *
     * @param office365Apis
     * @return Method reference list matching yaml api configuration
     */
    private List<JsonArrayRequest> getApiTargets(Office365Apis office365Apis) throws ConfigurationException {
        List<String> adApis = configuration.getTargetApi().getADApis();
        List<String> apiMethods = new java.util.LinkedList<>();

        if (adApis != null) {
            apiMethods.addAll(adApis.stream().map(api -> "get" + StringUtils.capitalize(api)).collect(Collectors.toList()));
        }

        List<JsonArrayRequest> apis = new ArrayList<>();
        for (String api : apiMethods) {
            apis.add(() -> (RequestDataResult) getApiMethodRef(office365Apis, api));
            logger.info("Initialized api: " + api);
        }

        if(apis.size()!=apiMethods.size()){
            throw new ConfigurationException("Invalid configuration of apis in configuration yaml, review the configured apis: "+adApis);
        }
        return apis;
    }

    private Callable<RequestDataResult> getApiMethodRef(Office365Apis office365Apis, String api) throws ConfigurationException {
        RequestDataResult res = null;
        try {
            res = (RequestDataResult) office365Apis.getClass().getMethod(api).invoke(office365Apis);
        } catch (ReflectiveOperationException illegalAccessException) {
            throw new ConfigurationException("Invalid configuration of apis in configuration yaml");
        }
        return (Callable<RequestDataResult>) res;
    }

    public MSGraphConfiguration getConfiguration() {
        return this.configuration;
    }

    private MSGraphConfiguration loadMSGraphConfig(String yamlFile) throws FileNotFoundException {
        Yaml yaml = new Yaml(new Constructor(MSGraphConfiguration.class));
        InputStream inputStream = new FileInputStream(new File(yamlFile));
        MSGraphConfiguration config = yaml.load(inputStream);

        checkNotNull(config.getSenderParams(), "Config file format error, logzioSenderParameters can't be empty");
        checkNotNull(config.getAzureADClient(), "Config file format error, azureADClient can't be empty");
        checkNotNull(config.getSenderParams().getAccountToken(), "Parameter logzioSenderParameters.accountToken is mandatory");
        checkNotNull(config.getAzureADClient().getTenantId(), "Parameter azureADClient.tenantId is mandatory");
        checkNotNull(config.getAzureADClient().getClientId(), "Parameter azureADClient.clientId is mandatory");
        checkNotNull(config.getAzureADClient().getClientSecret(), "Parameter azureADClient.clientSecret is mandatory");
        checkNotNull(config.getTargetApi(), "Parameter targetApi is mandatory");
        return config;
    }
}