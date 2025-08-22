package com.error.dhlvisitornotification;

import java.io.Serializable;

public class Visitor implements Serializable {
    private final String firstName;
    private final String lastName;
    private final String company;
    private final String phone;
    private final String email;
    private final String reason;

    public Visitor(String firstName, String lastName, String company, String phone, String email, String reason) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.phone = phone;
        this.email = email;
        this.reason = reason;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCompany() {
        return company;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getReason() {
        return reason;
    }
}