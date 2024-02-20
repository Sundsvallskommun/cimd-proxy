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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultSessionTest {

	private static DummyCimdServer server;
	private static final int port = 9971;
	private static final String host = "localhost";

	private final String username = "user01";
	private final String password = "seCreT";
	private Session session;

	private ConnectionFactory connectionFactory;

	@BeforeAll
	static void setUpCimd2Server() throws Exception {
		server = new DummyCimdServer(port);
		server.start();
	}

	@AfterAll
	static void tearDownCimd2Server() throws Exception {
		server.stop();
	}

	@BeforeEach
	void setUp() {
		connectionFactory = new TcpNetConnectionFactory(
				host, port, username, password);
	}

	@AfterEach
	void tearDown() {
		server.getReceivedCommands().clear();
	}

	@Test
	void testSubmitMessage() throws Exception {
		session = new DefaultSession(connectionFactory);
		try {
			String destinationAddress = "+19098858888";
			UserData userData = new StringUserData("Hi there");
			
			submitMessage(destinationAddress, userData);

			assertThat(server.getReceivedCommands())
				.withFailMessage("Two messages expected")
				.hasSize(2);
			assertThat(server.getReceivedCommands().get(0).getOperationCode())
				.withFailMessage("Login message expected")
				.isEqualTo(1);

			Packet submitMessagePacket = server.getReceivedCommands().get(1);
			assertThat(submitMessagePacket.getOperationCode())
				.withFailMessage("Submit message expected")
				.isEqualTo(3);
			assertThat(submitMessagePacket.getParameter(21).getValue())
				.withFailMessage("Destination address parameter expected")
				.isEqualTo(destinationAddress);
			assertThat(submitMessagePacket.getParameter(33).getValue())
				.withFailMessage("User data parameter expected")
				.isEqualTo(userData.getBody());
		} finally {
			server.getReceivedCommands().clear();
			session.close();

			assertThat(server.getReceivedCommands()).withFailMessage("One message expected").hasSize(1);
			assertThat(server.getReceivedCommands().get(0).getOperationCode())
				.withFailMessage("Logout message expected")
				.isEqualTo(2);
		}
	}

	@Test
	void reconnectsAfterServerDisconnectsDueToInactivity() throws Exception {
		session = new DefaultSession(connectionFactory);
		try {
			String destinationAddress = "+19098858888";
			UserData userData = new StringUserData("Hi there");
			
			submitMessage(destinationAddress, userData);

			System.err.println("Pausing for server to disconnect...");
			Thread.sleep(3000);

			// The session should get a new connection from connection factory
			submitMessage(destinationAddress, userData);
		} finally {
			server.getReceivedCommands().clear();
			session.close();


			assertThat(server.getReceivedCommands()).withFailMessage("One message expected").hasSize(1);
			assertThat(server.getReceivedCommands().get(0).getOperationCode())
					.withFailMessage("Logout message expected")
					.isEqualTo(2);
		}
	}

	private void submitMessage(String destinationAddress, UserData userData) throws Exception {
		String originatingAddress = null;
		String alphanumericOriginatingAddress = null;
		Boolean moreMessagesToSend = null;
		TimePeriod validityPeriod = null;
		Integer protocolIdentifier = null;
		TimePeriod firstDeliveryTime = null;
		Boolean replyPathEnabled = null;
		Integer statusReportRequest = null;
		Boolean cancelEnabled = null;
		Integer tariffClass = null;
		Integer serviceDescription = null;
		Integer priority = null;

		session.submitMessage(
			destinationAddress,
			originatingAddress, alphanumericOriginatingAddress,
			userData,
			moreMessagesToSend,
			validityPeriod,
			protocolIdentifier,
			firstDeliveryTime,
			replyPathEnabled,
			statusReportRequest,
			cancelEnabled,
			tariffClass,
			serviceDescription,
			priority);
	}
}
