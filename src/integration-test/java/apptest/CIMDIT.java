package apptest;

import static apptest.testclient.TestClientAssertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import apptest.testclient.TestClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;
import se.sundsvall.cimdproxy.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@ActiveProfiles("it")
@WireMockAppTestSuite(files = "classpath:/CIMDIT/", classes = Application.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
class CIMDIT extends AbstractAppTest {

    private static int cimdPort;

    @DynamicPropertySource
    static public void registerCimdPort(final DynamicPropertyRegistry dynamicPropertyRegistry) {
        cimdPort = TestSocketUtils.findAvailableTcpPort();

        dynamicPropertyRegistry.add("cimd.port", () -> cimdPort);
    }

    @Test
    void test1_successfulRequest() throws Exception {
        // Create a dummy call, actually expecting a 404 NOT FOUND, just to get the WireMock (lifecycle) to play nice...
        var call = setupCall()
            .withServicePath("/")
            .withHttpMethod(GET)
            .withExpectedResponseStatus(NOT_FOUND);

        // Create the CIMD test client
        var testClient = new TestClient("localhost", cimdPort);
        // Send a message
        testClient.send("+46701234567", "Hello, there!!");

        // Verify that all configured stubs have been called
        call.sendRequestAndVerifyResponse();

        // Verify that the test client has gotten the expected response
        assertThat(testClient).hasSuccessResponse();
    }


    @Test
    void test2_failureResponseFromSmsSender() throws Exception {
        // Create a dummy call, actually expecting a 404 NOT FOUND, just to get WireMock to play nice
        var call = setupCall()
            .withServicePath("/")
            .withHttpMethod(GET)
            .withExpectedResponseStatus(NOT_FOUND);

        // Create the CIMD test client
        var testClient = new TestClient("localhost", cimdPort);
        // Send a message
        testClient.send("+46701234567", "Hello, there!!");

        // Verify that all configured stubs have been called
        call.sendRequestAndVerifyResponse();

        // Verify that the test client has gotten the expected response
        assertThat(testClient).hasFailureResponse();
    }
}
