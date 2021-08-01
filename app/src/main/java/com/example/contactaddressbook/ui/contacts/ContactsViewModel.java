package com.example.contactaddressbook.ui.contacts;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.contactaddressbook.modelClasses.Contact;
import com.example.contactaddressbook.modelClasses.ContactSection;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ContactsViewModel extends ViewModel {

    private FirebaseFirestore db;

    private String TAG = "ContactsViewModel";
    private ArrayList<Contact> contacts = new ArrayList();
    private ArrayList sectionList = new ArrayList<ContactSection>();
    private MutableLiveData<String> mText;
    private MutableLiveData<ArrayList<ContactSection>> sectionListLive = new MutableLiveData<>();

    public ContactsViewModel() {
        initData();
        // retrieveUsers();
        initialiseSectionList();
        sectionListLive.setValue(sectionList);

        //ContactSection contactSection = new ContactSection(sectionList.get(0));
    }

    private void initData() {
        Contact contact1 = new Contact(123, "bob", "dylan", "bdylan@gmail.com", "01/04/91", "10 Pine Road", "Oak Avenue", "London", "LD01 101 ES", "null");
        Contact contact2 = new Contact(123, "doss", "dylan", "bdylan@gmail.com", "01/04/91", "10 Pine Road", "Oak Avenue","London", "LD01 101 ES", "null");
        Contact contact3 = new Contact(123, "zoss", "dylan", "bdylan@gmail.com", "01/04/91", "10 Pine Road", "Oak Avenue","London", "LD01 101 ES", "null");
        Contact contact4 = new Contact(123, "ryan", "rylan", "bdylan@gmail.com", "01/04/91", "10 Pine Road", "Oak Avenue","London", "LD01 101 ES", "null");
        Contact contact5 = new Contact(123, "alice", "dylan", "bdylan@gmail.com", "01/04/91", "10 Pine Road", "Oak Avenue","London", "LD01 101 ES", "null");
        Contact contact6 = new Contact(123, "chris", "dylan", "bdylan@gmail.com", "01/04/91", "10 Pine Road", "Oak Avenue","London", "LD01 101 ES", "null");

        contacts.add(contact1);
        contacts.add(contact2);
        contacts.add(contact3);
        contacts.add(contact4);
        contacts.add(contact5);
        contacts.add(contact6);
    }

    private void initialiseSectionList() {
         HashMap<String, ArrayList<Contact>> sectionsHash = new HashMap<>();

        // add section to hash map and add initialise empty array
        for (Contact contact : contacts) {
            String sectionTitle = contact.getLastName().substring(0, 1).toUpperCase();
            ArrayList<Contact> sectionContacts = new ArrayList();
            sectionsHash.put(sectionTitle, sectionContacts);
        }

        // fill up arrays in hash map with users
        for (Contact contact : contacts) {
            String sectionTitle = contact.getLastName().substring(0, 1).toUpperCase();
            ArrayList<Contact> sectionContacts =  sectionsHash.get(sectionTitle);
            sectionContacts.add(contact);
            sectionsHash.put(sectionTitle, sectionContacts);
        }

        sectionsHash.forEach((k, v) -> {
            Collections.sort(v);
        });

        // update the sectionList
        sectionsHash.forEach((k, v) -> {
            sectionList.add(new ContactSection(k, v));
        });

    }


    private void retrieveUsers() {
        db.collection("Contacts").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.d(TAG, "error retrieving users" + error.getMessage());
            }

            for (DocumentChange doc: value.getDocumentChanges()) {
                if (doc.getType() == DocumentChange.Type.ADDED) {
                    //User user = doc.getDocument().toObject(User.class);
                    //users.add(user);
                }
            }
        });
    }


    public LiveData<ArrayList<ContactSection>> getUserSections() {
        return sectionListLive;
    }
}