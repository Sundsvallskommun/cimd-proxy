package se.sundsvall.cimdproxy.cimd;

import com.googlecode.jcimd.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import se.sundsvall.cimdproxy.cimd.util.SessionUtil;

import static com.googlecode.jcimd.PacketSerializer.serializePacket;
import static se.sundsvall.cimdproxy.cimd.util.SessionUtil.getPacketSequenceNumberGenerator;

/**
 * CIMD packet encoder.
 */
public class CIMDPacketEncoder extends MessageToByteEncoder<Object> {

	private final boolean useChecksum;

	private Session session;

	public CIMDPacketEncoder(final boolean useChecksum) {
		this.useChecksum = useChecksum;
	}

	@Override
	protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out) throws Exception {

		try (var os = new ByteBufOutputStream(out)) {

			if (msg instanceof final Packet packet) {
				serializePacket(packet, getPacketSequenceNumberGenerator(session), useChecksum, os);
			} else {
				os.write(msg.toString().getBytes());
			}

			os.flush();
		}
	}

	@Override
	public void handlerAdded(final ChannelHandlerContext ctx) {
		session = SessionUtil.getSession(ctx.channel());
	}
}
