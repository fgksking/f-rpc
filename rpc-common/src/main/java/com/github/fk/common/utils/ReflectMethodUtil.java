package com.github.fk.common.utils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectMethodUtil {

    private static final List<String> IGNORE_METHOD_LIST;

    static {
        Set<String> methodNameSet = new HashSet<>(64);
        for(Method method : Object.class.getMethods()) {
            methodNameSet.add(method.getName());
        }
        for(Method method : Class.class.getMethods()) {
            methodNameSet.add(method.getName());
        }
        IGNORE_METHOD_LIST = new ArrayList<>(methodNameSet);
    }
    public static boolean isIgnoreMethod(final String methodName) {
        return getIgnoreMethodList().contains(methodName);
    }
    public static List<String> getIgnoreMethodList() {
        return IGNORE_METHOD_LIST;
    }
    public static List<String> getParamTypeNames(Method method){
        Class<?>[] parameterTypes = method.getParameterTypes();
        List<String> collected = Arrays.stream(parameterTypes).map(parameterType -> parameterType.getName()).collect(Collectors.toList());
        return  collected;
    }


}
