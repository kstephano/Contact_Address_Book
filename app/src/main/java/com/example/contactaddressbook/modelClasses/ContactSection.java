package com.example.contactaddressbook.modelClasses;

import java.util.List;

public class ContactSection {

    private String sectionName;
    private List<String> sectionItems;

    public ContactSection(String sectionName, List<String> sectionItems) {
        this.sectionName = sectionName;
        this.sectionItems = sectionItems;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public List<String> getSectionItems() {
        return sectionItems;
    }

    public void setSectionItems(List<String> sectionItems) {
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
