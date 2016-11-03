package my.test;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//original source came from coderanch thread: /t/384379/java/extract-generic-type-information-typed
public class TypeErasureTest {
    private Map<String, String> strings = new HashMap<>();
    public String getString() {
        return "s";
    }
    public void setString(String s) { }
    public <T> T getTyped() {
        return null;
    }
    public <T> void setTyped(T s) { }
    public Map<String, String> getStrings() {
        return strings;
    }
    public void setStrings(Map<String, String> strings) {
        this.strings = strings;
    }

    public static Class<?>[] getActualTypeArguments(String property,Class clasz) throws IntrospectionException {
        List<Class<?>> types = new ArrayList<>();
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(property, clasz);
        Method method = propertyDescriptor.getReadMethod();
        ParameterizedType genericReturnType = (ParameterizedType)method.getGenericReturnType();
        Type[] actualTypeArguments = genericReturnType.getActualTypeArguments();
        for ( int i=0 ; i < actualTypeArguments.length ; i ++) {
            Class<?> type = (Class<?>) (actualTypeArguments[i]); // could be class or interface
            types.add(type);
        }
        return types.toArray(new Class<?>[types.size()]);
    }

    public static void main(String[] s) throws Exception {
        Class<?>[] classes = getActualTypeArguments("strings", TypeErasureTest.class);
        for (Class cls:classes) {
            System.err.println("cls: " + cls);
        }

        //classes = getActualTypeArguments("string", my.test.TypeErasureTest.class);
        //for (Class cls:classes) {
        //    System.err.println("cls: " + cls);
        //}

        classes = getActualTypeArguments("typed", TypeErasureTest.class);
        for (Class cls:classes) {
            System.err.println("cls: " + cls);
        }
    }

    public static class MyCls<T> {

    }
}