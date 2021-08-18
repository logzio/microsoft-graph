package api.office365Apis;

import api.MSGraphRequestExecutor;
import objects.RequestDataResult;

public class RiskySignIns extends SignIns{

    private static final String AD_RISKY_SIGN_INS_FILTER_SUFFIX=" and (riskState eq 'atRisk' or riskState eq 'confirmedCompromised' or riskState eq 'unknownFutureValue')";

    public RiskySignIns(MSGraphRequestExecutor executor, int pullIntervalSeconds) {
        super(executor,pullIntervalSeconds);
    }

    @Override
    public RequestDataResult getApiRequest() {
        return office365request(GRAPH_API_URL+AD_SINGINS+timeFilterSuffix(CREATED_DATE_TIME_FIELD)+AD_RISKY_SIGN_INS_FILTER_SUFFIX);
    }
}
