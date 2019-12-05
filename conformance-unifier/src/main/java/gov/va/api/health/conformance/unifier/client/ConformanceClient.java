package gov.va.api.health.conformance.unifier.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** Conformance Client. */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ConformanceClient {

  private final RestTemplate restTemplate;

  private HttpEntity<Void> requestEntity(final String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
    if (token != null) {
      headers.add("Authorization", "Bearer " + token);
    }
    return new HttpEntity<>(headers);
  }

  /**
   * Search.
   *
   * @param query Query.
   * @param <T> Class.
   * @return Response.
   */
  public <T> T search(Query<T> query) {
    return search(query, null);
  }

  /**
   * Search.
   *
   * @param query Query.
   * @param token Access token.
   * @param <T> Class.
   * @return Response.
   */
  public <T> T search(Query<T> query, String token) {
    // TODO: we may wish to use an error handler to capture certain exceptions from the rest call.
    ResponseEntity<T> responseEntity =
        restTemplate.exchange(
            urlOf(query),
            HttpMethod.GET,
            requestEntity(token),
            ParameterizedTypeReference.forType(query.getType()));
    return responseEntity.getBody();
  }

  private String urlOf(Query<?> query) {
    return query.toQueryString();
  }
}
