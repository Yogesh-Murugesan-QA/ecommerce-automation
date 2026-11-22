package com.automation.ecommerce.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/** Reads values from testdata/testdata.json using paths like "product/name". */
public final class TestDataReader {

    private static final JsonNode DATA = load();

    private TestDataReader() {
    }

    private static JsonNode load() {
        String path = "testdata/testdata.json";
        try (InputStream in = TestDataReader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Test data file not found: " + path);
            }
            return new ObjectMapper().readTree(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read test data file: " + path, e);
        }
    }

    public static String get(String path) {
        JsonNode node = DATA.at("/" + path);
        if (node.isMissingNode()) {
            throw new IllegalArgumentException("No test data found at: " + path);
        }
        return node.asText();
    }

    public static int getInt(String path) {
        return Integer.parseInt(get(path));
    }

    /** New user with a unique email so tests never clash, even in parallel. */
    public static User newUser() {
        String email = "testuser_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        return new User(get("user/name"), email, get("user/password"), get("user/firstName"),
                get("user/lastName"), get("user/company"), get("user/address"), get("user/country"),
                get("user/state"), get("user/city"), get("user/zipcode"), get("user/mobile"),
                get("user/birthDay"), get("user/birthMonth"), get("user/birthYear"));
    }
}
