package com.example.contactaddressbook.ui.contacts;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.contactaddressbook.modelClasses.User;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ContactsViewModel extends ViewModel {

    private FirebaseFirestore db;

    private String TAG = "ContactsViewModel";
    private ArrayList users = new ArrayList<User>();
    private MutableLiveData<String> mText;
    private MutableLiveData<List<User>> usersLive;

    public ContactsViewModel() {
        mText.setValue("This is dashboard fragment");
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
                        usersLive.setValue(users);
                    }
                }
            }
        });
    }

    public LiveData<String> getText() {
        return mText;
    }
}