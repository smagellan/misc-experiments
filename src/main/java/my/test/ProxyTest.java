package my.test;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by vladimir on 8/14/16.
 */
public class ProxyTest {
    public static void main(String[] args) {
        String s = "invHandler";
        Serializable t1 = createSerializable(s);
        System.err.println(t1.toString());
        System.err.println(t1.getClass());

        Serializable t2 = createSerializable(s);
        System.err.println(t2.toString());
        System.err.println(t2.getClass());

        System.err.println(t1.getClass() == t2.getClass());
    }

    private static Serializable createSerializable(final String s) {
        return (Serializable) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(), new Class[] {Serializable.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(s, args);
            }
        });
    }
}
