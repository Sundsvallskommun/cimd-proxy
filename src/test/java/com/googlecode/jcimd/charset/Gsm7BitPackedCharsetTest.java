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
package com.googlecode.jcimd.charset;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Gsm7BitPackedCharsetTest {

	private Charset charset;
	private CharsetDecoder decoder;
	private CharsetEncoder encoder;

	@BeforeEach
	void setUp() {
		charset = Charset.forName("GSM");
		decoder = charset.newDecoder();
		encoder = charset.newEncoder();
	}

	@Test
	void decodesAlphaNumericCharacters() throws Exception {
		ByteBuffer byteBuffer = ByteBuffer.wrap(
				new byte[] {
						(byte) 0x49, (byte) 0x3A, (byte) 0x28, (byte) 0x3D,
						(byte) 0x07, (byte) 0x95, (byte) 0xC3, (byte) 0xF3,
						(byte) 0x3C, (byte) 0x88, (byte) 0xFE, (byte) 0x06,
						(byte) 0xCD, (byte) 0xCB, (byte) 0x6E, (byte) 0x32,
						(byte) 0x88, (byte) 0x5E, (byte) 0xC6, (byte) 0xD3,
						(byte) 0x41, (byte) 0xED, (byte) 0xF2, (byte) 0x7C,
						(byte) 0x1E, (byte) 0x3E, (byte) 0x97, (byte) 0xE7,
						(byte) 0x2E, (byte) 0x50, (byte) 0x4C, (byte) 0x36,
						(byte) 0x03
				});
		CharBuffer charBuffer = decoder.decode(byteBuffer);

		assertThat(charBuffer.toString()).isEqualTo("It is easy to send text messages. 123");
	}

	@Test
	void decodesHelloWorld() throws Exception {
		// Bytes (in HEX) are taken from
		// http://en.wikipedia.org/wiki/Concatenated_SMS
		ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[] {
			(byte) 0xE8, (byte) 0x32, (byte) 0x9B, (byte) 0xFD,
			(byte) 0x06, (byte) 0xDD, (byte) 0xDF, (byte) 0x72,
			(byte) 0x36, (byte) 0x19
		});
		CharBuffer charBuffer = decoder.decode(byteBuffer);

		assertThat(charBuffer.toString()).isEqualTo("hello world");
	}

	@Test
	void decodesNonAlphaNumericCharacters() throws Exception {
		byte[] bytes = new byte[] { 0x00, 0x01 };
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		CharBuffer charBuffer = decoder.decode(byteBuffer);

		assertThat(charBuffer.toString()).isEqualTo("@$");
	}

	@Test
	void decodesEscapedCharacters() throws Exception {
		byte[] bytes = new byte[] {
			(byte) 0x1B, (byte) 0xD4, (byte) 0x26, (byte) 0xB5,
			(byte) 0xE1, (byte) 0x6D, (byte) 0x7C
		};
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		CharBuffer charBuffer = decoder.decode(byteBuffer);

		assertThat(charBuffer.toString()).isEqualTo("{}[]");
	}

	@Test
	void decodesInvalidEscapeSequence() throws Exception {
		// 0x1B 0x48 0x1B 0x4A
		// (esc) 'H' (esc) 'J'
		// 0001 1011 0100 1000 0001 1011 0100 1010 
		// _001 1011 _100 1000 _001 1011 _100 1010 
		// 0001 1011 1110 0100 0100 0110 ____ 1001
		// 0x1B 0xE4 0x46 0x09
		// Escape (0x1B) should be followed by a valid character.
		// If not, 0x1B is decoded as SPACE, and the following byte
		// is treated as a non-escaped sequence.
		byte[] bytes = new byte[] {
			(byte) 0x1B, (byte) 0xE4, (byte) 0x46, (byte) 0x09
		};
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		CharBuffer charBuffer = decoder.decode(byteBuffer);

		assertThat(charBuffer.toString()).isEqualTo(" H J");
	}

	@Test
	void encodesAlphaNumericCharacters() throws Exception {
		String string = "It is easy to send text messages. 123";
		CharBuffer in = CharBuffer.wrap(string);
		byte[] expecteds = new byte[] {
			(byte) 0x49, (byte) 0x3A, (byte) 0x28, (byte) 0x3D,
			(byte) 0x07, (byte) 0x95, (byte) 0xC3, (byte) 0xF3,
			(byte) 0x3C, (byte) 0x88, (byte) 0xFE, (byte) 0x06,
			(byte) 0xCD, (byte) 0xCB, (byte) 0x6E, (byte) 0x32,
			(byte) 0x88, (byte) 0x5E, (byte) 0xC6, (byte) 0xD3,
			(byte) 0x41, (byte) 0xED, (byte) 0xF2, (byte) 0x7C,
			(byte) 0x1E, (byte) 0x3E, (byte) 0x97, (byte) 0xE7,
			(byte) 0x2E, (byte) 0x50, (byte) 0x4C, (byte) 0x36,
			(byte) 0x03
		};
		byte[] actuals = encoder.encode(in).array();
		for (int i = 0; i < expecteds.length; i++) {
			assertThat(actuals[i]).withFailMessage("at element " + i).isEqualTo(expecteds[i]);
		}
	}

	@Test
	void encodesNonAlphaNumericCharacters() throws Exception {
		String string = "@$";
		CharBuffer in = CharBuffer.wrap(string);
		byte[] expecteds = new byte[] {
			(byte) 0x00, (byte) 0x01
		};
		byte[] actuals = encoder.encode(in).array();
		for (int i = 0; i < expecteds.length; i++) {
			assertThat(actuals[i]).withFailMessage("at element " + i).isEqualTo(expecteds[i]);
		}
	}

	@Test
	void encodesEscapedCharacters() {
		String string = "{}[]";
		CharBuffer in = CharBuffer.wrap(string);
		byte[] expecteds = new byte[] {
			(byte) 0x1B, (byte) 0xD4, (byte) 0x26, (byte) 0xB5,
			(byte) 0xE1, (byte) 0x6D, (byte) 0x7C
		};
		// As these are ALL escaped characters, we allocate our own byte buffer.
		// Otherwise, the average of 1.3 bytes per character is NOT enough.
		ByteBuffer out = ByteBuffer.allocate(string.length() * 2);
		encoder.encode(in, out, true);
		byte[] actuals = out.array();
		for (int i = 0; i < expecteds.length; i++) {
			assertThat(actuals[i]).withFailMessage("at element " + i).isEqualTo(expecteds[i]);
		}
	}

	@Test
	void usesQuestionMarkToEncodeCharactersOutsideGsmRange() {
		CharBuffer in = CharBuffer.wrap("\u604F\u7D59");
		ByteBuffer out = ByteBuffer.allocate(2);
		encoder.encode(in, out, true);
		// 0x3F 0x3F
		// _011 1111 _011 1111
		// 1011 1111 __01 1111
		// 0xBF 0x1F
		assertThat(out.array()).isEqualTo(new byte[] { (byte) 0xBF, (byte) 0x1F });
	}

	@Test
	void encodeAndDecode() throws Exception {
		CharBuffer charBuffer1 = CharBuffer.wrap("abc123{}[]");
		CharBuffer charBuffer2 = decoder.decode(encoder.encode(charBuffer1));

		assertThat(charBuffer2.toString()).isEqualTo("abc123{}[]");
	}

	@Test
	void trailingCommercialAtIsIgnored() throws Exception {
		byte[] bytes = new byte[] {
			(byte) 0x31, (byte) 0xD9, (byte) 0x8C, (byte) 0x56,
			(byte) 0xB3, (byte) 0xDD, (byte) 0x00
		};
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		CharBuffer charBuffer2 = decoder.decode(byteBuffer);

		assertThat(charBuffer2.toString()).isEqualTo("1234567");
	}

	@Test
	void trailingCommercialAtAmbiguityIsAvoided() {
		String textMessage = "1234567";
		ByteBuffer byteBuffer = ByteBuffer.allocate(
			textMessage.length() * (int) Math.ceil(encoder.maxBytesPerChar()));
		encoder.encode(CharBuffer.wrap(textMessage), byteBuffer, true);
		byte[] bytes = new byte[byteBuffer.position()];
		byteBuffer.flip();
		byteBuffer.get(bytes);
		byte[] expecteds = new byte[] {
			(byte) 0x31, (byte) 0xD9, (byte) 0x8C, (byte) 0x56,
			(byte) 0xB3, (byte) 0xDD, (byte) 0x36
		};

		assertThat(bytes).isEqualTo(expecteds);
	}
}
