package smagellan.test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.MessageLite;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import com.example.tutorial.protos.Person;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.*;

public class KryoVsProtobufTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(KryoVsProtobufTest.class);

    private static final Random rnd = new Random(42);
    private static final Kryo kryo = buildKryo();
    private static final Collection<Person> pojos = buildPojos();

    private static Collection<Person> buildPojos() {
        List<Person> result = new ArrayList<>();
        for (int i = 0; i < 1000; ++i) {
            Person p = Person.newBuilder()
                    .setId(1234)
                    .setName("John Doe")
                    .setEmail("jdoe@example.com")
                    .setTemp(35.9)
                    .addPhones(
                            Person.PhoneNumber.newBuilder()
                                    .setNumber("555-4321")
                                    .setType(Person.PhoneType.HOME)
                    )
                    .build();
            result.add(p);
        }
        return result;
    }

    public static void main(String[] args) throws RunnerException {
        buildKryo();
        runJmh();
    }

    private static Kryo buildKryo() {
        Kryo ret = new Kryo();
        ret.register(Person.class);
        ret.register(Collections.unmodifiableList(new ArrayList<>()).getClass());
        ret.register(Person.PhoneNumber.class);
        ret.register(com.google.protobuf.UnknownFieldSet.class);
        ret.register(TreeMap.class);
        return ret;
    }

    private static void runJmh() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(KryoVsProtobufTest.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(2)
                .forks(1)
                .build();
        new Runner(options).run();
    }

    @Benchmark
    public Object kryoTest(MyState state) {
        ByteBufferOutput bos = new ByteBufferOutput(2048);
        int totalsHash = 0;
        long totalLength = 0;

        for (Person pojo : pojos) {
            kryo.writeObject(bos, pojo);
            byte[] bytes = bos.toBytes();
            //is it really required?
            totalsHash += Arrays.hashCode(bytes);
            totalLength += bytes.length;
            bos.reset();
        }
        state.setTotalKryoSize(totalLength);
        return totalsHash;
    }

    @Benchmark
    public Object protobufTest(MyState state) throws IOException {
        int totalsHash = 0;
        long totalLength = 0;

        byte[] bytes = null;
        for (Person pojo : pojos) {
            if (bytes == null || bytes.length < pojo.getSerializedSize()) {
                bytes = new byte[pojo.getSerializedSize()];
            }
            serializeTo(pojo, bytes);
            totalsHash += Arrays.hashCode(bytes);
            totalLength += bytes.length;
        }
        state.setTotalProtoBufSize(totalLength);
        return totalsHash;
    }

    private void serializeTo(MessageLite src, byte[] dest) throws IOException {
        CodedOutputStream output = CodedOutputStream.newInstance(dest, 0, src.getSerializedSize());
        src.writeTo(output);
        output.checkNoSpaceLeft();
    }

    @State(Scope.Benchmark)
    public static class MyState {
        private long totalKryoSize;
        private long totalProtoBufSize;

        public long getTotalKryoSize() {
            return totalKryoSize;
        }

        public void setTotalKryoSize(long totalKryoSize) {
            this.totalKryoSize = totalKryoSize;
        }

        public long getTotalProtoBufSize() {
            return totalProtoBufSize;
        }

        public void setTotalProtoBufSize(long totalProtoBufSize) {
            this.totalProtoBufSize = totalProtoBufSize;
        }

        @TearDown
        public void teardown() {
            KryoVsProtobufTest.logger.info("totalKryoSize: {}, totalProtoBufSize: {}", totalKryoSize, totalProtoBufSize);
        }
    }
}


