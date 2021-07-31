package com.example.contactaddressbook.modelClasses;

import java.util.List;

public class ContactSection {

    private String sectionName;
    private List<User> sectionItems;

    public ContactSection(String sectionName, List<User> sectionItems) {
        this.sectionName = sectionName;
        this.sectionItems = sectionItems;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public List<User> getSectionItems() {
        return sectionItems;
    }

    public void setSectionItems(List<User> sectionItems) {
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
