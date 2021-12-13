package com.api.tests;

import com.api.tests.dto.ErrorCode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthenticationApiTest extends ApiTestBase {

    @BeforeClass
    public void setup() {
        init();
    }

    @Test
    public void loginSucceededTest() {
        String authToken = given()
                .log().all()
                .contentType("multipart/form-data")
                .multiPart("login_id", LOGIN_ID)
                .multiPart("api_key", API_KEY)
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .path("auth_token");

        assertThat(authToken, hasLength(32));
    }

    @Test
    public void loginIdMissingTest() {
        List<ErrorCode> errorCodes = RestAssured
                // GIVEN
                .given()
                .log().all()
                .contentType("multipart/form-data")
                .multiPart("api_key", API_KEY)
                // WHEN
                .when()
                .post(LOGIN_ENDPOINT)
                // THEN
                .then()
                .statusCode(400)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("authenticate/login_id_missing.json"))
                .contentType(ContentType.JSON)
                .body("error_code", is("auth_invalid_user_login_details"))
                .extract()
                .body()
                .jsonPath()
                .getList("error_messages.login_id", ErrorCode.class);

        assertThat(errorCodes, hasSize(2));

        Set<String> codes = errorCodes
                .stream()
                .map(ec -> ec.code)
                .collect(Collectors.toSet());

        assertThat(codes, hasItems("login_id_is_required", "login_id_is_too_short"));
    }

    @Test
    public void loginIdIsTooShortTest() {
        List<ErrorCode> errorCodes = RestAssured
                // GIVEN
                .given()
                .log().all()
                .contentType("multipart/form-data")
                .multiPart("login_id", "")
                .multiPart("api_key", API_KEY)
                // WHEN
                .when()
                .post(LOGIN_ENDPOINT)
                // THEN
                .then()
                .statusCode(400)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("authenticate/login_id_empty_string.json"))
                .contentType(ContentType.JSON)
                .body("error_code", is("auth_invalid_user_login_details"))
                .extract()
                .body()
                .jsonPath()
                .getList("error_messages.login_id", ErrorCode.class);

        assertThat(errorCodes, hasSize(1));
        assertThat(errorCodes.get(0).code, is("login_id_is_too_short"));
    }

    @Test
    public void loginInvalidSuppliedCredentialsTest() {
        List<ErrorCode> errorCodes = RestAssured
                // GIVEN
                .given()
                .log().all()
                .contentType("multipart/form-data")
                .multiPart("login_id", "aaa")
                .multiPart("api_key", API_KEY)
                // WHEN
                .when()
                .post(LOGIN_ENDPOINT)
                // THEN
                .then()
                .statusCode(401)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("invalid_supplied_credentials.json"))
                .contentType(ContentType.JSON)
                .body("error_code", is("auth_failed"))
                .extract()
                .body()
                .jsonPath()
                .getList("error_messages.username", ErrorCode.class);

        assertThat(errorCodes, hasSize(1));
        assertThat(errorCodes.get(0).code, is("invalid_supplied_credentials"));
    }

    @Test
    public void loginApiKeyMissingTest() {
        List<ErrorCode> errorCodes = RestAssured
                // GIVEN
                .given()
                .log().all()
                .contentType("multipart/form-data")
                .multiPart("login_id", LOGIN_ID)
                // WHEN
                .when()
                .post(LOGIN_ENDPOINT)
                // THEN
                .then()
                .statusCode(400)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("authenticate/login_api_key_missing.json"))
                .contentType(ContentType.JSON)
                .body("error_code", is("auth_invalid_user_login_details"))
                .extract()
                .body()
                .jsonPath()
                .getList("error_messages.api_key", ErrorCode.class);

        assertThat(errorCodes, hasSize(2));

        Set<String> codes = errorCodes
                .stream()
                .map(ec -> ec.code)
                .collect(Collectors.toSet());

        assertThat(codes, hasItems("api_key_is_required", "api_key_length_is_invalid"));
    }

    @Test
    public void logoutSucceededTest() {
        String authToken = login();

        RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", authToken)
                // WHEN
                .when()
                .post(LOGOUT_ENDPOINT)
                // THEN
                .then()
                .statusCode(200)
                .assertThat()
                .contentType(ContentType.JSON)
                .body(is("{}"));
    }

    @Test
    public void logoutInvalidSuppliedCredentialsTest() {
        List<ErrorCode> errorCodes = RestAssured
                // GIVEN
                .given()
                .log().all()
                .header("X-Auth-Token", "this is invalid token")
                // WHEN
                .when()
                .post(LOGOUT_ENDPOINT)
                // THEN
                .then()
                .statusCode(401)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("invalid_supplied_credentials.json"))
                .contentType(ContentType.JSON)
                .body("error_code", is("auth_failed"))
                .extract()
                .body()
                .jsonPath()
                .getList("error_messages.username", ErrorCode.class);

        assertThat(errorCodes, hasSize(1));
        assertThat(errorCodes.get(0).code, is("invalid_supplied_credentials"));
    }
}
