package com.example.contactaddressbook.ui.contacts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.contactaddressbook.modelClasses.ContactSection;
import com.example.contactaddressbook.modelClasses.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ContactsViewModel extends ViewModel {

    private FirebaseFirestore db;

    private String TAG = "ContactsViewModel";
    private ArrayList users = new ArrayList<User>();
    private ArrayList sectionList = new ArrayList<ContactSection>();
    private MutableLiveData<String> mText;
    private MutableLiveData<ArrayList<ContactSection>> usersLive = new MutableLiveData<>();

    public ContactsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is contacts fragment");

        initData();
        usersLive.setValue(sectionList);

        //ContactSection contactSection = new ContactSection(sectionList.get(0));
    }

    private void initData() {
        User user1 = new User(123, "bob", "dylan", "01/04/91", "10 Pine Road", "London", "LD01 101 ES", "bdylan@gmail.com");

        String section1Name = "b";
        ArrayList<User> section1Items = new ArrayList<>();
        section1Items.add(user1);
        sectionList.add(new ContactSection(section1Name, section1Items));

        usersLive.setValue(sectionList);
    }
    /*
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
                        usersLive.setValue(users);
                    }
                }
            }
        });
    }
    */

    public LiveData<ArrayList<ContactSection>> getUserSections() {
        return usersLive;
    }

    public LiveData<String> getText() {
        return mText;
    }
}