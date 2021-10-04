package objects;

import java.util.List;

public class TargetApi {

    private List<String> ADApis;
    private List<String> ASCApis;

    public TargetApi() {
    }

    public List<String> getADApis() {
        return ADApis;
    }

    public void setADApis(List<String> ADApis) {
        this.ADApis = ADApis;
    }

    public List<String> getAscApis() {
        return ASCApis;
    }

    public void setASCApis(List<String> ASCApis) {
        this.ASCApis = ASCApis;
    }
}
