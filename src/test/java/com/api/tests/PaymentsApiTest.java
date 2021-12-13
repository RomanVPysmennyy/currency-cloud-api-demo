package com.api.tests;

import com.api.tests.dto.ErrorCode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;

public class PaymentsApiTest extends ApiTestBase {

    @BeforeClass
    public void setup() {
        init();
        authenticate();
    }

    @Test
    public void successfulCaseTest() {
        final String CURRENCY = "EUR";
        final String BENEFICIARY_ID = "e01bdf1e-5e97-411c-b325-6d50b75c811c";
        final String AMOUNT = "100";
        final String REASON = "Test";
        final String REFERENCE = "Test";
        final String PAYMENT_TYPE = "regular";

        String id = RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", authToken)
                .contentType("multipart/form-data")
                .multiPart("currency", CURRENCY)
                .multiPart("beneficiary_id", BENEFICIARY_ID)
                .multiPart("amount", AMOUNT)
                .multiPart("reason", REASON)
                .multiPart("reference", REFERENCE)
                .multiPart("payment_type", PAYMENT_TYPE)
                // WHEN
                .when()
                .post(PAYMENTS_ENDPOINT)
                // THEN
                .then()
                .log().all()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("payments/create_response.json"))
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
    public void beneficiaryIdIsRequiredTest() {
        final String CURRENCY = "EUR";
        final String AMOUNT = "100";
        final String REASON = "Test";
        final String REFERENCE = "Test";
        final String PAYMENT_TYPE = "regular";

        List<ErrorCode> errorCodes = RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", authToken)
                .contentType("multipart/form-data")
                .multiPart("currency", CURRENCY)
                .multiPart("amount", AMOUNT)
                .multiPart("reason", REASON)
                .multiPart("reference", REFERENCE)
                .multiPart("payment_type", PAYMENT_TYPE)
                // WHEN
                .when()
                .post(PAYMENTS_ENDPOINT)
                // THEN
                .then()
                .statusCode(400)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("payments/error.json"))
                .contentType(ContentType.JSON)
                .body("error_code", is("payment_create_failed"))
                .extract()
                .body()
                .jsonPath()
                .getList("error_messages.beneficiary_id", ErrorCode.class);

        assertThat(errorCodes, hasSize(2));

        Set<String> codes = errorCodes
                .stream()
                .map(ec -> ec.code)
                .collect(Collectors.toSet());

        assertThat(codes, hasItems("beneficiary_id_is_required", "beneficiary_id_is_not_valid_uuid"));
    }
}
