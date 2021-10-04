package objects;

import utils.exceptions.ConfigurationException;

public interface JsonArrayRequest {
    RequestDataResult getResult() throws ConfigurationException;
}
