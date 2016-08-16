package regex;

import org.apache.commons.lang3.StringUtils;
import java.util.regex.Pattern;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Created by vladimir on 8/11/16.
 */
public class RegexTest {
    public static void main(String[] args) throws Exception {
        startJmh();
        //new RegexTest().splitTest(new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous."));
    }

    @Benchmark
    public void splitTest(Blackhole bh) throws Exception {
        String s = StringUtils.repeat(" ", 100_000);
        String s2 = s;//s + "," + s;
        Pattern regex = Pattern.compile("\\s*,\\s*");
        String tokens[] = regex.split(s2);
        bh.consume(tokens);
    }

    //@Benchmark
    public void matchTest(Blackhole bh) throws Exception {
        String s = StringUtils.repeat(" ", 100_000);
        Pattern regex = Pattern.compile("\\s*,\\s*");
        boolean b = regex.matcher(s).matches();
        bh.consume(b);
    }

    public static void startJmh() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(RegexTest.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(3)
                .forks(1)
                .addProfiler(StackProfiler.class)
                .build();
        new Runner(options).run();
    }
}
