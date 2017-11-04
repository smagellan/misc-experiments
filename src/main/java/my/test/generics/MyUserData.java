package my.test.generics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

class MyUserData{
    private final double val;
    @JsonCreator
    public MyUserData(@JsonProperty("field") double val) {
        this.val = val;
    }

    @Override
    public String toString(){
        return "MyUserData[" + val + "]";
    }
}
