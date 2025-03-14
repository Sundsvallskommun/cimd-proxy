package se.sundsvall.cimdproxy.cimd;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.Parameter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.cimdproxy.cimd.util.SessionUtil;

/**
 * CIMD protocol adapter.
 */
@ChannelHandler.Sharable
public class CIMDAdapter extends ChannelInboundHandlerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(CIMDAdapter.class);

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

	private final CIMDMessageListener listener;

	private Session session;

	public CIMDAdapter(final CIMDMessageListener listener) {
		this.listener = listener;
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
		final var request = (Packet) msg;
		LOG.debug("Session [{}] - received packet: {}", session, request);

		Packet response;

		switch (request.getOperationCode()) {
			case Packet.OP_LOGIN -> {
				response = new Packet(request.getOperationCode() + 50, request.getSequenceNumber());
				for (final Parameter parameter : request.getParameters()) {
					if (parameter.getNumber() == Parameter.USER_IDENTITY) {
						LOG.info("Session [{}] - user: {}", session, StringUtils.isNotBlank(parameter.getValue()) ? parameter.getValue() : "<empty>");
						SessionUtil.setUserId(session, parameter.getValue());
						break;
					}
				}
			}
			case Packet.OP_LOGOUT -> {
				response = new Packet(request.getOperationCode() + 50, request.getSequenceNumber());
				session.send(response);
				session.close();
				return;
			}
			case Packet.OP_SUBMIT_MESSAGE -> {
				// Message from application to SMSC
				final var cimdPacket = new CIMDPacket(request);
				if (cimdPacket.getDestinationAddress() == null) {
					LOG.error("Session [{}] - missing destination parameter: {}", session, request);
					return;
				}
				LOG.info("Session [{}] - received message with destination address {}: {}",
					session, cimdPacket.getDestinationAddress(), cimdPacket.getUserData());
				if (listener.handleMessage(new CIMDMessage(cimdPacket.getDestinationAddress(), cimdPacket.getUserData()))) {
					response = new Packet(request.getOperationCode() + 50,
						request.getSequenceNumber(),
						new Parameter(Parameter.DESTINATION_ADDRESS, cimdPacket.getDestinationAddress()),
						new Parameter(Parameter.MC_TIMESTAMP, DATE_TIME_FORMATTER.format(LocalDateTime.now())));
				} else {
					response = new Packet(request.getOperationCode() + 50,
						request.getSequenceNumber(),
						new Parameter(Parameter.ERROR_CODE, "9"),
						new Parameter(Parameter.ERROR_TEXT, "Requested operation failed"));
				}
			}
			case Packet.OP_ALIVE -> response = new Packet(request.getOperationCode() + 50, request.getSequenceNumber());
			case CIMDConstants.OP_DELIVER_MESSAGE_RSP -> {
				// positive response to "deliver message" - msg from SMSC to app
				return;
			}
			case Packet.OP_NACK -> {
				LOG.error("Session [{}] - operation rejected by application", session);
				return;
			}
			default -> {
				response = new Packet(Packet.OP_GENERAL_ERROR_RESPONSE);
				LOG.error("Session [{}] - no handler for CIMD operation: {}", session, request);
			}
		}

		// Send response packet
		session.send(response);
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
		LOG.warn("Exception caught", cause);
	}

	@Override
	public void handlerAdded(final ChannelHandlerContext ctx) {
		session = SessionUtil.getSession(ctx.channel());

		// Send back something, anything, since MG/OneGate expects something from the proxy to
		// actually start the real CIMD communication...
		session.send("HOLA");
	}

	@Override
	public void handlerRemoved(final ChannelHandlerContext ctx) {
		try {
			session.close().sync();

			LOG.info("Session [{}] closed", session);

		} catch (final InterruptedException e1) {
			Thread.currentThread().interrupt();
		} catch (final Exception e2) {
			LOG.warn(String.format("Session [%s] - Unable to close session", session), e2);
		} finally {
			SessionUtil.removeSession(session);
		}
	}
}
