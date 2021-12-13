package com.api.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class ApiTestBase {

    final static String BASE_URI = "https://devapi.currencycloud.com/v2";

    final static String LOGIN_ENDPOINT = "/authenticate/api";
    final static String LOGOUT_ENDPOINT = "/authenticate/close_session";
    final static String BASIC_RATES_ENDPOINT = "/rates/find";
    final static String CONVERSION_ENDPOINT = "/conversions/create";
    final static String BENEFICIARIES_ENDPOINT = "/beneficiaries/create";
    final static String PAYMENTS_ENDPOINT = "/payments/create";

    final static String LOGIN_ID = "rpysmennyy@n-ix.com";
    final static String API_KEY = "9f7fdd519deee849e11e02c79ffebc01df0c1522e3cb8df5b9a68c94e0c61ebd";

    String authToken;

    void init() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = 443;
    }

    void authenticate() {
        authToken = login();
    }

    String login() {
        return given()
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
    }
}
