package smagellan.test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Created by vladimir on 7/22/16.
 */
public class ByteBuddyMisc {
    public void bbTest() throws  ReflectiveOperationException {
        Class<?> dynamicType = new ByteBuddy()
                .subclass(Object.class)
                .method(ElementMatchers.named("toString"))
                .intercept(FixedValue.value("Hello World!"))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        System.err.println(dynamicType.getConstructor().newInstance().toString());
    }



    public static void main(String[] args) throws ReflectiveOperationException {
        new ByteBuddyMisc().bbTest();
    }
}
