package com.github.fk.register.simple.handler;

import com.github.fk.register.domain.entry.ServiceEntry;
import com.github.fk.register.domain.message.NotifyMessage;
import com.github.fk.register.domain.message.body.ServerHeartbeatBody;
import com.github.fk.register.domain.message.impl.NotifyMessages;
import com.github.fk.register.simple.SimpleRpcRegister;
import com.github.fk.register.simple.client.RegisterClientService;
import com.github.fk.register.simple.client.impl.DefaultRegisterClientService;
import com.github.fk.register.simple.constant.MessageTypeConst;
import com.github.fk.register.simple.server.RegisterServerService;
import com.github.fk.register.simple.server.impl.DefaultRegisterServerService;
import com.github.fk.register.spi.RpcRegister;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class RegisterCenterServerHandler extends SimpleChannelInboundHandler {
    private final RpcRegister rpcRegister;

    public RegisterCenterServerHandler() {
        this.rpcRegister = this.buildSimpleRpcRegister();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String id = ctx.channel().id().asLongText();
        log.info("[Register Server] channel {} connected " + id);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //
        log.info("msg = {}",msg);
        NotifyMessage notifyMessage = (NotifyMessage) msg;
        Object body = notifyMessage.body();
        String type = NotifyMessages.type(notifyMessage);
        String seqId = notifyMessage.seqId();
        log.info("[Register Server] received message type: {}, seqId: {} ", type,
                seqId);
        final Channel channel = ctx.channel();

        switch (type) {
            case MessageTypeConst.SERVER_REGISTER_REQ:
                rpcRegister.register((ServiceEntry)body, channel);
                break;

            case MessageTypeConst.SERVER_UN_REGISTER_REQ:
                rpcRegister.unRegister((ServiceEntry)body);
                break;

            case MessageTypeConst.CLIENT_SUBSCRIBE_REQ:
                rpcRegister.subscribe((ServiceEntry)body, channel);
                break;

            case MessageTypeConst.CLIENT_UN_SUBSCRIBE_REQ:
                rpcRegister.unSubscribe((ServiceEntry)body, channel);
                break;

            case MessageTypeConst.CLIENT_LOOK_UP_SERVER_REQ:
                rpcRegister.lookUp(seqId, (ServiceEntry)body, channel);
                break;

            case MessageTypeConst.SERVER_HEARTBEAT_REQ:
                ServerHeartbeatBody heartbeatBody = (ServerHeartbeatBody) body;
                rpcRegister.serverHeartbeat(heartbeatBody, channel);
                break;

            default:
        }

    }

    /**
     * 构建简单注册实现类
     * @return 注册实现
     */
    private RpcRegister buildSimpleRpcRegister() {
        final RegisterServerService registerServerService = new DefaultRegisterServerService();
        final RegisterClientService registerClientService = new DefaultRegisterClientService();
        return new SimpleRpcRegister(registerServerService, registerClientService);
    }
}
