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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PacketSequenceNumberGeneratorTest {

	private PacketSequenceNumberGenerator appGenerator;
	private PacketSequenceNumberGenerator smscGenerator;

	@BeforeEach
	void setUp() {
		appGenerator = new ApplicationPacketSequenceNumberGenerator();
		smscGenerator = new SmsCenterPacketSequenceNumberGenerator();
	}

	@Test
	void generatesOddNumbersAndRollsBackToOne() {
		for (int i = 1; i <= 255; i += 2) {
			assertThat(appGenerator.nextSequence()).isEqualTo(i);
		}
		assertThat(appGenerator.nextSequence()).isEqualTo(1);
	}

	@Test
	void generatesEvenNumbersAndRollsBackToZero() {
		for (int i = 0; i <= 255; i += 2) {
			assertThat(smscGenerator.nextSequence()).isEqualTo(i);
		}
		assertThat(smscGenerator.nextSequence()).isZero();
	}
}
