package smagellan.test.generics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonDeserialization {
    public static void main(String[] args) throws IOException {
        deserializationVer1_1();
    }

    private static void deserializationVer1() throws IOException {
        String jsonBody = "[{\"field\":23}, {\"field\":24}]";
        ObjectMapper mapper = new ObjectMapper();
        List lst = mapper.readValue(jsonBody, new ArrayList<MyUserData>().getClass());
        lst.forEach( el -> System.err.println("el class: " + el.getClass() + "; val: " + el));
    }

    private static void deserializationVer1_1() throws IOException {
        String jsonBody = "[{\"field\":23}, {\"field\":24}]";
        ObjectMapper mapper = new ObjectMapper();
        MyUserData[] lst = mapper.readValue(jsonBody, MyUserData[].class);
        Arrays.stream(lst).forEach( el -> System.err.println("el class: " + el.getClass() + "; val: " + el));
    }

    private static void deserializationVer2() throws IOException {
        String jsonBody = "[{\"field\":23}]";
        ObjectMapper mapper = new ObjectMapper();
        List lst = mapper.readValue(jsonBody, List.class);
        lst.forEach( el -> System.err.println("el class: " + el.getClass() + "; val: " + el));
    }

    private static void deserializationVer3() throws IOException {
        String jsonBody = "[{\"field\":23}, {\"field\":24}]";
        ObjectMapper mapper = new ObjectMapper();
        TypeReference typeReference = new TypeReference<List<MyUserData>>() { };
        List<MyUserData> lst = mapper.readValue(jsonBody, typeReference);
        lst.forEach( el -> System.err.println("el class: " + el.getClass() + "; val: " + el));
    }

    private static void deserializationVer4() throws IOException {
        String jsonBody = "[{\"field\":23}, {\"field\":24}]";
        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = TypeFactory
                .defaultInstance()
                .constructCollectionType(ArrayList.class, MyUserData.class);
        List<MyUserData> lst = mapper.readValue(jsonBody, javaType);
        lst.forEach( el -> System.err.println("el class: " + el.getClass() + "; val: " + el));
    }
}


