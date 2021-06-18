package test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class RestServiceClient {

  private final WebClient webClient;
  private final RestServiceProperties restServiceProperties;
  private final ObjectMapper objectMapper;

  public RestServiceClient(WebClient webClient, RestServiceProperties restServiceProperties, ObjectMapper objectMapper) {
    this.webClient = webClient;
    this.restServiceProperties = restServiceProperties;
    this.objectMapper = objectMapper;
  }

  public void publish(Object object) {
    final String message;
    try {
      message = objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    try {
      webClient
          .post()
          .uri(restServiceProperties.getDownstreamUrl())
          .bodyValue(message)
          .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
          .exchangeToMono(
              response -> {
                HttpStatus httpStatus = response.statusCode();
                if (httpStatus == HttpStatus.OK) {
                  return response.bodyToMono(String.class);
                }
                throw new HttpClientErrorException(httpStatus, httpStatus.getReasonPhrase());
              }
          )
          .block();
      logSuccess();
    } catch (Exception e) {
      log.error("Error: {}", e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
  }

  public void logSuccess() {
    log.info("Success");
  }
}
