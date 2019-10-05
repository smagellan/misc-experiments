package smagellan.test.lombok;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

public class MyValueInstantiator extends ValueInstantiator {
    private final MethodHandle mh;

    public MyValueInstantiator(Class<?> declaringClass, Class<?> returnClass) throws NoSuchMethodException, IllegalAccessException {
        this("builder", declaringClass, returnClass);
    }

    public MyValueInstantiator(Class<?> declaringClass) throws NoSuchMethodException, IllegalAccessException {
        this(declaringClass, getBuilderClassForPojoClass(declaringClass));
    }

    public MyValueInstantiator(String methodName, Class<?> declaringClass, Class<?> returnClass) throws NoSuchMethodException, IllegalAccessException {
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
        MethodType mt = MethodType.methodType(returnClass);
        this.mh = publicLookup.findStatic(declaringClass, methodName, mt);
    }

    public static Class<?> getBuilderClassForPojoClass(Class<?> declaringClass) {
        String builderClassName = declaringClass.getSimpleName() + "$" + declaringClass.getSimpleName() + "Builder";
        return Arrays.stream(declaringClass.getClasses())
                .filter(el -> el.getName().endsWith(builderClassName))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }


    @Override
    public Object createUsingDefault(DeserializationContext ctxt) {
        try {
            return mh.invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public boolean canCreateUsingDefault() {
        return true;
    }
}
