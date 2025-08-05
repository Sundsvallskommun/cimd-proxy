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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class TextMessageUserDataFactoryTest {

	@Test
	void createsGsm7BitEncodedUserData() throws Exception {
		final UserData[] uds = TextMessageUserDataFactory.newInstance("abc123 {curly}[square] brackets \u00FC");
		assertThat(uds)
			.isNotNull()
			.hasSize(1);

		final UserData ud = uds[0];
		assertThat(ud).isNotNull();
		assertThat(ud.getDataCodingScheme()).isEqualTo((byte) 0x00);
		assertThat(ud.getHeader()).isNull();
		assertThat(ud.isBodyBinary()).isTrue();

		final var bytes = new byte[] {
			(byte) 0x61, (byte) 0xf1, (byte) 0x38, (byte) 0x26, (byte) 0x9b, (byte) 0x81,
			(byte) 0x36, (byte) 0xa8, (byte) 0x71, (byte) 0x5d, (byte) 0xce, (byte) 0xce,
			(byte) 0x6f, (byte) 0x52, (byte) 0x1b, (byte) 0xde, (byte) 0x3c, (byte) 0x5e,
			(byte) 0x0f, (byte) 0xcb, (byte) 0xcb, (byte) 0x1b, (byte) 0x1f, (byte) 0x48,
			(byte) 0x2c, (byte) 0x0f, (byte) 0x8f, (byte) 0xd7, (byte) 0x65, (byte) 0xfa,
			(byte) 0x1c, (byte) 0xe4, (byte) 0x07
		};

		assertThat(ud.getBinaryBody()).containsExactly(bytes);
	}

	@Test
	void createsUtf16EncodedUserDataWhenTextMessageContainsNonGsmCharacters() {
		final UserData[] uds = TextMessageUserDataFactory.newInstance("\u4F60\u597D");
		assertThat(uds)
			.isNotNull()
			.hasSize(1);

		final UserData ud = uds[0];
		assertThat(ud).isNotNull();
		assertThat(ud.getDataCodingScheme()).isEqualTo((byte) 0x08);
		assertThat(ud.getHeader()).isNull();
		assertThat(ud.isBodyBinary()).isTrue();

		assertThat(ud.getBinaryBody()).isEqualTo(new byte[] {
			0x4F, 0x60, 0x59, 0x7D
		});
	}

	@Test
	void createsConcatenatedMessageUsingUserDataHeader() {
		final UserData[] uds = TextMessageUserDataFactory.newInstance("first part" + ", 2nd part", 15);
		assertThat(uds)
			.isNotNull()
			.hasSize(2);

		assertThat(uds[0].getHeader()).isNotNull();
		byte[] udh = uds[0].getHeader();
		assertThat(udh).hasSize(6);
		assertThat(udh[0]).isEqualTo((byte) 0x05);
		assertThat(udh[1]).isEqualTo((byte) 0x00);
		assertThat(udh[2]).isEqualTo((byte) 0x03);
		// assertThat(udh[3]).isEqualTo((byte) 0x00); // reference number
		assertThat(udh[4]).isEqualTo((byte) 0x02); // total number of parts
		assertThat(udh[5]).isEqualTo((byte) 0x01); // part's number in the sequence

		// TODO: Assert body contents
		assertThat(uds[0].isBodyBinary()).isTrue();
		assertThat(uds[0].getBinaryBody()).hasSizeLessThanOrEqualTo(9);
		// assertThat(uds[0].getBinaryBody()).isEqualTo(new byte[] { });

		assertThat(uds[1].getHeader()).isNotNull();
		udh = uds[1].getHeader();
		assertThat(udh).hasSize(6);
		assertThat(udh[0]).isEqualTo((byte) 0x05);
		assertThat(udh[1]).isEqualTo((byte) 0x00);
		assertThat(udh[2]).isEqualTo((byte) 0x03);
		// assertThat(udh[3]).isEqualTo((byte) 0x00); // reference number
		assertThat(udh[4]).isEqualTo((byte) 0x02); // total number of parts
		assertThat(udh[5]).isEqualTo((byte) 0x02); // part's number in the sequence
		// TODO: Assert body contents
		assertThat(uds[1].isBodyBinary()).isTrue();
		assertThat(uds[1].getBinaryBody()).hasSizeLessThanOrEqualTo(9);
		// assertThat(uds[1].getBinaryBody()).isEqualTo(new byte[] { });
	}

	@Test
	void createsConcatenatedUnicodeMessage() {
		final String textMessage = "\u5B6B\u5B50\u5175\u6CD5 \u8A08\u7BC7\u7B2C\u4E00 \u5B6B\u5B50\u66F0\uFF1A\u5175\u8005\uFF0C\u570B\u4E4B\u5927\u4E8B\uFF0C\u6B7B\u751F\u4E4B\u5730\uFF0C\u5B58\u4EA1\u4E4B\u9053\uFF0C\u4E0D\u53EF\u4E0D\u5BDF\u4E5F\u3002\u6545\u7D93\u4E4B\u4EE5\u4E94\uFF0C\u6821\u4E4B\u4EE5\u8A08\uFF0C\u800C\u7D22\u5176\u60C5\uFF1A\u4E00\u66F0\u9053\uFF0C\u4E8C\u66F0\u5929\uFF0C\u4E09\u66F0\u5730\uFF0C\u56DB\u66F0 \u5C07\uFF0C\u4E94\u66F0\u6CD5\u3002\u9053\u8005\uFF0C\u4EE4\u6C11\u4E8E\u4E0A\u540C\u610F\u8005\u4E5F\uFF0C\u53EF\u8207\u4E4B\u6B7B\uFF0C\u53EF\u8207\u4E4B\u751F\uFF0C\u6C11\u4E0D \u8A6D\u4E5F\u3002\u5929\u8005\uFF0C\u9670\u967D\u3001\u5BD2\u6691\u3001\u6642\u5236\u4E5F\u3002\u5730\u8005\uFF0C\u9AD8\u4E0B\u3001\u9060\u8FD1\u3001\u96AA\u6613\u3001\u5EE3\u72F9 \u3001\u6B7B\u751F\u4E5F\u3002\u5C07\u8005\uFF0C\u667A\u3001\u4FE1\u3001\u4EC1\u3001\u52C7\u3001\u56B4\u4E5F\u3002\u6CD5\u8005\uFF0C\u66F2\u5236\u3001\u5B98\u9053\u3001\u4E3B\u7528 \u4E5F\u3002\u51E1\u6B64\u4E94\u8005\uFF0C\u5C07\u83AB\u4E0D\u805E\uFF0C\u77E5\u4E4B\u8005\u52DD\uFF0C\u4E0D\u77E5\u4E4B\u8005\u4E0D\u52DD\u3002\u6545\u6821\u4E4B\u4EE5\u8A08\uFF0C \u800C\u7D22\u5176\u60C5\u3002\u66F0\uFF1A\u4E3B\u5B70\u6709\u9053\uFF1F\u5C07\u5B70\u6709\u80FD\uFF1F\u5929\u5730\u5B70\u5F97\uFF1F\u6CD5\u4EE4\u5B70\u884C\uFF1F\u5175\u773E\u5B70 \u5F37\uFF1F\u58EB\u5352\u5B70\u7DF4\uFF1F\u8CDE\u7F70\u5B70\u660E\uFF1F\u543E\u4EE5\u6B64\u77E5\u52DD\u8CA0\u77E3\u3002";
		final UserData[] uds = TextMessageUserDataFactory.newInstance(textMessage, 140);

		assertThat(uds).hasSize(4);
	}

	@Test
	void countUtf16Bytes() throws Exception {
		try (BufferedReader r = new BufferedReader(new InputStreamReader(
			Objects.requireNonNull(getClass().getResourceAsStream("utf8-samples.txt")), "UTF-8"))) {
			String line;
			final StringBuilder sb = new StringBuilder();
			while ((line = r.readLine()) != null) {
				sb.append(line);
			}
			System.err.println(sb.toString().length());
			final String textMessage = sb.toString();
			System.err.println(textMessage.length());
			System.err.println(Character.codePointCount(textMessage, 0, textMessage.length()) * 2);
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			try (Writer w = new OutputStreamWriter(out, "UTF-16BE")) {
				w.write(textMessage);
			}
			System.err.println(out.toByteArray().length);
		}
	}
}
