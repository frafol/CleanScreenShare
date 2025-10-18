package it.frafol.cleanss.velocity.objects.adapter;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;

public class ReflectUtil {
    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static MethodHandles.Lookup getSuperLookup() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafeField = getField(unsafeClass, unsafeClass, true);
        Method theUnsafeGetObjectMethod = getMethod(unsafeClass, "getObject", false, Object.class, long.class);
        Method theUnsafeStaticFieldOffsetMethod = getMethod(unsafeClass, "staticFieldOffset", false, Field.class);
        Object theUnsafe = theUnsafeField.get(null);
        Field implLookup = getField(MethodHandles.Lookup.class, "IMPL_LOOKUP", false);

        return (MethodHandles.Lookup) theUnsafeGetObjectMethod.invoke(theUnsafe, MethodHandles.Lookup.class, theUnsafeStaticFieldOffsetMethod.invoke(theUnsafe, implLookup));
    }

    public static void addFileLibrary(File file) throws Throwable {
        ClassLoader classLoader = ReflectUtil.class.getClassLoader();
        MethodHandle handle = ReflectUtil.getSuperLookup().unreflect(ReflectUtil.getMethodWithParent(classLoader.getClass(), "addURL", false, URL.class));
        handle.invoke(classLoader, file.toURI().toURL());
    }

    public static Field getField(Class<?> clazz, String target, boolean handleAccessible) throws NoSuchFieldException {
        try {
            Field field;
            field = clazz.getDeclaredField(target);
            if (handleAccessible) field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldException(target + " field in " + clazz.getName());
        }
    }

    public static Field getField(Class<?> clazz, Class<?> target, boolean handleAccessible) throws NoSuchFieldException {
        return getField0(clazz, clazz, target, handleAccessible);
    }

    private static Field getField0(Class<?> source, Class<?> clazz, Class<?> target, boolean handleAccessible) throws NoSuchFieldException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() != target) continue;
            if (handleAccessible) field.setAccessible(true);
            return field;
        }
        clazz = clazz.getSuperclass();
        if (clazz != null) return getField(clazz, target, handleAccessible);
        throw new NoSuchFieldException(target.getName() + " type in " + source.getName());
    }

    public static Method getMethod(Class<?> clazz, String name, boolean handleAccessible, Class<?>... args) throws NoSuchMethodException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equalsIgnoreCase(name)) continue;
            if (!Arrays.equals(method.getParameterTypes(), args)) continue;
            if (handleAccessible) method.setAccessible(true);
            return method;
        }
        throw new NoSuchMethodException(name + " method in " + clazz.getName());
    }

    public static Method getMethodWithParent(Class<?> clazz, String name, boolean handleAccessible, Class<?>... args) throws NoSuchMethodException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equalsIgnoreCase(name)) continue;
            if (!Arrays.equals(method.getParameterTypes(), args)) continue;
            if (handleAccessible) method.setAccessible(true);
            return method;
        }
        if (clazz != Object.class)
            return getMethodWithParent(clazz.getSuperclass(), name, handleAccessible, args);
        throw new NoSuchMethodException(name + " method in " + clazz.getName());
    }
}
