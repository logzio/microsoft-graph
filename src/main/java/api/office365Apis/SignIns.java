package api.office365Apis;

import api.MSGraphRequestExecutor;
import objects.RequestDataResult;

public class SignIns extends Office365Api {

    protected static final String AD_SINGINS = "auditLogs/signIns";

    public SignIns(MSGraphRequestExecutor executor,int pullIntervalSeconds) {
        super(executor,pullIntervalSeconds);
    }

    @Override
    public RequestDataResult getApiRequest() {
        return office365request(GRAPH_API_URL + AD_SINGINS+timeFilterSuffix(CREATED_DATE_TIME_FIELD));
    }
}
