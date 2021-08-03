package com.example.contactaddressbook.modelClasses;

public class Contact implements Comparable<Contact> {

    private String phone;

    private String contactID;
    private String firstName;
    private String lastName;
    private String dob;
    private String email;
    private String streetLineOne;
    private String streetLineTwo;
    private String city;
    private String postcode;
    private String profileImageURL;

    public Contact(String contactID, String phone, String firstName, String lastName, String email, String dob,
                   String streetLineOne, String streetLineTwo, String city, String postcode,
                   String profileImageURL) {
        this.phone = phone;
        this.contactID = contactID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;
        this.streetLineOne = streetLineOne;
        this.streetLineTwo = streetLineTwo;
        this.city = city;
        this.postcode = postcode;
        this.profileImageURL = profileImageURL;
    }

    public Contact() { }

    @Override
    public int compareTo(Contact otherContact) {
        String fullName = this.lastName.toLowerCase() + this.firstName.toLowerCase();
        String fullNameOther = otherContact.getLastName().toLowerCase() +
                otherContact.getFirstName().toLowerCase();
        return fullName.compareTo(fullNameOther);
    }

    public String getContactID() { return contactID; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStreetLineOne() { return streetLineOne; }

    public void setStreetLineOne(String streetLineOne) { this.streetLineOne = streetLineOne; }

    public String getStreetLineTwo() { return streetLineTwo; }

    public void setStreetLineTwo(String streetLineTwo) { this.streetLineTwo = streetLineTwo; }

    public String getProfileImageURL() { return profileImageURL; }

    public void setProfileImageURL(String profileImageURL) { this.profileImageURL = profileImageURL; }
}
