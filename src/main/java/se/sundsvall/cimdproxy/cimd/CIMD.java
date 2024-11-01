package se.sundsvall.cimdproxy.cimd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import io.netty.handler.ssl.ClientAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

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
	private final SslContext sslContext;

	private final NioEventLoopGroup parentEventLoopGroup = new NioEventLoopGroup();
	private final NioEventLoopGroup clientEventLoopGroup = new NioEventLoopGroup();

	private ChannelFuture channelFuture;

	CIMD(final CIMDProperties properties) throws SSLException {
		port = properties.port();
		sslEnabled = properties.ssl().enabled();
		useCimdChecksum = properties.useCimdChecksum();

		if (sslEnabled) {
			// Note: serverCert should contain full chain
			InputStream certInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(properties.ssl().serverCert()));
			InputStream keyInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(properties.ssl().serverKey()));

			var sslContextBuilder = SslContextBuilder.forServer(certInputStream, keyInputStream, properties.ssl().serverKeyPassword());

			if (properties.ssl().trustedCert() != null) {
				// Assume Client certificate authentication (two-way SSL) when trustedCert is set
				// Verify with cmd "openssl s_client -cert <cert-file> -key <key-file> -showcerts -connect <address>"
				// For self-signed certs add flag -CAfile <cert-file>
				LOG.info("Only accepting trusted clients present in truststore (two-way SSL)");
				var truststoreInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(properties.ssl().trustedCert()));
				sslContextBuilder
					.clientAuth(ClientAuth.REQUIRE)
					.trustManager(truststoreInputStream);
			} else {
				// Verify with cmd "openssl s_client -showcerts -connect <address>"
				// For self-signed certs add flag -CAfile <cert-file>
				LOG.info("Accepting all clients (one-way SSL)");
				sslContextBuilder
					.clientAuth(ClientAuth.NONE);
			}

			sslContext = sslContextBuilder.build();
		} else {
			sslContext = null;
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
}
