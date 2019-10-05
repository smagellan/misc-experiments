package smagellan.test.lombok;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class LombokMain {
    public static void main(String[] args) throws IOException, NoSuchMethodException, IllegalAccessException {
        SimpleModule m = createInstantiatorsModule();

        ObjectMapper mapper = new ObjectMapper().registerModule(m);
        Child c = Child.builder().childName("c").parentName("p").build();

        String s = mapper.writeValueAsString(c);
        Child cr = mapper.readValue(s, Child.class);

        System.err.println(cr.equals(c));
        Child.builder();
    }

    @NotNull
    private static SimpleModule createInstantiatorsModule() throws NoSuchMethodException, IllegalAccessException {
        SimpleModule m = new SimpleModule();
        SimpleValueInstantiators instantiators = new SimpleValueInstantiators();
        instantiators.addValueInstantiator(Child.ChildBuilder.class, new MyValueInstantiator(Child.class));
        m.setValueInstantiators(instantiators);
        return m;
    }

}
