package smagellan.test.lombok;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleInstantiator extends ValueInstantiator {
    private final MethodHandle mh;

    private MethodHandleInstantiator(MethodHandle methodHandle) {
        this.mh = methodHandle;
    }

    public static ValueInstantiator instantiatorFor(Class<?> declaringClass, Class<?> returnClass) {
        return instantiatorFor("builder", declaringClass, returnClass);
    }

    public static ValueInstantiator instantiatorFor(Class<?> declaringClass) {
        return instantiatorFor(declaringClass, getBuilderClassForPojoClass(declaringClass));
    }

    public static ValueInstantiator instantiatorFor(String methodName, Class<?> declaringClass, Class<?> returnClass) {
        try {
            MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
            MethodType mt = MethodType.methodType(returnClass);
            MethodHandle mh = publicLookup.findStatic(declaringClass, methodName, mt);
            return new MethodHandleInstantiator(mh);
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    public static Class<?> getBuilderClassForPojoClass(Class<?> declaringClass) {
        try {
            String builderClassName = declaringClass.getName() + "$" + declaringClass.getSimpleName() + "Builder";
            return Class.forName(builderClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
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
