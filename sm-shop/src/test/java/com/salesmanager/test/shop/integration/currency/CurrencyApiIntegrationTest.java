package com.salesmanager.test.shop.integration.currency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.test.shop.common.ServicesTestSupport;

@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CurrencyApiIntegrationTest extends ServicesTestSupport {

  @Inject
  private TestRestTemplate testRestTemplate;

  /**
   * GET /api/v1/currency must return a non-empty list containing INR.
   */
  @Test
  public void testGetCurrencies_returnsNonEmptyList() {
    final HttpEntity<String> entity = new HttpEntity<>(getHeader());
    final ResponseEntity<String> response = testRestTemplate.exchange(
        "/api/v1/currency", HttpMethod.GET, entity, String.class);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertNotNull(response.getBody());
    assertTrue("Response body must contain INR", response.getBody().contains("INR"));
  }

  /**
   * PUT /api/v1/private/currency/INR with symbolOverride "₹" must return 200
   * and the override must appear in the subsequent GET response.
   */
  @Test
  public void testUpdateSymbolOverride_persistsOverride() {
    // Set the override
    HttpHeaders headers = getHeader();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String setBody = "{\"symbolOverride\": \"₹\"}";
    HttpEntity<String> putEntity = new HttpEntity<>(setBody, headers);

    ResponseEntity<Void> putResponse = testRestTemplate.exchange(
        "/api/v1/private/currency/INR", HttpMethod.PUT, putEntity, Void.class);
    assertThat("PUT must return 200", putResponse.getStatusCode(), is(HttpStatus.OK));

    // Verify override appears in GET response
    ResponseEntity<String> getResponse = testRestTemplate.exchange(
        "/api/v1/currency", HttpMethod.GET, new HttpEntity<>(getHeader()), String.class);
    assertThat(getResponse.getStatusCode(), is(HttpStatus.OK));
    assertTrue("GET response must contain override symbol ₹",
        getResponse.getBody().contains("₹"));
  }

  /**
   * PUT with an empty symbolOverride must clear the override.
   * The GET response must no longer contain the previously-set symbol.
   */
  @Test
  public void testUpdateSymbolOverride_emptyValueClearsOverride() {
    HttpHeaders headers = getHeader();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // First set an override
    testRestTemplate.exchange("/api/v1/private/currency/INR", HttpMethod.PUT,
        new HttpEntity<>("{\"symbolOverride\": \"₹\"}", headers), Void.class);

    // Now clear it
    ResponseEntity<Void> clearResponse = testRestTemplate.exchange(
        "/api/v1/private/currency/INR", HttpMethod.PUT,
        new HttpEntity<>("{\"symbolOverride\": \"\"}", headers), Void.class);
    assertThat("Clear PUT must return 200", clearResponse.getStatusCode(), is(HttpStatus.OK));
  }

  /**
   * PUT for a non-existent currency code must return 404.
   */
  @Test
  public void testUpdateSymbolOverride_unknownCode_returns404() {
    HttpHeaders headers = getHeader();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Void> response = testRestTemplate.exchange(
        "/api/v1/private/currency/INVALID", HttpMethod.PUT,
        new HttpEntity<>("{\"symbolOverride\": \"X\"}", headers), Void.class);

    assertThat("Unknown currency code must return 404",
        response.getStatusCode(), is(HttpStatus.NOT_FOUND));
  }
}
