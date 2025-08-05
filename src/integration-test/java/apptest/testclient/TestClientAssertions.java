package apptest.testclient;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.Parameter;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class TestClientAssertions {

	public static TestClientAssert assertThat(final TestClient testClient) {
		return new TestClientAssert(testClient);
	}

	public static class TestClientAssert extends AbstractAssert<TestClientAssert, TestClient> {

		TestClientAssert(final TestClient testClient) {
			super(testClient, TestClientAssert.class);
		}

		public void hasSuccessResponse() {
			isNotNull();

			var responsePacket = actual.getReceivedPackets()
				.stream()
				.filter(Packet::isPositiveResponse)
				.toList()
				.getFirst();

			Assertions.assertThat(responsePacket.getParameter(Parameter.ERROR_CODE)).isNull();
			Assertions.assertThat(responsePacket.getParameter(Parameter.ERROR_TEXT)).isNull();
			Assertions.assertThat(responsePacket.getParameter(Parameter.MC_TIMESTAMP)).isNotNull();
		}

		public void hasFailureResponse() {
			isNotNull();

			var responsePacket = actual.getReceivedPackets()
				.stream()
				.filter(Packet::isNegativeResponse)
				.toList()
				.getFirst();

			Assertions.assertThat(responsePacket.getParameter(Parameter.ERROR_CODE)).isNotNull();
			Assertions.assertThat(responsePacket.getParameter(Parameter.ERROR_TEXT)).isNotNull().extracting(Parameter::getValue).isEqualTo("Requested operation failed");
			Assertions.assertThat(responsePacket.getParameter(Parameter.MC_TIMESTAMP)).isNull();
		}
	}
}
