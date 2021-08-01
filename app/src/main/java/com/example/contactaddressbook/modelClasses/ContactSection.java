package com.example.contactaddressbook.modelClasses;

import java.util.ArrayList;

public class ContactSection {

    private String sectionName;
    private ArrayList<Contact> sectionItems;

    public ContactSection(String sectionName, ArrayList<Contact> sectionItems) {
        this.sectionName = sectionName;
        this.sectionItems = sectionItems;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public ArrayList<Contact> getSectionItems() {
        return sectionItems;
    }

    public void setSectionItems(ArrayList<Contact> sectionItems) {
        this.sectionItems = sectionItems;
    }

    @Override
    public String toString() {
        return "ContactSection{" +
                "sectionName='" + sectionName + '\'' +
                ", sectionItems=" + sectionItems +
                '}';
    }
}
