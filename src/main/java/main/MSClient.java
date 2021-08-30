package main;

import api.MSGraphRequestExecutor;
import api.office365Apis.Office365Api;
import objects.JsonArrayRequest;
import objects.MSGraphConfiguration;
import objects.RequestDataResult;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import utils.ApiUtil;
import utils.exceptions.ConfigurationException;

import javax.naming.AuthenticationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class MSClient {

    private static final Logger logger = Logger.getLogger(MSClient.class);
    private final MSGraphConfiguration configuration;

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

        ArrayList<JsonArrayRequest> requests = new ArrayList(getApiTargets(executor,getApiClassNames()));
        FetchSendManager manager = new FetchSendManager(requests,configuration);
        manager.start();
    }

    /**
     * @param executor
     * @param apiClassNames
     * @return Method reference list matching yaml api configuration
     */
    private List<JsonArrayRequest> getApiTargets(MSGraphRequestExecutor executor, List<String> apiClassNames) throws ConfigurationException {
        List<JsonArrayRequest> apis = new ArrayList<>();
        for (String api : apiClassNames) {
            apis.add(() -> getApiMethodRef(executor, api));
            logger.info("Initialized api: " + api);
        }

        if (apis.size() != apiClassNames.size()) {
            throw new ConfigurationException("Invalid configuration of apis in configuration yaml, review the configured apis: " + apis);
        }

        return apis;
    }

    /**
     *
     * @return List of api class names to use
     * @throws ConfigurationException
     */
    private List<String> getApiClassNames() throws ConfigurationException {
        List<String> adApis = configuration.getTargetApi().getADApis();
        List<String> ascApis = configuration.getTargetApi().getAscApis();
        List<String> apiMethods = new java.util.LinkedList<>();
        if (adApis != null) {
            apiMethods.addAll(adApis.stream().map(StringUtils::capitalize).collect(Collectors.toList()));
        }

        if (ascApis != null) {
            apiMethods.addAll(ascApis.stream().map(name->"ASC"+StringUtils.capitalize(name)).collect(Collectors.toList()));
        }

        if (apiMethods.size() ==0) {
            throw new ConfigurationException("Invalid configuration of apis in configuration yaml, review the configured apis: " + adApis+ascApis);
        }

        return apiMethods;
    }

    /** Invokes a constructor from the supplied class name.
     *  Constructor parameters are taken from the MSGraphConfiguration.AzureAdClient object.
     *  All of the constructor parameters except MSGraphRequestExecutor must match the AzureAdClient object names.
     * @param executor
     * @param api Api class name, excluding package prefix
     * @return
     * @throws ConfigurationException
     */
    private RequestDataResult getApiMethodRef(MSGraphRequestExecutor executor, String api) throws ConfigurationException {
        RequestDataResult res;
        try {
            java.lang.reflect.Constructor<?> constructor=ClassUtils.getClass(ApiUtil.getApisPackageName()+api).getDeclaredConstructors()[0];
            if(constructor.getParameterCount()>1) {
                Parameter[] constructorParams = constructor.getParameters();
                Object [] reflectionParams = new Object[constructorParams.length];
                reflectionParams[0]=executor;
                for (int i=1; i<constructorParams.length;i++) {
                    reflectionParams[i]=(FieldUtils.readField(configuration.getAzureADClient(),constructorParams[i].getName(),
                            true));
                 }

                res=((Office365Api)constructor.newInstance(reflectionParams)).getApiRequest();
            }
        else {
                res=((Office365Api)constructor.newInstance(executor)).getApiRequest();
            }
        } catch (ReflectiveOperationException illegalAccessException) {
            throw new ConfigurationException("Invalid configuration of apis in configuration yaml");
        }
        return res;
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
        checkNotNull(checkApiCount(config), "At least one api must be specified in configuration file");
        checkNotNull(checkSubscriptionIdForTasksApi(config),"Subscription id is mandatory when using ASC tasks api");
        return config;
    }

    private Boolean checkSubscriptionIdForTasksApi(MSGraphConfiguration config) {
        if((config.getTargetApi().getAscApis() !=null && config.getTargetApi().getAscApis().contains("tasks")
        )&& (config.getAzureADClient().getSubscriptionId()==null || config.getAzureADClient().getSubscriptionId().isEmpty())){
            return null;
        }

        return Boolean.TRUE;
    }

    private Boolean checkApiCount(MSGraphConfiguration config) {
        List<String> ascApis = config.getTargetApi().getAscApis();
        List<String> adApis = config.getTargetApi().getADApis();
        int apiCount = 0;
        apiCount+=ascApis!=null?ascApis.size():0;
        apiCount+=adApis!=null?adApis.size():0;
        return apiCount > 0 ? Boolean.TRUE : null;
    }
}