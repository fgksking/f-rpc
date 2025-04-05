package com.github.fk.client.handler;

import com.github.fk.client.support.register.ClientRegisterManager;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.register.domain.entry.ServiceEntry;
import com.github.fk.register.domain.message.NotifyMessage;
import com.github.fk.register.domain.message.body.RegisterCenterAddNotifyBody;
import com.github.fk.register.domain.message.body.RegisterCenterRemoveNotifyBody;
import com.github.fk.register.domain.message.impl.NotifyMessages;
import com.github.fk.register.simple.constant.MessageTypeConst;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClientRegisterHandler extends SimpleChannelInboundHandler {
    /**
     * 注册服务
     */
    private final InvokeManager invokeManager;

    /**
     * 客户端注册中心管理类
     */
    private final ClientRegisterManager clientRegisterManager;

    public RpcClientRegisterHandler(InvokeManager invokeManager,
                                    ClientRegisterManager clientRegisterManager) {
        this.invokeManager = invokeManager;
        this.clientRegisterManager = clientRegisterManager;
    }

    //接收注册中心的信息
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        NotifyMessage notifyMessage = (NotifyMessage) msg;
        Object body = notifyMessage.body();
        String type = NotifyMessages.type(notifyMessage);
        String seqId = notifyMessage.seqId();
        log.info("[Register Client] received message type: {}, seqId: {} ", type,
                seqId);
        final Channel channel = ctx.channel();
        switch (type) {
            case MessageTypeConst.CLIENT_LOOK_UP_SERVER_RESP:
                RpcResponse rpcResponse = (RpcResponse) body;
                log.info("[Register Client] Register response is :{}", rpcResponse);
                invokeManager.addResponse(rpcResponse.seqId(), rpcResponse);
                break;

            case MessageTypeConst.SERVER_REGISTER_NOTIFY_CLIENT_REQ:
                ServiceEntry serviceEntry = (ServiceEntry) body;
                clientRegisterManager.serverRegisterNotify(serviceEntry);
                break;

            case MessageTypeConst.SERVER_UNREGISTER_NOTIFY_CLIENT_REQ:
                ServiceEntry serviceEntry2 = (ServiceEntry) body;
                clientRegisterManager.serverUnRegisterNotify(serviceEntry2);
                break;

            case MessageTypeConst.REGISTER_CENTER_ADD_NOTIFY:
                RegisterCenterAddNotifyBody addNotifyBody = (RegisterCenterAddNotifyBody) body;
                clientRegisterManager.addRegisterChannel(addNotifyBody, channel);
                break;

            case MessageTypeConst.REGISTER_CENTER_REMOVE_NOTIFY:
                RegisterCenterRemoveNotifyBody removeNotifyBody = (RegisterCenterRemoveNotifyBody) body;
                clientRegisterManager.removeRegisterChannel(removeNotifyBody);
                break;

            default:
                log.warn("[Register Client] not support type: {} and seqId: {}",
                        type, seqId);
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //写入数据
        ctx.flush();
        //关闭通道？
        ctx.close();
    }
}
