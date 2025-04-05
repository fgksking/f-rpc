package com.github.fk.common.utils;

public class ArgUtil {

    public static void notEmpty(String strName , String des){
        if(strName ==null || strName.isEmpty()) {
            throw new IllegalArgumentException(des + " can not be null!");
        }
    }
    public static void notNull(Object object, String name) {
        if (null == object) {
            throw new IllegalArgumentException(name + " can not be null!");
        }
    }
    public static void notEmpty(Object strName , String des){
        if(strName ==null) {
            throw new IllegalArgumentException(des + " can not be null!");
        }
    }

}
