package smagellan.test.btrace;

import org.openjdk.btrace.core.annotations.*;

import static org.openjdk.btrace.core.BTraceUtils.*;

/*
 * This BTrace script inserts a probe into
 * method entry of java.lang.Thread.start() method.
 * At each Thread.start(), it raises a DTrace probe
 * in addition to printing the name of the thread.
 * A D-script like jthread.d may be used to get the
 * associated DTrace probe events.
 */
@BTrace
public class ThreadStartProbe {
    @OnMethod(
            clazz = "java.lang.Thread",
            method = "/.*/"
    )
    public static void onnewThread(@Self Thread t) {
        //D.probe("jthreadstart", Threads.name(t));
        println("starting " + Threads.name(t));
    }

    @OnMethod(
            clazz = "smagellan.test.btrace.ThreadSpawner",
            method = "/.*/"
    )
    public static void onSpawnThread(@Self Object thisObject, @ProbeMethodName String probeMethod) {
        println("onMethod; probeMethod: " + probeMethod);
    }
}



