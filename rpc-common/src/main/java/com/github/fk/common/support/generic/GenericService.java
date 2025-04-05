package com.github.fk.common.support.generic;

public interface GenericService {
    /**
     * Generic invocation
     *
     * @param method          Method name, e.g. findPerson. If there are overridden methods, parameter info is
     *                       required, e.g. findPerson(java.lang.String)
     * @param parameterTypes Parameter types
     * @param args           Arguments
     * @return invocation return value
     */
    Object $invoke(String method, String[] parameterTypes, Object[] args) ;

}
