package com.github.fk.server.handler;

import com.github.fk.common.config.component.RpcAddress;
import com.github.fk.register.domain.message.NotifyMessage;
import com.github.fk.register.domain.message.body.RegisterCenterAddNotifyBody;
import com.github.fk.register.domain.message.body.RegisterCenterRemoveNotifyBody;
import com.github.fk.register.domain.message.body.ServerHeartbeatBody;
import com.github.fk.register.domain.message.impl.NotifyMessages;
import com.github.fk.register.simple.constant.MessageTypeConst;
import com.github.fk.server.support.register.ServerRegisterManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 与注册中心建立长连接
 */
@Slf4j
public class RpcServerRegisterHandler extends SimpleChannelInboundHandler {

    private final ServerRegisterManager serverRegisterManager;

    public RpcServerRegisterHandler(ServerRegisterManager serverRegisterManager) {
        this.serverRegisterManager = serverRegisterManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        NotifyMessage notifyMessage = (NotifyMessage) msg;
        Object body = notifyMessage.body();
        String type = NotifyMessages.type(notifyMessage);
        String seqId = notifyMessage.seqId();
        final Channel channel = ctx.channel();
        switch (type) {
            // 注册中心添加机器
            case MessageTypeConst.REGISTER_CENTER_ADD_NOTIFY:
                RegisterCenterAddNotifyBody addNotifyBody = (RegisterCenterAddNotifyBody) body;
                registerCenterAddNotify(addNotifyBody, channel);
                break;

            // 注册中心移除机器
            case MessageTypeConst.REGISTER_CENTER_REMOVE_NOTIFY:
                RegisterCenterRemoveNotifyBody removeNotifyBody = (RegisterCenterRemoveNotifyBody) body;
                registerCenterRemoveNotify(removeNotifyBody);
                break;
            case MessageTypeConst.SERVER_HEARTBEAT_REQ:
                ServerHeartbeatBody heartbeatBody = (ServerHeartbeatBody) body;

            default:
                log.warn("[Server Register Listener] not support type: {} and seqId: {}",
                        type, seqId);
        }
    }

    private void registerCenterRemoveNotify(RegisterCenterRemoveNotifyBody removeNotifyBody) {
        log.info("注册中心移除机器通知");
        RpcAddress rpcAddress = new RpcAddress(removeNotifyBody.ip(), removeNotifyBody.port());
        serverRegisterManager.removeRegisterChannel(rpcAddress);
    }

    private void registerCenterAddNotify(RegisterCenterAddNotifyBody addNotifyBody, Channel channel) {
        log.info("注册中心添加机器通知");
        RpcAddress rpcAddress = new RpcAddress(addNotifyBody.ip(), addNotifyBody.port());
        serverRegisterManager.addRegisterChannel(rpcAddress, channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[Rpc Server] meet ex", cause);
        ctx.close();
    }
}
