package com.youzi.blue.net.client.work;

import com.youzi.blue.net.client.handlers.ClientChannelHandler;
import com.youzi.blue.net.client.handlers.IdleCheckHandler;
import com.youzi.blue.net.common.protocol.Message;
import com.youzi.blue.net.common.protocol.MessageDecoder;
import com.youzi.blue.net.common.protocol.MessageEncoder;
import com.youzi.blue.net.common.utils.LoggerFactory;

import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;

/**
 * 客户端启动器
 */
public class Net {
    private static final LoggerFactory log = LoggerFactory.getLogger();
    private final Bootstrap bootstrap;
    InetSocketAddress inetAddress;

    String username;

    public Net(@NotNull String username) {
        this.username = username;
        bootstrap = new Bootstrap();
        inetAddress = new InetSocketAddress("61.243.3.19", 5672);
    }

    public Channel start() {
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        //ssl加密
                        SSLContext sslContext = SslContextCreator.getSSLContext();
                        SSLEngine sslEngine = sslContext.createSSLEngine();
                        sslEngine.setUseClientMode(true);
                        ch.pipeline().addLast(new SslHandler(sslEngine));

                        ch.pipeline().addLast(new MessageDecoder());
                        ch.pipeline().addLast(new MessageEncoder());
                        ch.pipeline().addLast(new IdleCheckHandler());
                        ch.pipeline().addLast(new ClientChannelHandler());

                    }
                });
        //连接服务器
        ChannelFuture await;
        try {
            await = bootstrap.connect(inetAddress).await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 添加连接状态监听
        await.addListener(new ConnectListener(this));
        if (await.isSuccess()) {
            //认证
            Message message = new Message();
            message.setType(Message.TYPE.LINK);
            message.setData(username.getBytes(StandardCharsets.UTF_8));
            await.channel().writeAndFlush(message);

            return await.channel();
        } else {
            log.info("连接失败");
            try {
                await.channel().close().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }
}
