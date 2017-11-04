package my.test.generics;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ReflectionHelper {
    public static Type[] getParameterizedTypes(Object object) {
        Type superclassType = object.getClass().getGenericSuperclass();
        if (!ParameterizedType.class.isAssignableFrom(superclassType.getClass())) {
            return null;
        }
        return ((ParameterizedType)superclassType).getActualTypeArguments();
    }

    public static void main(String[] args) {
        typeTest2();
    }

    private static void typeTest2() {
        TypeVariable<Class<MyDateContainer>>[] typeParams = MyDateContainer.class.getTypeParameters();
        for (TypeVariable<Class<MyDateContainer>> typeParam : typeParams) {
            System.err.println(Arrays.asList(typeParam.getBounds()));
        }
    }

    private static void typeTest1() {
        Type[] types = ReflectionHelper.getParameterizedTypes(new ArrayList<String>());
        for (Type t : types) {
            if (t instanceof TypeVariable) {
                TypeVariable tVar = (TypeVariable)t;
                System.err.println(tVar);
            } else {
                System.err.println("val: " + t + "class: " + t.getClass());
            }
        }
    }
}

class MyDateContainer<T extends Date & Serializable>{
    public T getDate() {
        return null;
    }
}
