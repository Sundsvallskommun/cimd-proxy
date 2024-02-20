package se.sundsvall.cimdproxy.cimd;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.Parameter;
import com.googlecode.jcimd.charset.GsmCharsetProvider;

public class CIMDPacket {

	private static final Logger LOG = LoggerFactory.getLogger(CIMDPacket.class);

	// Since Java SPI doesn't seem to work, because of reasons...
	private static final Charset CHARSET = new GsmCharsetProvider().charsetForName("GSM");

	private String destinationAddress;
	private String userData; // user data un-encoded

	public CIMDPacket(final Packet p) {
		Integer dataCodingScheme = null;
		String userDataBinary = null;

		for (final Parameter pr : p.getParameters()) {
			switch (pr.getNumber()) {
				case Parameter.DESTINATION_ADDRESS -> destinationAddress = pr.getValue();
				case Parameter.DATA_CODING_SCHEME -> dataCodingScheme = Integer.valueOf(pr.getValue());
				case Parameter.USER_DATA -> userData = pr.getValue();
				case Parameter.USER_DATA_BINARY -> {
					userDataBinary = pr.getValue();
					userData = decodeUserDataBinary(dataCodingScheme, userDataBinary);
				}
			}
		}
	}

	private String decodeUserDataBinary(final Integer dataCodingScheme, final String userDataBinary) {
		if (dataCodingScheme == null) {
			LOG.info("Data coding scheme not specified for user binary data");

			return userDataBinary;
		}
		if (dataCodingScheme == 0) {
			CharBuffer cb = null;
			try {
				final CharsetDecoder decoder = CHARSET.newDecoder();
				final byte[] encHex = Hex.decodeHex(userDataBinary.toCharArray());
				cb = decoder.decode(ByteBuffer.wrap(encHex));
			} catch (DecoderException | CharacterCodingException e) {
				LOG.info("Failed to decode user data: " + userDataBinary, e);
			}
			return new String(cb.array());
		}
		LOG.info("Unsupported data coding scheme: {}", dataCodingScheme);
		return userDataBinary;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public String getUserData() {
		return userData;
	}
}
