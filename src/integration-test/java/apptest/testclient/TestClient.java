package apptest.testclient;

import static com.googlecode.jcimd.Packet.OP_SUBMIT_MESSAGE;
import static com.googlecode.jcimd.Parameter.DATA_CODING_SCHEME;
import static com.googlecode.jcimd.Parameter.DESTINATION_ADDRESS;
import static com.googlecode.jcimd.Parameter.USER_DATA;
import static com.googlecode.jcimd.Parameter.USER_DATA_BINARY;
import static com.googlecode.jcimd.Parameter.USER_DATA_HEADER;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.Parameter;
import com.googlecode.jcimd.StringUserData;
import com.googlecode.jcimd.UserData;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.cimdproxy.cimd.CIMDPacketDecoder;
import se.sundsvall.cimdproxy.cimd.CIMDPacketEncoder;
import se.sundsvall.cimdproxy.cimd.Session;
import se.sundsvall.cimdproxy.cimd.util.SessionUtil;

public class TestClient {

	private static final Logger LOG = LoggerFactory.getLogger(TestClient.class);

	private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
	private final AtomicBoolean opened = new AtomicBoolean(false);
	private Session session;

	private List<Packet> sentPackets = new LinkedList<>();
	private List<Packet> receivedPackets = new LinkedList<>();

	public TestClient(final String remoteHost, final int remotePort) throws InterruptedException {
		if (opened.compareAndSet(false, true)) {
			var bootstrap = new Bootstrap();

			var channel = bootstrap.group(eventLoopGroup)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(final SocketChannel ch) {
						var pipeline = ch.pipeline();

						pipeline.addLast(new CIMDPacketDecoder(true));
						pipeline.addLast(new CIMDPacketEncoder(true));
						pipeline.addLast(new SimpleChannelInboundHandler<>() {
							@Override
							protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) {
								if (msg instanceof Packet packet) {
									receivedPackets.add(packet);
									if (packet.isPositiveResponse()) {
										LOG.info("Message successfully sent - {} - {}", packet.getOperationCode(), sanitizeForLogging(packet.toString()));
									} else {
										LOG.info("Unable to send message - {} - {}", packet.getOperationCode(), sanitizeForLogging(packet.toString()));
									}
								}
							}
						});
					}
				})
				.connect(remoteHost, remotePort)
				.sync()
				.channel();

			session = SessionUtil.getSession(channel);

			receivedPackets.clear();
		}
	}

	public void send(final String destinationAddress, final String message) {
		send(destinationAddress, new StringUserData(message));
	}

	public void send(final String destinationAddress, final UserData userData) {
		var parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter(DESTINATION_ADDRESS, destinationAddress));
		if (userData != null) {
			addParameterIfNotNull(DATA_CODING_SCHEME, userData.getDataCodingScheme(), parameters);
			addParameterIfNotNull(USER_DATA_HEADER, userData.getHeader(), parameters);
			if (userData.isBodyBinary()) {
				addParameterIfNotNull(USER_DATA_BINARY, userData.getBinaryBody(), parameters);
			} else {
				addParameterIfNotNull(USER_DATA, userData.getBody(), parameters);
			}
		}

		var packet = new Packet(OP_SUBMIT_MESSAGE, parameters.toArray(new Parameter[0]));
		session.send(packet);

		sentPackets.add(packet);
	}

	public void disconnect() {
		if (opened.compareAndSet(true, false)) {
			session.close();
			eventLoopGroup.shutdownGracefully();
		}
	}

	public List<Packet> getSentPackets() {
		return sentPackets;
	}

	public List<Packet> getReceivedPackets() {
		return receivedPackets;
	}

	private void addParameterIfNotNull(final int number, final String value, final List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, value));
		}
	}

	private void addParameterIfNotNull(final int number, final Integer value, final List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, value));
		}
	}

	private void addParameterIfNotNull(final int number, final byte[] value, final List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, value));
		}
	}
}
