package objects;


import org.json.JSONArray;

public class RequestDataResult {
    private boolean succeed;
    private JSONArray data;

    public RequestDataResult() {
        this.succeed = true;
    }

    public RequestDataResult(JSONArray data) {
        this.data = data;
        this.succeed = true;
    }

    public void setRequestDataResult(RequestDataResult res) {
        this.succeed = res.isSucceed();
        this.data = res.data;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public JSONArray getData() {
        return data;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }
}