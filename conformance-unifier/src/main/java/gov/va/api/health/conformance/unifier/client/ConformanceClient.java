package gov.va.api.health.conformance.unifier.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
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
public class ConformanceClient {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final RestTemplate restTemplate;

  public ConformanceClient(@Autowired RestTemplateBuilder restTemplateBuilder) {
    restTemplate = restTemplateBuilder.build();
  }

  private HttpEntity<Void> requestEntity() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
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
    return search(query, MAPPER);
  }

  /**
   * Search.
   *
   * @param query Query.
   * @param <T> Class.
   * @param mapper Jackson mapper to use for deserialization.
   * @return Response.
   */
  @SneakyThrows
  public <T> T search(Query<T> query, ObjectMapper mapper) {
    // TODO: we may wish to use an error handler to capture certain exceptions from the rest call.
    var typeRef = ParameterizedTypeReference.forType(query.getType());
    TypeReference<T> tr =
        new TypeReference<>() {
          public Type getType() {
            return typeRef.getType();
          }
        };

    ResponseEntity<String> responseEntity =
        restTemplate.exchange(urlOf(query), HttpMethod.GET, requestEntity(), String.class);
    return mapper.readValue(responseEntity.getBody(), tr);
  }

  private String urlOf(Query<?> query) {
    return query.toQueryString();
  }
}
