package api.office365Apis;

import api.MSGraphRequestExecutor;
import objects.RequestDataResult;

import java.util.Date;

public class ASCTasks extends Office365Api {

    private static final String ASC_TASKS = "tasks";
    private static final String API_VERSION = "&api-version=2015-06-01-preview";
    private static final String CREATION_TIME_UTC_FIELD = "properties/creationTimeUtc";
    private final String subscriptionId;

    public ASCTasks(MSGraphRequestExecutor executor, String subscriptionId, int pullIntervalSeconds) {
        super(executor, pullIntervalSeconds);
        this.subscriptionId = subscriptionId;
    }

    @Override
    public RequestDataResult getApiRequest() {
        return office365request(MANAGEMENT_API_URL + SUBSCRIPTIONS + subscriptionId + "/" + PROVIDERS + MICROSOFT_SECURITY_PROVIDER
                + ASC_TASKS + timeFilterSuffix(CREATION_TIME_UTC_FIELD) + API_VERSION);
    }

    @Override
    public String timeFilterSuffix(String timeField) {
        Date fromDate = getDateTime();
        return PARAMETER_PREFIX + FILTER_PREFIX + timeField + GREATER_OR_EQUEAL + getDateFormat().format(fromDate);
    }
}
