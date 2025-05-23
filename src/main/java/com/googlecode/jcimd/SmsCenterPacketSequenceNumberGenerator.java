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
 * 
 */
package com.googlecode.jcimd;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is used by SMS Center to send packets to application.
 * Generates <em>even</em> packet numbers starting from <em>zero</em>.
 *
 * @author Lorenzo Dee
 */
public class SmsCenterPacketSequenceNumberGenerator implements PacketSequenceNumberGenerator {

	private final AtomicInteger sequence = new AtomicInteger(0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.integration.cimd.PacketSequenceNumberGenerator#nextSequence()
	 */
	@Override
	public int nextSequence() {
		synchronized (sequence) {
			int current = sequence.get();
			sequence.set((sequence.get() + 2) & 0x00FF);
			return current;
		}
	}
}
