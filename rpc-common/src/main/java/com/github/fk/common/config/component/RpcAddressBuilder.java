package com.github.fk.common.config.component;

import com.github.fk.common.utils.ArgUtil;

import java.util.ArrayList;
import java.util.List;

public class RpcAddressBuilder {
    private RpcAddressBuilder(){}

    /**
     * 将地址信息转换对对象信息 例127.0.0.1:9527,127.0.0.1:9988
     * @param addresses 地址信息
     * @return 转换后的信息列表
     */
    public static List<RpcAddress> of(final String addresses) {
        ArgUtil.notEmpty(addresses, "addresses");

        String[] addressArray = addresses.split(",");
        ArgUtil.notEmpty(addressArray, "addresses");

        List<RpcAddress> rpcAddressList = new ArrayList<>();
        for(String address : addressArray) {
            String[] addressSplits = address.split(":");
            if (addressSplits.length < 2) {
                throw new IllegalArgumentException("Address must be has ip and port, like 127.0.0.1:9527");
            }
            System.out.println(address);
            String ip = addressSplits[0];
            System.out.println("ip = " +ip);
            System.out.println("port = "+ addressSplits[1]);
            int port = Integer.parseInt(addressSplits[1]);
            // 包含权重信息
            int weight = 1;
            if (addressSplits.length >= 3) {
                weight = Integer.getInteger(addressSplits[2], 1);
            }

            RpcAddress rpcAddress = new RpcAddress(ip, port, weight);
            rpcAddressList.add(rpcAddress);
        }
        return rpcAddressList;
    }
}
