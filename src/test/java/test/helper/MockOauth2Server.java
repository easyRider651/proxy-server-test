package test.helper;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_ID;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_SECRET;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.GRANT_TYPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.PASSWORD;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.USERNAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import test.WebClientConfig;

@Slf4j
public class MockOauth2Server {

  private final ObjectMapper mapper = new ObjectMapper();
  private WireMockServer mockOauth2Server;

  public void setup() {
    mockOauth2Server = new WireMockServer(options().port(TestHelper.MOCK_OAUTH2_SERVER_PORT));
    mockOauth2Server.addMockServiceRequestListener(TestHelper::logMockOauth2ServerRequestResponse);
    mockOauth2Server.start();
    mockOauth2Server.stubFor(post(urlEqualTo(TestHelper.OAUTH2_PATH))
        .withRequestBody(containing(GRANT_TYPE + "=" + PASSWORD))
        .withRequestBody(containing(USERNAME + "=" + WebClientConfig.USER))
        .withRequestBody(containing(PASSWORD + "=" + WebClientConfig.PASSWORD))
        .withRequestBody(containing(CLIENT_ID + "=" + WebClientConfig.CLIENT_ID))
        .withRequestBody(containing(CLIENT_SECRET + "=" + WebClientConfig.CLIENT_SECRET))
        .willReturn(okJson(getSuccessResponseBody()))
    );
  }

  public void shutdown() {
    mockOauth2Server.stop();
    mockOauth2Server.shutdown();
  }

  public int countRequestsMatching(String path) {
    RequestPattern requestPattern = WireMock.postRequestedFor(urlEqualTo(path)).build();
    return mockOauth2Server.countRequestsMatching(requestPattern).getCount();
  }

  private String getSuccessResponseBody() {
    try {
      return mapper.writeValueAsString(new TokenResponse(TestHelper.DEFAULT_ACCESS_TOKEN, TestHelper.DEFAULT_ACCESS_EXPIRES,
          TestHelper.DEFAULT_REFRESH_TOKEN, TestHelper.DEFAULT_REFRESH_EXPIRES));
    } catch (IOException e) {
      log.error("Unable to convert object to JSON string", e);
      return null;
    }
  }
}
