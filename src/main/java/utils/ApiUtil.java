package utils;

import api.authorization.Authorizer;
import api.authorization.AzureADAuthorizationManager;
import api.authorization.AzureManagementAuthorizationManager;
import api.office365Apis.*;

import java.util.HashMap;
import java.util.Map;

public class ApiUtil {
    private static final Map<Class<? extends Office365Api>,Class<? extends Authorizer>> apiTargetToAuthenticationManager;
    private static final String apiPackageName=Office365Api.class.getPackageName();

    static{
        apiTargetToAuthenticationManager= new HashMap<>();
        apiTargetToAuthenticationManager.put(SignIns.class,AzureADAuthorizationManager.class);
        apiTargetToAuthenticationManager.put(RiskySignIns.class,AzureADAuthorizationManager.class);
        apiTargetToAuthenticationManager.put(DirectoryAudits.class,AzureADAuthorizationManager.class);
        apiTargetToAuthenticationManager.put(ASCAlerts.class,AzureADAuthorizationManager.class);
        apiTargetToAuthenticationManager.put(ASCTasks.class, AzureManagementAuthorizationManager.class);
    }

    public static Class<? extends Authorizer> getAuthorizationManagerClass(Class<?> apiTargetClass){
        return apiTargetToAuthenticationManager.get(apiTargetClass);
    }

    public static String getApisPackageName(){
        return apiPackageName+".";
    }
}
