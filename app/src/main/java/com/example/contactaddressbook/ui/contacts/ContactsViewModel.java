package com.example.contactaddressbook.ui.contacts;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.contactaddressbook.model.Contact;
import com.example.contactaddressbook.model.ContactSection;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ContactsViewModel extends ViewModel {

    private final FirebaseFirestore db;

    private final String TAG = "ContactsViewModel";
    private final ArrayList<Contact> contacts = new ArrayList<>();
    private final ArrayList<ContactSection> sectionList = new ArrayList<>();
    private final MutableLiveData<ArrayList<ContactSection>> sectionListLive = new MutableLiveData<>();

    public ContactsViewModel() {
        db = FirebaseFirestore.getInstance();
        retrieveContacts();
    }

    /**
     * Initialise the list of ContactSection objects made up of alphabetically
     * sorted contacts categorised by the first letter of their last name.
     */
    private void initialiseSectionList() {
         HashMap<String, ArrayList<Contact>> sectionsHash = new HashMap<>();

        // add section to hash map and add initialise empty array
        for (Contact contact : contacts) {
            String sectionTitle = contact.getLastName().substring(0, 1).toUpperCase();
            ArrayList<Contact> sectionContacts = new ArrayList<>();
            sectionsHash.put(sectionTitle, sectionContacts);
        }

        // created a new sorted map out of the old one
        Map<String, ArrayList<Contact>> sortedMap = new TreeMap<>(sectionsHash);

        // fill up arrays in hash map with users
        for (Contact contact : contacts) {
            String sectionTitle = contact.getLastName().substring(0, 1).toUpperCase();
            ArrayList<Contact> sectionContacts =  sortedMap.get(sectionTitle);
            sectionContacts.add(contact);
            sortedMap.put(sectionTitle, sectionContacts);
        }

        sortedMap.forEach((k, v) -> Collections.sort(v));

        // update the sectionList
        sortedMap.forEach((k, v) -> sectionList.add(new ContactSection(k, v)));
    }


    /**
     * Populate the ArrayList of contacts
     */
    private void retrieveContacts() {
        db.collection("Contacts").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.d(TAG, "error retrieving contacts" + error.getMessage());
            }

            for (DocumentChange doc: value.getDocumentChanges()) {
                if (doc.getType() == DocumentChange.Type.ADDED) {
                    Contact user = doc.getDocument().toObject(Contact.class);
                    Log.d(TAG, "user: " + user.getFirstName());
                    contacts.add(user);
                }
            }
            initialiseSectionList();
            sectionListLive.setValue(sectionList);
        });
    }


    public LiveData<ArrayList<ContactSection>> getUserSections() {
        return sectionListLive;
    }
}