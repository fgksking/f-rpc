package com.github.fk.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ipUtils {

    public static String getLocalIp(){
        InetAddress localHost;
        try {
          localHost= InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String localIp = localHost.getHostAddress();
        return localIp;
    }

     public static void main(String[] args) {
         String localIp = ipUtils.getLocalIp();
         System.out.println(localIp);
     }
}
