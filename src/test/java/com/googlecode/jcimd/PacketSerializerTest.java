/*
 * Copyright 2010-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.jcimd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PacketSerializerTest {

	public static final char STX = 0x02;
	public static final char TAB = '\t'; // ASCII value is 9
	public static final char COLON = ':'; // ASCII value is 58
	public static final char ETX = 0x03;

	private PacketSerializer serializer;

	@BeforeEach
	void setUp() {
		serializer = new PacketSerializer();
		serializer.setSequenceNumberGenerator(
			new ApplicationPacketSequenceNumberGenerator());
	}

	@Test
	void serializeLoginCommand() throws Exception {
		final Packet command = new Packet(1,
			new Parameter(10, "username"),
			new Parameter(11, "password"));
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final byte[] expected = concat(
			("" + STX + "01" + COLON + "001" + TAB).getBytes(),
			("010" + COLON + "username" + TAB).getBytes(),
			("011" + COLON + "password" + TAB).getBytes(),
			("CS"/* two-bytes for checksum */ + ETX).getBytes());
		final int checksum = calculateCheckSum(Arrays.copyOfRange(expected, 0, expected.length - 3));
		// check sum needs to be 0-9 A-F (upper-case)
		final String checkSumHexString = String.format("%02X", checksum);
		expected[expected.length - 3] = (byte) checkSumHexString.charAt(0);
		expected[expected.length - 2] = (byte) checkSumHexString.charAt(1);
		serializer.serialize(command, outputStream);

		assertThat(outputStream.toByteArray()).isEqualTo(expected);
	}

	@Test
	void deserializeLoginCommand() throws Exception {
		final byte[] bytes = concat(
			("" + STX + "01" + COLON + "001" + TAB).getBytes(),
			("010" + COLON + "username" + TAB).getBytes(),
			("011" + COLON + "password" + TAB).getBytes(),
			("CS"/* two-bytes for checksum */ + ETX).getBytes());
		final int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
		final String checkSumHexString = String.format("%02X", checksum);
		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		final Packet command = new Packet(1, 1,
			new Parameter(10, "username"),
			new Parameter(11, "password"));
		final Packet actual = serializer.deserialize(inputStream);

		assertThat(actual).isEqualTo(command);
	}

	@Test
	void deserializeLoginCommandWithoutChkSum() throws Exception {
		final PacketSerializer ser = new PacketSerializer("ser", false);
		final byte[] bytes = concat(
			("" + STX + "01" + COLON + "001" + TAB).getBytes(),
			("010" + COLON + "username" + TAB).getBytes(),
			("011" + COLON + "password" + TAB).getBytes(),
			("" + ETX).getBytes());
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		final Packet command = new Packet(1, 1,
			new Parameter(10, "username"),
			new Parameter(11, "password"));
		final Packet actual = ser.deserialize(inputStream);

		assertThat(actual).isEqualTo(command);
	}

	@Test
	void deserializeLoginCommandWithExtraDataBetweenPackets() throws Exception {
		final byte[] bytes = concat(
			("noise" + STX + "01" + COLON + "001" + TAB).getBytes(),
			("010" + COLON + "username" + TAB).getBytes(),
			("011" + COLON + "password" + TAB).getBytes(),
			("CS"/* two-bytes for checksum */ + ETX + "noise").getBytes());
		final int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 5, bytes.length - 8));
		final String checkSumHexString = String.format("%02X", checksum);
		bytes[bytes.length - 8] = (byte) checkSumHexString.charAt(0);
		bytes[bytes.length - 7] = (byte) checkSumHexString.charAt(1);
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		final Packet command = new Packet(1, 1,
			new Parameter(10, "username"),
			new Parameter(11, "password"));
		final Packet actual = serializer.deserialize(inputStream);

		assertThat(actual).isEqualTo(command);
	}

	@Test
	void deserializeCommandWithMissingColon() {
		final byte[] bytes = concat(
			("" + STX + "01" + COLON + "001" + TAB).getBytes(),
			("010" + /* COLON + */ "username" + TAB).getBytes(),
			("011" + COLON + "password" + TAB).getBytes(),
			("CS"/* two-bytes for checksum */ + ETX).getBytes());
		final int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
		final String checkSumHexString = String.format("%02X", checksum);
		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

		assertThatExceptionOfType(IOException.class).isThrownBy(() -> serializer.deserialize(inputStream));
	}

	@Test
	void deserializeCommandWithFourByteParameterType() {
		final byte[] bytes = concat(
			("" + STX + "01" + COLON + "001" + TAB).getBytes(),
			("0100" + COLON + "username" + TAB).getBytes(),
			("011" + COLON + "password" + TAB).getBytes(),
			("CS"/* two-bytes for checksum */ + ETX).getBytes());
		final int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
		final String checkSumHexString = String.format("%02X", checksum);
		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

		assertThatExceptionOfType(IOException.class).isThrownBy(() -> serializer.deserialize(inputStream));
	}

	@Test
	void deserializeCommandWithNonHexCheckSum() {
		final byte[] bytes = concat(
			("" + STX + "01" + COLON + "001" + TAB).getBytes(),
			("010" + COLON + "username" + TAB).getBytes(),
			("011" + COLON + "password" + TAB).getBytes(),
			("CS"/* two-bytes for checksum */ + ETX).getBytes());
		final int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
		bytes[bytes.length - 3] = (byte) (checksum & (0xFF00 >> 8));
		bytes[bytes.length - 2] = (byte) (checksum & 0x00FF);
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

		assertThatExceptionOfType(IOException.class).isThrownBy(() -> serializer.deserialize(inputStream));
	}

	@Disabled("Escaping characters should be done via UserData implementation")
	@Test
	void deserializeUserDataParameter() throws Exception {
		final byte[] bytes = concat(
			("" + STX + "03:001" + TAB).getBytes("ASCII"),
			("033:_XX( curly braces _XX) _XX< square brackets _XX>" + TAB).getBytes("ASCII"),
			("CS"/* two-bytes for checksum */ + ETX).getBytes("ASCII"));
		final int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
		final String checkSumHexString = Integer.toHexString(checksum).toUpperCase();
		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		final Packet command = new Packet(3, 1, new Parameter(33, "{ curly braces } [ square brackets ]"));
		final Packet actual = serializer.deserialize(inputStream);

		assertThat(actual).isEqualTo(command);
	}

	private static byte[] concat(final byte[]... arrays) {
		if (arrays.length == 0) {
			throw new IllegalArgumentException("No arrays to concat");
		}
		if (arrays.length == 1) {
			return arrays[0];
		}
		int totalLength = 0;
		for (final byte[] array : arrays) {
			totalLength += array.length;
		}
		final byte[] result = (byte[]) java.lang.reflect.Array.newInstance(arrays[0].getClass().getComponentType(), totalLength);
		int offset = 0;
		for (final byte[] a : arrays) {
			System.arraycopy(a, 0, result, offset, a.length);
			offset += a.length;
		}
		return result;
	}

	protected int calculateCheckSum(final byte[] bytes) {
		int sum = 0;
		for (final byte b : bytes) {
			sum += b;
			sum &= 0xFF;
		}
		return sum;
	}
}
