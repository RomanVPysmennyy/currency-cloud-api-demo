package com.api.tests;

import com.api.tests.dto.ErrorCode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;

public class ConversionApiTest extends ApiTestBase {

    @BeforeClass
    public void setup() {
        init();
        authenticate();
    }

    @Test
    @Ignore("Test API is broken at the moment and doesn't return valid response")
    public void successfulCaseTest() {
        final String BUY_CURRENCY = "GBP";
        final String SELL_CURRENCY = "USD";
        final String FIXED_SIDE = "sell";
        final String AMOUNT = "100";
        final String TERM_AGREEMENT = "true";

        String id = RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", authToken)
                .contentType("multipart/form-data")
                .multiPart("buy_currency", BUY_CURRENCY)
                .multiPart("sell_currency", SELL_CURRENCY)
                .multiPart("fixed_side", FIXED_SIDE)
                .multiPart("amount", AMOUNT)
                .multiPart("term_agreement", TERM_AGREEMENT)
                // WHEN
                .when()
                .post(CONVERSION_ENDPOINT)
                // THEN
                .then()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("conversion/create_conversion_response.json"))
                .contentType(ContentType.JSON)
                .extract()
                .path("id");

        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            fail("id should be UUID");
        }
    }

    @Test
    public void sellCurrencyIsRequiredTest() {
        final String BUY_CURRENCY = "GBP";
        final String FIXED_SIDE = "sell";
        final String AMOUNT = "100";
        final String TERM_AGREEMENT = "true";

        List<ErrorCode> errorCodes = RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", authToken)
                .contentType("multipart/form-data")
                .multiPart("buy_currency", BUY_CURRENCY)
                .multiPart("fixed_side", FIXED_SIDE)
                .multiPart("amount", AMOUNT)
                .multiPart("term_agreement", TERM_AGREEMENT)
                // WHEN
                .when()
                .post(CONVERSION_ENDPOINT)
                // THEN
                .then()
                .statusCode(400)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("conversion/conversion_error.json"))
                .contentType(ContentType.JSON)
                .body("error_code", is("conversion_create_failed"))
                .extract()
                .body()
                .jsonPath()
                .getList("error_messages.sell_currency", ErrorCode.class);

        assertThat(errorCodes, hasSize(2));

        Set<String> codes = errorCodes
                .stream()
                .map(ec -> ec.code)
                .collect(Collectors.toSet());

        assertThat(codes, hasItems("sell_currency_is_required", "sell_currency_is_in_invalid_format"));
    }
}
