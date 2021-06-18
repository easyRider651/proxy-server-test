package test.helper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TokenResponse {

  @JsonProperty("token_type")
  public final String tokenType = "bearer";
  @JsonProperty("access_token")
  public String accessToken;
  @JsonProperty("expires_in")
  public Integer expiresIn;
  @JsonProperty("refresh_token")
  public String refreshToken;
  @JsonProperty("refresh_expires_in")
  public Integer refreshExpiresIn;
}
