package api.office365Apis;

import api.MSGraphRequestExecutor;
import objects.RequestDataResult;

public class DirectoryAudits extends Office365Api{

    private static final String AD_DIRECTORY_AUDITS = "auditLogs/directoryaudits";

    public DirectoryAudits(MSGraphRequestExecutor executor,int pullIntervalSeconds) {
        super(executor,pullIntervalSeconds);
    }

    @Override
    public RequestDataResult getApiRequest() {
        return office365request(GRAPH_API_URL+AD_DIRECTORY_AUDITS+timeFilterSuffix(ACTIVITY_DATE_TIME_FIELD));
    }
}
