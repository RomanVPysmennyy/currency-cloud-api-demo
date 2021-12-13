package com.api.tests;

import com.api.tests.dto.ErrorCode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RatesApiTest extends ApiTestBase {

    @BeforeClass
    public void setup() {
        init();
        authenticate();
    }

    @Test
    @Ignore("Test API is broken at the moment and doesn't return valid response")
    public void successfulCaseTest() {
        final String CURRENCY_PAIR = "GBPUSD";

        List<String> rates = RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", authToken)
                .queryParam("currency_pair", CURRENCY_PAIR)
                // WHEN
                .when()
                .get(BASIC_RATES_ENDPOINT)
                // THEN
                .then()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("rates/basic_rates_successful_response.json"))
                .contentType(ContentType.JSON)
                .body("rates." + CURRENCY_PAIR, hasSize(2))
                .body("unavailable", hasSize(0))
                .extract()
                .body()
                .jsonPath()
                .getList("rates." + CURRENCY_PAIR, String.class);

        // verify that rates are numbers
        rates
                .stream()
                .map(r -> Float.parseFloat(r))
                .collect(Collectors.toSet());
    }

    @Test
    public void currencyPairIsRequiredTest() {
        List<ErrorCode> errorCodes = RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", authToken)
                // WHEN
                .when()
                .get(BASIC_RATES_ENDPOINT)
                // THEN
                .then()
                .statusCode(400)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("rates/currency_pair_error.json"))
                .contentType(ContentType.JSON)
                .body("error_code", is("rates_find_failed"))
                .extract()
                .body()
                .jsonPath()
                .getList("error_messages.currency_pair", ErrorCode.class);

        assertThat(errorCodes, hasSize(2));

        Set<String> codes = errorCodes
                .stream()
                .map(ec -> ec.code)
                .collect(Collectors.toSet());

        assertThat(codes, hasItems("currency_pair_is_required", "currency_pair_is_too_short"));
    }

    @Test
    public void currencyPairIsTooShortTest() {
        List<ErrorCode> errorCodes = RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", authToken)
                .queryParam("currency_pair", "AAA")
                // WHEN
                .when()
                .get(BASIC_RATES_ENDPOINT)
                // THEN
                .then()
                .statusCode(400)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("rates/currency_pair_error.json"))
                .contentType(ContentType.JSON)
                .body("error_code", is("rates_find_failed"))
                .extract()
                .body()
                .jsonPath()
                .getList("error_messages.currency_pair", ErrorCode.class);

        assertThat(errorCodes, hasSize(1));
        assertThat(errorCodes.get(0).code, is("currency_pair_is_too_short"));
    }
}
