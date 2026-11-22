package com.automation.ecommerce.utils;

/** Test user details: profile data comes from testdata.json, email is unique per run. */
public record User(String name, String email, String password, String firstName, String lastName,
                   String company, String address, String country, String state, String city,
                   String zipcode, String mobile, String birthDay, String birthMonth, String birthYear) {
}
