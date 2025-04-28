package se.sundsvall.cimdproxy.cimd;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.stereotype.Component;
import se.sundsvall.cimdproxy.configuration.CIMDProperties;

/**
 * CIMD server.
 */
@Component
public class CIMD implements DisposableBean {

	private static final Logger LOG = LoggerFactory.getLogger(CIMD.class);

	static final String SSL_BUNDLE_NAME = "cimd";

	private final int port;
	private final boolean useCimdChecksum;
	private final SslContext sslContext;

	private final NioEventLoopGroup parentEventLoopGroup = new NioEventLoopGroup();
	private final NioEventLoopGroup clientEventLoopGroup = new NioEventLoopGroup();

	private ChannelFuture channelFuture;

	CIMD(final CIMDProperties properties, final SslBundles sslBundles) throws SSLException {
		port = properties.port();
		useCimdChecksum = properties.useCimdChecksum();

		var sslBundle = sslBundles.getBundle(SSL_BUNDLE_NAME);
		var sslManagerBundle = sslBundle.getManagers();

		sslContext = SslContextBuilder.forServer(sslManagerBundle.getKeyManagerFactory())
			.clientAuth(ClientAuth.REQUIRE)
			.trustManager(sslManagerBundle.getTrustManagerFactory())
			.build();
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

						if (sslContext != null) {
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

			LOG.info("CIMD listening on port {} using SSL", port);
		} catch (final InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
		} catch (final Exception e) {
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
