package api.office365Apis;

import api.MSGraphRequestExecutor;
import objects.RequestDataResult;

public class ASCAlerts extends Office365Api{

    private static final String ASC_ALERTS="security/alerts";

    public ASCAlerts(MSGraphRequestExecutor executor,int pullIntervalSeconds) {
        super(executor,pullIntervalSeconds);
    }

    @Override
    public RequestDataResult getApiRequest() {
        return  office365request(GRAPH_API_URL+ASC_ALERTS+timeFilterSuffix(CREATED_DATE_TIME_FIELD));
    }
}
