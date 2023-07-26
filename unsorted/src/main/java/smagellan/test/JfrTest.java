package smagellan.test;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;

import java.time.Duration;


public class JfrTest {
    private long totalNanos;

    public JfrTest() {
    }

    public void doWork() throws Exception {
        //Configuration.getConfiguration("default")
        try (RecordingStream stream = new RecordingStream()) {
            stream.enable("jdk.GarbageCollection").withoutStackTrace();
            stream.onEvent(this::onEvent);
            stream.startAsync();

            long s = 0;
            for (long i = 0; i < 25_000_000 * 100L; ++i) {
                s += new Object().hashCode();
            }
            System.err.println(s);
        }
        System.err.println("totalPauses ms: " + Duration.ofNanos(totalNanos).toMillis());
    }

    public void onEvent(RecordedEvent evt) {
        if (evt.getEventType().getName().equals("jdk.GarbageCollection")) {
            if (evt.hasField("sumOfPauses")) {
                totalNanos += evt.getDuration("sumOfPauses").toNanos();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        JfrTest test = new JfrTest();
        test.doWork();
    }
}
