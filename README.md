# Microsoft Graph API Integration
**Version 0.0.7**  [Change log](CHANGELOG.md)

You can ship logs available from the Microsoft Graph APIs with Logzio-MSGraph.
Logzio-MSGraph is a self-hosted application.

**Logzio-MSGraph supports these APIs:**

**Azure Active Directory:**

- Audit logs

- Sign-in logs (Including risky sign-in)

**ASC (Azure Security Center):**
- Alerts

There are many other APIs available through Microsoft Graph.
If you don't see your API in the list,
please [open an issue](https://github.com/logzio/microsoft-graph/issues/new) at GitHub to request it.

## To integrate Microsoft Graph and Logz.io

### 1. Register a new app in Azure Active Directory

In the Azure portal, go to [App registration](https://portal.azure.com/#blade/Microsoft_AAD_RegisteredApps/ApplicationsListBlade)
and select **New registration** from the top menu.

Name your app and click **Register**.

### 2. Create a client secret

Choose **Certificates & secrets** from the side menu,
and click on **New client secret**.

Add a **Description**.
We recommend something specific, such as "secret for Logzio-MSGraph integration".

In the **Expires** list, choose **Never**.

Click **Add**.

Copy the value of the generated secret to your text editor.
You'll need this later.

**Note**:
You won't be able to retrieve the secret's value after you leave this page.

### 3. Set the app's permissions

Choose **API permissions** from the side menu,
and click **Add a permission**.

Select **Microsoft Graph > Application permissions**.

Select the following items, for each desired API:

Sign Ins (Including risky sign ins) and Directory Audits:
* **AuditLog.Read.All**
* **Directory.Read.All**

ASC(Azure Security Center) Alerts:
* **SecurityEvents.Read.All**

Click **Add permissions**.

Click **Grant admin consent for Default Directory**, and then click **Yes** to confirm.

**Note**: Only Azure administrators can grant consent for Default Directory.
If the _Grant admin consent_ button is disabled, ask your Azure admin to update the setting for you.

### 4. Create a configuration file

Create a configuration yaml file (`logzio-msgraph-config.yaml`) for Logzio-MSGraph.

For a complete list of options, see the configuration parameters below.👇

```yaml
senderParams:
  accountToken: "<<SHIPPING-TOKEN>>"
  listenerUrl: "<<LISTENER-HOST>>"

azureADClient:
  pullIntervalSeconds: 300
  tenantId: "<<AD_TENANT_ID>>"
  clientId: "<<APP_CLIENT_ID>>"
  clientSecret: "<<APP_CLIENT_SECRET>>"

targetApis:
  ADApis:
    - <<supportedApi1>>
    - <<supportedApi2>>
  ASCApis:
    - <<supportedApi1>>

logLevel: INFO

additionalFields:
  <<KEY>>: "<<VALUE>>"
```

**Parameters**

| Parameter | Description |
|---|---|
| senderParams.accountToken | **Required**. Replace `<<SHIPPING-TOKEN>>` with the [token](https://app.logz.io/#/dashboard/settings/general) of the account you want to ship to. |
| senderParams.listenerUrl | **Default**: `listener.logz.io` <br> Listener URL. <br> Replace `<<LISTENER-HOST>>` with your region's listener host (for example, `listener.logz.io`). For more information on finding your account's region, see [Account region](https://docs.logz.io/user-guide/accounts/account-region.html). |
| senderParams.fromDisk | **Default**: `true` <br> If `true`, logs are stored on disk until they're shipped (see [If from-disk=true](#if-fromdisk-true)). If `false`, logs persist in memory until they're shipped (see [If from-disk=false](#if-fromdisk-false)). |
| senderParams.senderDrainIntervals | **Default**: `30` <br> How often the sender should drain the queue, in seconds. |
| azureADClient.tenantId | **Required**. Azure Active Directory tenant ID. <br> You can find this in the _Overview_ section of the app you registered in step 1. |
| azureADClient.clientId | **Required**. Application client ID. <br> You can find this in the _Overview_ section of the app you registered in step 1. |
| azureADClient.clientSecret | **Required**. The Application Client Secret you created in step 2. |
| azureADClient.pullIntervalSeconds | **Default**: `300` <br>  Time interval, in seconds, to pull the logs with the Graph API. |
| targetApis | **Required**. <br> Specifies types of api lists to run, each API provider has its own list. Must contain at least 1 list with 1 api in the list. Current supported providers: ADApis, ASCApis. |
| targetApis.ADApis | **Optional**. <br>  List of AD apis to run. Current supported apis: directoryAudits, signIns (this includes risky sign ins), riskySignIns. All apis are case sensitive and should be configured as mentioned here. |
| targetApis.ASCApis | **Optional**. <br>  List of ASC apis to run. Current supported apis: alerts. All apis are case sensitive and should be configured as mentioned here. |
| logLevel | **Default**: `INFO` <br> Log level for Logizo-MSGraph to omit. Can be one of: `OFF`, `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE`, `ALL`. |
| additionalFields | **Optional**. <br> List of additional fields to be added to the sent data. Pairs of key and value.

#### <span id="if-fromdisk-true">If fromDisk=true</span>

| Parameter | Description |
|---|---|
| senderParams.fileSystemFullPercentThreshold | **Default**: `98` <br> Threshold percentage of disk space at which to stop queueing. If this threshold is reached, all new logs are dropped until used space drops below the threshold. Set to `-1` to ignore threshold. |
| senderParams.gcPersistedQueueFilesIntervalSeconds | **Default**: `30` <br> Time interval, in seconds, to clean sent logs from the disk. |
| senderParams.diskSpaceCheckInterval | **Default**: `1000` <br> Time interval, in milliseconds, to check for disk space. |

#### <span id="if-fromdisk-false">If fromDisk=false</span>

| Parameter | Description |
|---|---|
| senderParams.inMemoryQueueCapacityInBytes | **Default**: `1024 * 1024 * 100` <br> The amount of memory, in bytes, Logzio-MSGraph can use for the memory queue. Set to `-1` for unlimited bytes. |
| senderParams.logsCountLimit | **Default**: `-1` <br> The number of logs in the memory queue before dropping new logs. Set to `-1` to configure the sender to not limit the queue by logs count. |

### 5. Download and run Logzio-MSGraph

You can launch Logzio-MSGraph in a Docker container or as a standalone Java app.

In a Docker container:

```shell
docker run -d -v $(pwd)/logzio-msgraph-config.yaml:/config.yaml logzio/logzio-msgraph
```

Or to run as a standalone Java app,
download the latest jar from the [release page](https://github.com/logzio/microsoft-graph/releases)
and run:

```shell
java -jar logzio-msgraph.jar logzio-msgraph-config.yaml
```

Logs collected by this integration will have the type `Microsoft-Graph`

### 6. Check Logz.io for your logs

Give your logs some time to get from your system to ours, and then open [Kibana](https://app.logz.io/#/dashboard/kibana).

If you still don't see your logs, see [log shipping troubleshooting]({{site.baseurl}}/user-guide/log-shipping/log-shipping-troubleshooting.html).