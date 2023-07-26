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

    public static ValueInstantiator instantiatorFor(Class<?> entityClass, Class<?> builderClass) {
        return instantiatorFor("builder", entityClass, builderClass);
    }

    public static ValueInstantiator instantiatorFor(Class<?> entityClass) {
        return instantiatorFor(entityClass, getBuilderClassForEntityClass(entityClass));
    }

    public static ValueInstantiator instantiatorFor(String methodName, Class<?> entityClass, Class<?> builderClass) {
        try {
            MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
            MethodType mt = MethodType.methodType(builderClass);
            MethodHandle mh = publicLookup.findStatic(entityClass, methodName, mt);
            return new MethodHandleInstantiator(mh);
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    public static Class<?> getBuilderClassForEntityClass(Class<?> entityClass) {
        try {
            String builderClassName = entityClass.getName() + "$" + entityClass.getSimpleName() + "Builder";
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
