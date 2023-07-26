package smagellan.test;

public class JavaUtilLoggingViaLog4j2 {
    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }
    private static final java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(JavaUtilLoggingViaLog4j2.class.getName());

    public static void main(String[] args) {
        julLogger.info("jul logging");
    }
}
