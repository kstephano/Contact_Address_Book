package com.example.contactaddressbook.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactaddressbook.R;
import com.example.contactaddressbook.adapters.ContactsRecyclerAdaptor;
import com.example.contactaddressbook.modelClasses.ContactSection;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {

    private ContactsViewModel contactsViewModel;

    private View root;
    private Toolbar toolbar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        contactsViewModel =
                new ViewModelProvider(this).get(ContactsViewModel.class);
        root = inflater.inflate(R.layout.fragment_contacts, container, false);
        toolbar = root.findViewById(R.id.toolbar_contacts);
        toolbar.setTitle(getResources().getString(R.string.title_contacts));

        contactsViewModel.getUserSections().observe(getViewLifecycleOwner(),
                new Observer<ArrayList<ContactSection>>() {
            @Override
            public void onChanged(ArrayList<ContactSection> contactSections) {
                final RecyclerView mainRecyclerView = root.findViewById(R.id.recycler_contacts);
                ContactsRecyclerAdaptor contactsRecyclerAdaptor = new ContactsRecyclerAdaptor(
                        contactSections);
                mainRecyclerView.setAdapter(contactsRecyclerAdaptor);
                mainRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            }
        });

        return root;
    }
}