package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.helper.MockDownstreamServer;
import test.helper.MockOauth2Server;
import test.helper.MockProxyServer;
import test.helper.TestHelper;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WebClientApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = { "endpoint.downstreamUrl=http://downstreamhost:8088/api",
                   "endpoint.proxyUrl=http://localhost:8090",
                   "endpoint.oauth2Url=http://oauth2host:8080/oauth2" })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ProxyServerTest {

  @SpyBean
  private RestServiceClient restServiceClient;
  private MockDownstreamServer mockDownstreamServer;
  private MockProxyServer mockProxyServer;
  private MockOauth2Server mockOauth2Server;

  @BeforeEach
  void setUp() throws IOException, InterruptedException {
    mockDownstreamServer = new MockDownstreamServer();
    mockDownstreamServer.setup();
    mockProxyServer = new MockProxyServer();
    mockProxyServer.setup();
    mockOauth2Server = new MockOauth2Server();
    mockOauth2Server.setup();
    Thread.sleep(5000);
  }

  @AfterEach
  void cleanUp() throws IOException, InterruptedException {
    mockDownstreamServer.shutdown();
    mockProxyServer.shutdown();
    mockOauth2Server.shutdown();
    Thread.sleep(5000);
  }

  @Test
  void accessOauth2ServerAndDownstreamServerThroughProxyServer() throws InterruptedException {

    restServiceClient.publish("Test message");

    Thread.sleep(5000);

    verify(restServiceClient, timeout(2000).times(1)).publish(any());
    verify(restServiceClient, timeout(2000).times(1)).logSuccess();
    assertEquals(1, mockDownstreamServer.getRequestCount());
    assertEquals(1, mockOauth2Server.countRequestsMatching(TestHelper.OAUTH2_PATH));
    assertEquals(1, mockProxyServer.countRequestsMatching(TestHelper.OAUTH2_PATH));
    assertEquals(1, mockProxyServer.countRequestsMatching(TestHelper.API_PATH));
  }
}
