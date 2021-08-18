package api.office365Apis;

import api.MSGraphRequestExecutor;
import objects.RequestDataResult;
import org.apache.log4j.Logger;
import org.json.JSONException;
import utils.ApiUtil;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;

public abstract class Office365Api {
    protected static final Logger logger = Logger.getLogger(Office365Api.class);
    public static final String GREATER_OR_EQUEAL = " ge ";
    public static final String FILTER_PREFIX = "$filter=";
    public static final String PARAMETER_PREFIX="?";
    public static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String CREATED_DATE_TIME_FIELD = "createdDateTime";
    public static final String ACTIVITY_DATE_TIME_FIELD = "activityDateTime";
    public static final String GRAPH_API_URL = "https://graph.microsoft.com/v1.0/";
    public static final String MANAGEMENT_API_URL="https://management.azure.com/";
    public static final String SUBSCRIPTIONS="subscriptions/";
    public static final String PROVIDERS="providers/";
    public static final String MICROSOFT_SECURITY_PROVIDER="Microsoft.Security/";
    private final MSGraphRequestExecutor requestExecutor;
    private final int pullIntervalMillis; // in millis
    private final DateFormat dateFormat;

    public Office365Api(MSGraphRequestExecutor executor,int pullIntervalSeconds) {
        this.requestExecutor = executor;
        this.pullIntervalMillis =pullIntervalSeconds*1000;
        this.dateFormat = new SimpleDateFormat(ISO_8601);
        this.dateFormat.setTimeZone(TimeZone.getTimeZone(UTC));
    }

    protected RequestDataResult office365request(String api) {
        return getRequestDataResult(api);
    }

    private RequestDataResult getRequestDataResult(String api) {
        RequestDataResult dataResult = new RequestDataResult();
        try {
            dataResult.setData(requestExecutor.getAllPages(api,getAuthorizationManagerClass()));
            return dataResult;
        } catch (IOException | JSONException e) {
            logger.warn("error parsing response: " + e.getMessage(), e);
        } catch (AuthenticationException e) {
            logger.error(e.getMessage(), e);
        }
        dataResult.setSucceed(false);
        return dataResult;
    }

    public MSGraphRequestExecutor getRequestExecutor() {
        return requestExecutor;
    }

    public abstract RequestDataResult getApiRequest();

    public Class<?> getAuthorizationManagerClass() {
        return ApiUtil.getAuthorizationManagerClass(this.getClass());
    }

    public  String timeFilterSuffix(String timeField){
        Date fromDate=getDateTime();
        return PARAMETER_PREFIX+FILTER_PREFIX + timeField + GREATER_OR_EQUEAL + getDateFormat().format(fromDate);
    }

    protected Date getDateTime(){
        Date fromDate = new Date();
        fromDate.setTime(fromDate.getTime() - pullIntervalMillis);
        return fromDate;
    }

    protected DateFormat getDateFormat() {
        return dateFormat;
    }
}
