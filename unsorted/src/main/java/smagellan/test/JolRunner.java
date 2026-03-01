package smagellan.test;

import org.openjdk.jol.info.GraphLayout;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JolRunner {
    public static void main(String[] args) {
        Map<String, String> obj = IntStream.range(0, 10_000)
                .boxed()
                .map(Object::toString)
                .collect(Collectors.toMap(Function.identity(), Function.identity()));
        //needs -Djdk.attach.allowAttachSelf  -XX:+EnableDynamicAgentLoading
        //-Djdk.instrument.traceUsage
        //WARNING: Unable to attach Serviceability Agent. You can try again with escalated privileges. Two options: a) use -Djol.tryWithSudo=true to try with sudo; b) echo 0 | sudo tee /proc/sys/kernel/yama/ptrace_scope
        String footprint = GraphLayout.parseInstance(obj).toFootprint();
        System.err.println(footprint);
    }
}
