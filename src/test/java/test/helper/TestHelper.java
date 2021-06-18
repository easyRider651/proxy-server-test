package test.helper;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.RecordedRequest;

@Slf4j
public class TestHelper {

  public static final String OAUTH2_PATH = "/oauth2";
  public static final String API_PATH = "/api";

  public static final String DEFAULT_ACCESS_TOKEN = "ACCESS-TOKEN";
  public static final Integer DEFAULT_ACCESS_EXPIRES = 60;
  public static final String DEFAULT_REFRESH_TOKEN = "REFRESH-TOKEN";
  public static final Integer DEFAULT_REFRESH_EXPIRES = 1800;

  public static final int MOCK_OAUTH2_SERVER_PORT = 8080;
  public static final int MOCK_PUBLISH_SERVER_PORT = 8088;
  public static final int MOCK_PROXY_SERVER_PORT = 8090;

  public static UrlPattern MOCK_OAUTH2_SERVER_URL_PATTERN = UrlPattern.fromOneOf(OAUTH2_PATH, null, null, null);
  public static UrlPattern MOCK_PUBLISH_SERVER_URL_PATTERN = UrlPattern.fromOneOf(API_PATH, null, null, null);

  private TestHelper() {
    // Do not instantiate.
  }

  public static void logMockDownstreamServerRequest(RecordedRequest request) {
    log.info("MockDownstreamServer request method: {} URL: {}", request.getMethod(), request.getRequestUrl());
    request.getHeaders().forEach(pair -> log.info("MockDownstreamServer request header name: {} value: {}", pair.component1(), pair.component2()));
    log.info("MockDownstreamServer request bodySize: {}", request.getBodySize());
  }

  public static void logMockProxyServerRequestResponse(Request request, Response response) {
    log.info("MockProxyServer request method: {} URL: {}", request.getMethod(), request.getAbsoluteUrl());
    request.getHeaders().all().forEach(header -> header.values().forEach(value ->
        log.info("MockProxyServer request header name: {} value: {}", header.key(), value)));
    String requestBody = request.getBodyAsString();
    log.info("MockProxyServer request bodySize: {}", requestBody.length());
    log.info("MockProxyServer request body: {}", requestBody);
    log.info("MockProxyServer response status: {}", response.getStatus());
    response.getHeaders().all().forEach(header -> header.values().forEach(value ->
        log.info("MockProxyServer response header name: {} value: {}", header.key(), value)));
    String responseBody = response.getBodyAsString();
    if (responseBody != null) {
      log.info("MockProxyServer response bodySize: {}", responseBody.length());
    }
    log.info("MockProxyServer response body: {}", responseBody);
  }

  public static void logMockOauth2ServerRequestResponse(Request request, Response response) {
    log.info("MockOauth2Server request method: {} URL: {}", request.getMethod(), request.getAbsoluteUrl());
    request.getHeaders().all().forEach(header -> header.values().forEach(value ->
        log.info("MockOauth2Server request header name: {} value: {}", header.key(), value)));
    log.info("MockOauth2Server request body: {}", request.getBodyAsString());
    log.info("MockOauth2Server response status: {}", response.getStatus());
    response.getHeaders().all().forEach(header -> header.values().forEach(value ->
        log.info("MockOauth2Server response header name: {} value: {}", header.key(), value)));
    log.info("MockOauth2Server response body: {}", response.getBodyAsString());
  }
}
