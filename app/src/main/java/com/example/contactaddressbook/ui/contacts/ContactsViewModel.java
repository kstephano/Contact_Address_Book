package com.example.contactaddressbook.ui.contacts;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.contactaddressbook.modelClasses.ContactSection;
import com.example.contactaddressbook.modelClasses.User;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ContactsViewModel extends ViewModel {

    private FirebaseFirestore db;

    private String TAG = "ContactsViewModel";
    private ArrayList<User> users = new ArrayList();
    private ArrayList sectionList = new ArrayList<ContactSection>();
    private MutableLiveData<String> mText;
    private MutableLiveData<ArrayList<ContactSection>> sectionListLive = new MutableLiveData<>();

    public ContactsViewModel() {
        initData();
        initialiseSectionList();
        sectionListLive.setValue(sectionList);

        //ContactSection contactSection = new ContactSection(sectionList.get(0));
    }

    private void initData() {
        User user1 = new User(123, "bob", "dylan", "01/04/91", "10 Pine Road", "London", "LD01 101 ES", "bdylan@gmail.com");
        User user2 = new User(123, "doss", "dylan", "01/04/91", "10 Pine Road", "London", "LD01 101 ES", "bdylan@gmail.com");
        User user3 = new User(123, "zoss", "dylan", "01/04/91", "10 Pine Road", "London", "LD01 101 ES", "bdylan@gmail.com");
        User user4 = new User(123, "ryan", "rylan", "01/04/91", "10 Pine Road", "London", "LD01 101 ES", "bdylan@gmail.com");
        User user5 = new User(123, "alice", "dylan", "01/04/91", "10 Pine Road", "London", "LD01 101 ES", "bdylan@gmail.com");
        User user6 = new User(123, "chris", "dylan", "01/04/91", "10 Pine Road", "London", "LD01 101 ES", "bdylan@gmail.com");

        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);
        users.add(user5);
        users.add(user6);
    }

    private void initialiseSectionList() {
         HashMap<String, ArrayList<User>> sectionsHash = new HashMap<>();

        // add section to hash map and add initialise empty array
        for (User user: users) {
            String sectionTitle = user.getLastName().substring(0, 1).toUpperCase();
            ArrayList<User> sectionUsers = new ArrayList();
            sectionsHash.put(sectionTitle, sectionUsers);
        }

        // fill up arrays in hash map with users
        for (User user: users) {
            String sectionTitle = user.getLastName().substring(0, 1).toUpperCase();
            ArrayList<User> sectionUsers =  sectionsHash.get(sectionTitle);
            sectionUsers.add(user);
            sectionsHash.put(sectionTitle, sectionUsers);
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
        db.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("contacts", "error retrieving users" + error.getMessage());
                }

                for (DocumentChange doc: value.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        User user = doc.getDocument().toObject(User.class);
                        users.add(user);
                    }
                }
            }
        });
    }


    public LiveData<ArrayList<ContactSection>> getUserSections() {
        return sectionListLive;
    }
}