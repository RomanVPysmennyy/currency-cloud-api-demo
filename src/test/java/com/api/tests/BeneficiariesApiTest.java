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

public class BeneficiariesApiTest extends ApiTestBase {

    @BeforeClass
    public void setup() {
        init();
        authenticate();
    }

    @Test
    public void successfulCaseTest() {
        final String NAME = "Test";
        final String BANK_ACCOUNT_HOLDER_NAME = "Test";
        final String BANK_COUNTRY = "DE";
        final String CURRENCY = "EUR";
        final String IBAN = "AL47 2121 1009 0000 0002 3569 87411";

        String id = RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", authToken)
                .contentType("multipart/form-data")
                .multiPart("name", NAME)
                .multiPart("bank_account_holder_name", BANK_ACCOUNT_HOLDER_NAME)
                .multiPart("bank_country", BANK_COUNTRY)
                .multiPart("currency", CURRENCY)
                .multiPart("iban", IBAN)
                // WHEN
                .when()
                .post(BENEFICIARIES_ENDPOINT)
                // THEN
                .then()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("beneficiaries/create_response.json"))
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
    public void bankAccountHolderNameMissingTest() {
        final String NAME = "Test";
        final String BANK_COUNTRY = "DE";
        final String CURRENCY = "EUR";
        final String IBAN = "AL47 2121 1009 0000 0002 3569 87411";

        List<ErrorCode> errorCodes = RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", authToken)
                .contentType("multipart/form-data")
                .multiPart("name", NAME)
                .multiPart("bank_country", BANK_COUNTRY)
                .multiPart("currency", CURRENCY)
                .multiPart("iban", IBAN)
                // WHEN
                .when()
                .post(BENEFICIARIES_ENDPOINT)
                // THEN
                .then()
                .statusCode(400)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("beneficiaries/error.json"))
                .contentType(ContentType.JSON)
                .body("error_code", is("beneficiary_create_failed"))
                .extract()
                .body()
                .jsonPath()
                .getList("error_messages.bank_account_holder_name", ErrorCode.class);

        assertThat(errorCodes, hasSize(3));

        Set<String> codes = errorCodes
                .stream()
                .map(ec -> ec.code)
                .collect(Collectors.toSet());

        assertThat(codes, hasItems("bank_account_holder_name_is_required", "bank_account_holder_name_is_too_short", "bank_account_holder_name_is_in_invalid_format"));
    }
}
