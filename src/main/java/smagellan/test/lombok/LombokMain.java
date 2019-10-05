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
        SimpleModule m = createInstantiatorsModule();

        ObjectMapper mapper = new ObjectMapper().registerModule(m);
        Child c = Child.builder().childName("c").parentName("p").build();

        String s = mapper.writeValueAsString(c);
        Child cr = mapper.readValue(s, Child.class);

        log.info("equals: {}", cr.equals(c));
    }

    @NotNull
    private static SimpleModule createInstantiatorsModule() {
        SimpleModule m = new SimpleModule();
        SimpleValueInstantiators instantiators = new SimpleValueInstantiators();
        instantiators.addValueInstantiator(Child.ChildBuilder.class, MethodHandleInstantiator.instantiatorFor(Child.class));
        m.setValueInstantiators(instantiators);
        return m;
    }
}
