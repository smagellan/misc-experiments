import java.sql.Timestamp;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.primitives.Ints;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.InsertOnDuplicateSetMoreStep;
import org.jooq.InsertOnDuplicateStep;
import org.jooq.InsertValuesStepN;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.types.DayToSecond;
import org.jooq.util.mysql.MySQLDSL;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JooqMain {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
        DSLContext ctxt = context.getBean(DSLContext.class);
        ZoneOffset offset = fetchMySqlTimezoneOffset(ctxt);
        System.err.println(offset);
    }

    public static ZoneOffset fetchMySqlTimezoneOffset0(DSLContext ctxt) {
        Field<String> zoneOffsetField = DSL.field("zone_offset", String.class);
        String offset = ctxt
                .select(utcOffset().cast(String.class).as(zoneOffsetField))
                .fetchOne(zoneOffsetField);
        if (!(offset.startsWith("-") || offset.startsWith("+"))) {
            offset = "+" + offset;
        }
        return ZoneOffset.of(offset);
    }

    public static ZoneOffset fetchMySqlTimezoneOffset(DSLContext ctxt) {
        Field<DayToSecond> zoneOffsetField = DSL.field("zone_offset", DayToSecond.class);
        DayToSecond offset = ctxt
                .select(utcOffset().as(zoneOffsetField))
                .fetchOne(zoneOffsetField);
        return ZoneOffset.ofHours(Ints.checkedCast(Duration.ofMillis(offset.longValue()).toHours()));
    }


    /**
     * @param ctxt используемый DSL
     * @return текущий timestamp на сервере
     * @see DSL#currentTimestamp()
     */
    public static Timestamp fetchMySqlCurrentTimestamp(DSLContext ctxt) {
        Field<Timestamp> tsField = DSL.field("cur_time", Timestamp.class);
        return ctxt
                .select(DSL.currentTimestamp().as(tsField))
                .fetchOne(tsField);
    }

    public static Field<DayToSecond> utcOffset() {
        return DSL.timestampDiff(DSL.currentTimestamp(), utcTimestamp());
    }

    public static Field<Timestamp> utcTimestamp() {
        return DSL.function("utc_timestamp", Timestamp.class);
    }
}
