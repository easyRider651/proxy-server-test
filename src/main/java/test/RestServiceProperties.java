package test;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "endpoint")
@Getter
@Setter
public class RestServiceProperties {

  private String downstreamUrl;
  private String oauth2Url;
  private String proxyUrl;
}
