package my.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


public class TestPhasesExample {
    @ClassRule
    public static MyRule classRule = new MyRule("@ClassRule");

    @Rule
    public MyRule instanceRule = new MyRule("@Rule");

    @BeforeClass
    public static void beforeClass() {
        System.err.println("beforeClass");
    }

    @AfterClass
    public static void afterClass() {
        System.err.println("afterClass");
    }

    @Before
    public void before() {
        System.err.println("before");
    }

    @After
    public void after() {
        System.err.println("after");
    }

    @Test
    public void test1() {
        System.err.println("test1");
    }


    @Test
    public void test2() {
        System.err.println("test2");
    }

    @Test
    public void test3() {
        System.err.println("test2");
    }

    @Test
    public void test4() {
        System.err.println("test3");
    }
}


class MyRule implements TestRule {
    private final String name;

    MyRule(String name) {
        this.name = name;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        System.err.println(String.format("apply (%s)", name));
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                System.err.println(String.format("evaluate (%s)", name));
                base.evaluate();
            }
        };
    }
}