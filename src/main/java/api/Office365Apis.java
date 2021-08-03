package api;

import objects.RequestDataResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;

import javax.naming.AuthenticationException;
import java.io.IOException;

public class Office365Apis {

    private static final Logger logger = Logger.getLogger(Office365Apis.class);
    private static final String AD_SINGINS = "auditLogs/signIns";
    private static final String CREATED_DATE_TIME_FIELD = "createdDateTime";
    private static final String AD_DIRECTORY_AUDITS = "auditLogs/directoryaudits";
    private static final String ACTIVITY_DATE_TIME_FIELD = "activityDateTime";
    private static final String GRAPH_API_URL = "https://graph.microsoft.com/v1.0/";
    private static final String AD_RISKY_SIGN_INS_FILTER_SUFFIX=" and (riskState eq 'atRisk' or riskState eq 'confirmedCompromised' or riskState eq 'unknownFutureValue')";
    private final MSGraphRequestExecutor requestExecutor;

    public Office365Apis(MSGraphRequestExecutor executor) {
        this.requestExecutor = executor;
    }

    private RequestDataResult office365request(String api, String dateField, String optionalFilter) {
        return getRequestDataResult(api, dateField, optionalFilter);
    }

    private RequestDataResult office365request(String api, String dateField) {
        return getRequestDataResult(api, dateField, StringUtils.EMPTY);
    }

    private RequestDataResult getRequestDataResult(String api, String dateField, String optionalFilter) {
        RequestDataResult dataResult = new RequestDataResult();
        try {
            dataResult.setData(requestExecutor.getAllPages(api, dateField, optionalFilter));
            return dataResult;
        } catch (IOException | JSONException e) {
            logger.warn("error parsing response: " + e.getMessage(), e);
        } catch (AuthenticationException e) {
            logger.error(e.getMessage(), e);
        }
        dataResult.setSucceed(false);
        return dataResult;
    }

    public RequestDataResult getSignIns() {
        return office365request(GRAPH_API_URL +AD_SINGINS ,CREATED_DATE_TIME_FIELD);
    }

    public RequestDataResult getDirectoryAudits() {
        return office365request(GRAPH_API_URL +AD_DIRECTORY_AUDITS, ACTIVITY_DATE_TIME_FIELD);
    }

    public RequestDataResult getRiskySignIns(){
        return office365request(GRAPH_API_URL+AD_SINGINS,CREATED_DATE_TIME_FIELD,AD_RISKY_SIGN_INS_FILTER_SUFFIX);
    }
}