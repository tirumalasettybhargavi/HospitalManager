package com.hospital.hospitalmanager.model;

public class Patient extends Person {
    private int age;
    private String gender;
    private String address;
    private String medicalHistory;

    public Patient(String id, String name, String contact, int age,
                   String gender, String address, String medicalHistory) {
        super(id, name, contact);
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.medicalHistory = medicalHistory;
    }

    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public String getMedicalHistory() { return medicalHistory; }

    public void setAge(int age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setAddress(String address) { this.address = address; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    @Override
    public String getRole() { return "Patient"; }
}
