package smagellan.test;

import com.example.tutorial.protos.Person;
import com.google.protobuf.TextFormat;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ProtobufTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProtobufTest.class);

    public static void main(String[] args) throws TextFormat.ParseException {
        Person john =
                Person.newBuilder()
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
        String johnString = TextFormat.printer().printToString(john).replace("\n", " ");
        logger.info("johnString: {}", johnString);
        Person.Builder bldr = Person.newBuilder();
        TextFormat.getParser().merge(johnString, bldr);
        logger.info("equals: {}", Objects.equals(bldr.build(), john));
    }
}
