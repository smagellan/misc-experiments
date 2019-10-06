package smagellan.test.lombok;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Slf4j
public class LombokMain {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = createObjectMapper1();
        Child c = Child.builder().childName("c").parentName("p").build();

        String s = mapper.writeValueAsString(c);
        Child cr = mapper.readValue(s, Child.class);

        log.info("equals: {}", cr.equals(c));
    }

    private static ObjectMapper createObjectMapper1() {
        SimpleModule m = createInstantiatorsModule();
        return new ObjectMapper().registerModule(m);
    }

    @NotNull
    private static SimpleModule createInstantiatorsModule() {
        SimpleModule m = new SimpleModule();
        SimpleValueInstantiators instantiators = builderInstantiatorsFor(Child.class);
        m.setValueInstantiators(instantiators);
        return m;
    }

    @NotNull
    private static SimpleValueInstantiators builderInstantiatorsFor(Class<?>... classes) {
        SimpleValueInstantiators instantiators = new SimpleValueInstantiators();
        for (Class<?> cls : classes) {
            Class<?> builderClass = MethodHandleInstantiator.getBuilderClassForEntityClass(cls);
            instantiators.addValueInstantiator(builderClass, MethodHandleInstantiator.instantiatorFor(cls, builderClass));
        }
        return instantiators;
    }
}
