package se.sundsvall.cimdproxy.cimd;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import se.sundsvall.cimdproxy.cimd.util.SslUtil;
import se.sundsvall.cimdproxy.configuration.CIMDProperties;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * CIMD server.
 */
@Component
public class CIMD implements DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(CIMD.class);

    private final int port;
    private final boolean sslEnabled;
    private final boolean useCimdChecksum;
    private SslContext sslContext;

    private final NioEventLoopGroup parentEventLoopGroup = new NioEventLoopGroup();
    private final NioEventLoopGroup clientEventLoopGroup = new NioEventLoopGroup();

    private ChannelFuture channelFuture;

    CIMD(final CIMDProperties properties) throws Exception {
        port = properties.port();
        sslEnabled = properties.ssl().enabled();
        useCimdChecksum = properties.useCimdChecksum();

        if (sslEnabled) {
            var keyStoreAlias = properties.ssl().keystore().alias();
            var keyStorePassword = properties.ssl().keystore().password();
            var keyStoreData = Base64.getDecoder().decode(properties.ssl().keystore().data());

            var privateKey = SslUtil.getPrivateKey(keyStoreAlias, keyStoreData, keyStorePassword);
            var cert = (X509Certificate) SslUtil.getCertificate(keyStoreAlias, keyStoreData, keyStorePassword);

            sslContext = SslContextBuilder.forServer(privateKey, properties.ssl().keystore().password(), cert).build();
        }
    }

    public void start(final CIMDMessageListener listener) {
        try {
            channelFuture = new ServerBootstrap()
                .group(parentEventLoopGroup, clientEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(final SocketChannel socketChannel) {
                        var pipeline = socketChannel.pipeline();

                        if (sslEnabled) {
                            pipeline.addLast(sslContext.newHandler(socketChannel.alloc()));
                        }

                        pipeline
                            .addLast(new CIMDPacketDecoder(useCimdChecksum))
                            .addLast(new CIMDPacketEncoder(useCimdChecksum))
                            .addLast(new CIMDAdapter(listener));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(port)
                .sync();

            LOG.info("CIMD listening on port {}{}", port, sslEnabled ? " (using SSL)" : "");
        } catch (Exception e) {
            LOG.error("Unable to start CIMD", e);

            System.exit(-1);
        }
    }

    @Override
    public void destroy() throws Exception {
        LOG.info("CIMD shutting down");

        try {
            channelFuture
                .channel()
                .closeFuture()
                .await(5, TimeUnit.SECONDS);
        } finally {
            clientEventLoopGroup.shutdownGracefully();
            parentEventLoopGroup.shutdownGracefully();

            LOG.info("CIMD shut down");
        }
    }
}
