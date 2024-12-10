package se.sundsvall.cimdproxy.cimd.util;

import com.googlecode.jcimd.PacketSequenceNumberGenerator;
import com.googlecode.jcimd.SmsCenterPacketSequenceNumberGenerator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.cimdproxy.cimd.Session;
import se.sundsvall.cimdproxy.cimd.SessionAttribute;

public final class SessionUtil {

	private static final Logger LOG = LoggerFactory.getLogger(SessionUtil.class);

	private static final Map<ChannelId, Session> SESSIONS = new HashMap<>();

	private SessionUtil() {}

	public static synchronized Session getSession(final Channel channel) {
		SESSIONS.computeIfAbsent(channel.id(), channelId -> {
			var session = new Session(channel);
			session.setAttribute(
				SessionAttribute.CIMD_PACKET_SEQUENCE_GENERATOR.toString(),
				new SmsCenterPacketSequenceNumberGenerator());
			LOG.info("Session [{}] opened", channelId);
			return session;
		});

		return SESSIONS.get(channel.id());
	}

	public static synchronized void removeSession(final Session session) {
		SESSIONS.remove(session.getId());
	}

	public static String getUserId(final Session session) {
		return session.getAttribute(SessionAttribute.USER_ID.toString());
	}

	public static void setUserId(final Session session, final String userId) {
		session.setAttribute(SessionAttribute.USER_ID.toString(), userId);
	}

	public static PacketSequenceNumberGenerator getPacketSequenceNumberGenerator(final Session session) {
		return session.getAttribute(SessionAttribute.CIMD_PACKET_SEQUENCE_GENERATOR.toString());
	}
}
