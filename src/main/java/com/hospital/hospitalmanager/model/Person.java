package com.hospital.hospitalmanager.model;

/**
 * Base class for everyone stored in the hospital system.
 * Fields are private (encapsulation) and reached through getters and setters.
 */
public abstract class Person {
    private final String id;
    private String name;
    private String contact;

    protected Person(String id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }

    public void setName(String name) { this.name = name; }
    public void setContact(String contact) { this.contact = contact; }

    /** Each subclass says what kind of person it is (polymorphism). */
    public abstract String getRole();

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
