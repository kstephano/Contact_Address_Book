package com.example.contactaddressbook.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ContactSection {

    private final String sectionName;
    private final ArrayList<Contact> sectionItems;

    public ContactSection(String sectionName, ArrayList<Contact> sectionItems) {
        this.sectionName = sectionName;
        this.sectionItems = sectionItems;
    }

    public String getSectionName() {
        return sectionName;
    }

    public ArrayList<Contact> getSectionItems() {
        return sectionItems;
    }

    @NotNull
    @Override
    public String toString() {
        return "ContactSection{" +
                "sectionName='" + sectionName + '\'' +
                ", sectionItems=" + sectionItems +
                '}';
    }
}
