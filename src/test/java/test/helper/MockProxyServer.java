package test.helper;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPattern;

public class MockProxyServer {

  private WireMockServer mockProxyServer;

  public void setup() {
    mockProxyServer = new WireMockServer(options().port(TestHelper.MOCK_PROXY_SERVER_PORT));
    mockProxyServer.addMockServiceRequestListener(TestHelper::logMockProxyServerRequestResponse);
    mockProxyServer.start();
    mockProxyServer.stubFor(WireMock.any(TestHelper.MOCK_OAUTH2_SERVER_URL_PATTERN)
        .willReturn(WireMock.aResponse().proxiedFrom("http://localhost:" + TestHelper.MOCK_OAUTH2_SERVER_PORT)));
    mockProxyServer.stubFor(WireMock.any(TestHelper.MOCK_PUBLISH_SERVER_URL_PATTERN)
        .willReturn(WireMock.aResponse().proxiedFrom("http://localhost:" + TestHelper.MOCK_PUBLISH_SERVER_PORT)));
  }

  public void shutdown() {
    mockProxyServer.stop();
    mockProxyServer.shutdown();
  }

  public int countRequestsMatching(String path) {
    RequestPattern requestPattern = WireMock.postRequestedFor(urlEqualTo(path)).build();
    return mockProxyServer.countRequestsMatching(requestPattern).getCount();
  }
}
