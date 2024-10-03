package se.sundsvall.cimdproxy.cimd;

import static java.util.Objects.requireNonNull;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;

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
			var keyStoreProperties = properties.ssl().keyStore();

			var keyStoreType = keyStoreProperties.type();
			var keyStoreAlias = keyStoreProperties.alias();
			var keyStorePassword = keyStoreProperties.password();
			var keyStoreData = Base64.getDecoder().decode(keyStoreProperties.data());
LOG.info("KeyStore: '{}'", keyStoreProperties.data());
			var privateKey = requireNonNull(SslUtil.getPrivateKey(keyStoreType, keyStoreAlias, keyStoreData, keyStorePassword), "Unable to obtain private key");
			var cert = requireNonNull((X509Certificate) SslUtil.getCertificate(keyStoreType, keyStoreAlias, keyStoreData, keyStorePassword), "Unable to obtain certificate");

			var sslContextBuilder = SslContextBuilder.forServer(privateKey, keyStorePassword, cert);
			if (properties.ssl().trustAll()) {
				sslContextBuilder.trustManager(new NoOpTrustManager());
			} else {
				sslContextBuilder.trustManager(cert);
			}
			sslContext = sslContextBuilder.build();
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

	private static class NoOpTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(final X509Certificate[] chain, final String authType) { }

		@Override
		public void checkServerTrusted(final X509Certificate[] chain, final String authType) { }

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}
}
