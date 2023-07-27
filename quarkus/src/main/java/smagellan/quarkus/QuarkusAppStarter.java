package smagellan.quarkus;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.util.Arrays;

@QuarkusMain
public class QuarkusAppStarter implements QuarkusApplication {
    public static void main(String[] args) {
        Quarkus.run(QuarkusAppStarter.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        System.out.println("Hello " + Arrays.asList(args));
        return 0;
    }
}
