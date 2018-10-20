package smagellan.test.generics;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ErasureExample {
    public static void main(String[] args) throws NoSuchMethodException {
        List<Date> lst = new ArrayList<>();
        List<String> lst2 = new ArrayList<>();

        System.err.println("classes are equal: " + lst.getClass().equals(lst2.getClass()));

        System.err.println(lst.getClass().toGenericString());
        System.err.println(new MyGenericClass<Date>().getClass().toGenericString());


        System.err.println(Arrays.asList(lst.getClass().getMethod("remove", int.class)));


        //System.err.println(Arrays.asList(MyGenericClass.class.getMethod("getValue")));
    }

    public List<String> getList() {
        return Collections.emptyList();
    }

    public static void traceTypeParameters(Class cls){
        Type tp = cls.getGenericSuperclass();
        //Class<?> persistentClass = (Class<?>)((ParameterizedType)tp).getActualTypeArguments()[0];
        //System.err.println("persistentClass: " + persistentClass);
        System.err.println(tp);
    }
}

