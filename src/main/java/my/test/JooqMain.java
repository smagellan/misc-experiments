package my.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.ZoneOffset;

import javax.sql.DataSource;

import com.google.common.primitives.Ints;
import my.test.generated.jooq.Tables;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.types.DayToSecond;
import org.jooq.util.GenerationTool;
import org.jooq.util.jaxb.Configuration;
import org.jooq.util.jaxb.Database;
import org.jooq.util.jaxb.Generator;
import org.jooq.util.jaxb.Jdbc;
import org.jooq.util.jaxb.Target;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JooqMain {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
        //metadataTest();
        //clickhouseJooqTest(context);
        //metedataGeneration();
        //metadataSelection(context);
        //System.err.println(Tables.COLUMNS.NAME.toString());
        pgInsertTest(context);

        //DSLContext ctxt = context.getBean(DSLContext.class);
        //ZoneOffset offset = fetchMySqlTimezoneOffset(ctxt);
        //System.err.println(offset);
    }

    public static void pgSelectTest(ApplicationContext context) throws SQLException {
        DataSource ds = context.getBean("dataSource", DataSource.class);
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT c1, c2 FROM test_table")) {
                    System.err.println(stmt.getUpdateCount());
                    System.err.println(stmt.getUpdateCount());
                }
                System.err.println(stmt.getUpdateCount());
            }
        }
    }

    public static void pgInsertTest(ApplicationContext context) throws SQLException {
        DataSource ds = context.getBean("dataSource", DataSource.class);
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("INSERT into test_table VALUES (1,2), (3,4)");
                System.err.println(stmt.getUpdateCount());
                System.err.println(stmt.getUpdateCount());
                stmt.executeUpdate("DELETE FROM test_table WHERE c1=1");
                System.err.println(stmt.getUpdateCount());
                stmt.executeUpdate("DELETE FROM test_table WHERE c1=1");
                System.err.println(stmt.getUpdateCount());
            }
        }
    }

    public static void metadataSelection(ApplicationContext ctxt) {
        DataSource ds = ctxt.getBean("clickhouseDatasource", DataSource.class);
        DSLContext dslContext = createJooqDslContext(ds);
        dslContext.select(Tables.COLUMNS.NAME).from(Tables.COLUMNS).execute();
    }

    public static void metedataGeneration() throws Exception {
        Configuration configuration = new Configuration()
                .withJdbc(new Jdbc()
                        .withDriver("ru.yandex.clickhouse.ClickHouseDriver")
                        .withUrl("jdbc:clickhouse://localhost:8123/system")
                        .withUser("")
                        .withPassword(""))
                .withGenerator(new Generator()
                        .withDatabase(new Database()
                                .withName("my.test.jooq.clickhouse.ClickhouseJooqDatabase")
                                .withIncludes(".*")
                                .withExcludes("")
                                .withInputSchema("system"))
                        .withTarget(new Target()
                                .withPackageName("my.test.generated.jooq")
                                .withDirectory("src/generated-sources/jooq")));
        GenerationTool.generate(configuration);
    }

    public static void metadataTest() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:clickhouse://localhost:8123/system")){
            try(ResultSet rs = conn.getMetaData().getColumns(null, "system", "%", null)) {

            }
        }
    }

    public static void clickhouseJooqTest(ApplicationContext ctx) {
        DataSource ds = ctx.getBean("clickhouseDatasource", DataSource.class);
        DSLContext dslContext = createJooqDslContext(ds);
        Field<Integer> dummyField = DSL.field("dummy", Integer.class);
        Name systemOneTable = DSL.name("system.one");
        dslContext.select(dummyField)
                .from(systemOneTable)
                .where(dummyField.eq(DSL.value(0)))
                .execute();
    }

    private static DSLContext createJooqDslContext(DataSource ds) {
        return new DefaultDSLContext(ds, SQLDialect.DEFAULT,
                    new Settings()
                            .withRenderNameStyle(RenderNameStyle.AS_IS)
                            .withRenderSchema(false)
                            .withRenderCatalog(false));
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
