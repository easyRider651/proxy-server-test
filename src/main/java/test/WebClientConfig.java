package test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class WebClientConfig {

  private static final String REGISTRATION_ID = "registrationId";
  public static final String CLIENT_ID = "testclient";
  public static final String CLIENT_SECRET = "secret";
  public static final String USER = "user";
  public static final String PASSWORD = "password";

  private final RestServiceProperties restServiceProperties;

  public WebClientConfig(RestServiceProperties restServiceProperties) {
    this.restServiceProperties = restServiceProperties;
  }

  @Bean
  public ReactiveClientRegistrationRepository clientRegistrationRepository() {

    log.info("downstreamUrl: {}", restServiceProperties.getDownstreamUrl());
    log.info("oauth2Url    : {}", restServiceProperties.getOauth2Url());
    log.info("proxyUrl     : {}", restServiceProperties.getProxyUrl());

    ClientRegistration clientRegistration = ClientRegistration
        .withRegistrationId(REGISTRATION_ID)
        .authorizationGrantType(AuthorizationGrantType.PASSWORD)
        .clientId(CLIENT_ID)
        .clientSecret(CLIENT_SECRET)
        .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
        .userInfoAuthenticationMethod(AuthenticationMethod.FORM)
        .tokenUri(restServiceProperties.getOauth2Url())
        .build();
    return new InMemoryReactiveClientRegistrationRepository(clientRegistration);
  }

  @Bean
  public ReactiveOAuth2AuthorizedClientService authorizedClientService(ReactiveClientRegistrationRepository clientRegistrationRepository) {
    return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
  }

  @Bean
  public ClientHttpConnector clientHttpConnector() {
    log.debug("The proxy for the calls to the REST server is {}", restServiceProperties.getProxyUrl());
    HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom();
    if (Strings.isNotEmpty(restServiceProperties.getProxyUrl())) {
      try {
        HttpHost proxy = HttpHost.create(restServiceProperties.getProxyUrl());
        clientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
      } catch (URISyntaxException e) {
        log.error("Error: {}", e.getMessage());
        throw new RuntimeException(e);
      }
    }
    CloseableHttpAsyncClient client = clientBuilder.build();
    return new HttpComponentsClientHttpConnector(client);
  }

  @Bean
  public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(ReactiveClientRegistrationRepository clientRegistrationRepository,
      ReactiveOAuth2AuthorizedClientService authorizedClientService, ClientHttpConnector clientHttpConnector, WebClient.Builder webClientBuilder) {

    WebClient webClient = webClientBuilder
        .clientConnector(clientHttpConnector)
        .filter(logRequest())
        .build();

    AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
        clientRegistrationRepository, authorizedClientService);
    authorizedClientManager.setAuthorizedClientProvider(createAuthorizedClientProvider(webClient));
    authorizedClientManager.setContextAttributesMapper(request -> {
      Map<String, Object> contextAttributes = new HashMap<>();
      contextAttributes.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, USER);
      contextAttributes.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, PASSWORD);
      return Mono.just(contextAttributes);
    });

    return authorizedClientManager;
  }

  @Bean
  public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
      ClientHttpConnector clientHttpConnector, WebClient.Builder webClientBuilder) {

    ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2FilterFunction = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
    oauth2FilterFunction.setDefaultClientRegistrationId(REGISTRATION_ID);

    return webClientBuilder
        .clientConnector(clientHttpConnector)
        .filters(exchangeFilterFunctions -> {
          exchangeFilterFunctions.add(oauth2FilterFunction);
          exchangeFilterFunctions.add(logRequest());
        })
        .build();
  }

  private ReactiveOAuth2AuthorizedClientProvider createAuthorizedClientProvider(WebClient webClient) {
    WebClientReactiveClientCredentialsTokenResponseClient clientCredentialsTokenResponseClient = new WebClientReactiveClientCredentialsTokenResponseClient();
    clientCredentialsTokenResponseClient.setWebClient(webClient);
    return ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
        .clientCredentials(builder -> builder.accessTokenResponseClient(clientCredentialsTokenResponseClient))
        .password()
        .refreshToken()
        .build();
  }

  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
      log.info("Request: method: {} URL: {}", clientRequest.method(), clientRequest.url());
      clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("Request header: name: {} value: {}", name, value)));
      return Mono.just(clientRequest);
    });
  }
}