package com.github.fk.client.proxy.impl;

import cn.hutool.core.util.IdUtil;
import com.github.fk.client.proxy.RemoteInvokeContext;
import com.github.fk.client.proxy.RemoteInvokeService;
import com.github.fk.client.proxy.ServiceContext;
import com.github.fk.client.support.calltype.CallTypeStrategy;
import com.github.fk.client.support.calltype.impl.CallTypeStrategyFactory;
import com.github.fk.client.support.fail.FailStrategy;
import com.github.fk.client.support.fail.impl.FailStrategyFactory;
import com.github.fk.client.support.filter.RpcFilter;
import com.github.fk.client.support.register.ClientRegisterManager;
import com.github.fk.common.rpc.domain.RpcChannelFuture;
import com.github.fk.common.rpc.domain.RpcRequest;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.support.loadBalance.ILoadBalance;
import com.github.fk.common.support.loadBalance.IServer;
import com.github.fk.common.support.loadBalance.impl.LoadBalanceContext;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class RemoteInvokeServiceImpl implements RemoteInvokeService {

    /**
     * 远程调用
     * @param context 远程调用上下文
     * @return
     */
    @Override
    public Object remoteInvoke(RemoteInvokeContext context) {
        final RpcRequest rpcRequest = context.request();
        final ServiceContext proxyContext = context.serviceProxyContext();
        final RpcFilter rpcFilter = proxyContext.rpcFilter();


        // 设置唯一标识
        final String seqId = IdUtil.randomUUID();
        rpcRequest.seqId(seqId);

        // 构建 filter 相关信息,结合 pipeline 进行整合
        rpcFilter.filter(context);

        // 负载均衡
        // 这里使用 load-balance 进行选择 channel 写入。
        final Channel channel = getLoadBalanceChannel(proxyContext);
        log.info("[Client] start call channel id: {}", channel.id().asLongText());

        // 对于信息的写入，实际上有着严格的要求。
        // writeAndFlush 实际是一个异步的操作，直接使用 sync() 可以看到异常信息。
        // 支持的必须是 ByteBuf
        channel.writeAndFlush(rpcRequest).syncUninterruptibly();
        log.info("[Client] start call remote with request: {}", rpcRequest);
        final InvokeManager invokeManager = proxyContext.invokeManager();
        invokeManager.addRequest(seqId, proxyContext.timeout());

        // 获取结果
        CallTypeStrategy callTypeStrategy = CallTypeStrategyFactory.callTypeStrategy(proxyContext.callType());
        RpcResponse rpcResponse = callTypeStrategy.result(proxyContext, rpcRequest);
        invokeManager.removeReqAndResp(seqId);

        // 获取调用结果
        context.rpcResponse(rpcResponse);
        FailStrategy failStrategy = FailStrategyFactory.failStrategy(proxyContext.failType());
        return failStrategy.fail(context);
    }

    private Channel getLoadBalanceChannel(ServiceContext serviceContext) {
        final ClientRegisterManager clientRegisterManager = serviceContext.clientRegisterManager();
        final String serviceId = serviceContext.serviceId();
        List<RpcChannelFuture> channelFutures = clientRegisterManager.queryServerChannelFutures(serviceId);
        // 刷选channel
        List<IServer> servers = BalacncetoList(channelFutures);
        final ILoadBalance loadBalance = serviceContext.loadBalance();
        LoadBalanceContext context = LoadBalanceContext.newInstance()
                .servers(servers);
        IServer server = loadBalance.select(context);
        log.info("负载均衡获取地址信息：{}", server.url());
        RpcChannelFuture future = (RpcChannelFuture) server;
        log.info("channel  = {}", future);
        return future.channelFuture().channel();
    }

    private List<IServer> BalacncetoList(List<RpcChannelFuture> channelFutures) {
        Iterator<RpcChannelFuture> iterator = channelFutures.iterator();
        List<IServer> servers = new ArrayList<>();
        while(iterator.hasNext()){
            IServer next = iterator.next();
            servers.add(next);
        }
        return servers;
    }


}
