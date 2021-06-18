package test.helper;

import java.io.IOException;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

public class MockDownstreamServer {

  private MockWebServer mockDownstreamServer;

  public void setup() throws IOException {
    mockDownstreamServer = new MockWebServer();
    mockDownstreamServer.start(TestHelper.MOCK_PUBLISH_SERVER_PORT);
    Dispatcher publishDispatcher = new Dispatcher() {
      @NotNull
      @Override
      public MockResponse dispatch(@NotNull RecordedRequest request) {
        TestHelper.logMockDownstreamServerRequest(request);
        return new MockResponse().setResponseCode(HttpStatus.OK.value()).setBody("");
      }
    };
    mockDownstreamServer.setDispatcher(publishDispatcher);
  }

  public void shutdown() throws IOException {
    mockDownstreamServer.shutdown();
  }

  public int getRequestCount() {
    return mockDownstreamServer.getRequestCount();
  }
}
