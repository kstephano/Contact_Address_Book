package com.example.contactaddressbook.adapters;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactaddressbook.R;
import com.example.contactaddressbook.model.Contact;

import java.util.ArrayList;

public class ChildRecyclerAdaptor extends RecyclerView.Adapter<ChildRecyclerAdaptor.viewHolder> {

    private final ArrayList<Contact> contacts;

    public ChildRecyclerAdaptor(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.contact_row, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        String firstName = contacts.get(position).getFirstName();
        String lastName = contacts.get(position).getLastName();
        String name = firstName + " " + lastName;
        // boundaries for characters to be made bold
        int boldCharStart = firstName.length() + 1;
        int boldCharEnd = firstName.length() + 1 + lastName.length();
        // create a new spannable string with the last name of the contact made bold
        SpannableStringBuilder str = new SpannableStringBuilder(name);
        str.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), boldCharStart, boldCharEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // set the text for the Text View.
        holder.contactTV.setText(str);
        Bundle bundle = new Bundle();
        bundle.putString("contactID", contacts.get(position).getContactID());
        bundle.putString("firstName", contacts.get(position).getFirstName());
        bundle.putString("lastName", contacts.get(position).getLastName());
        // navigate to the edit contact fragment
        holder.contactTV.setOnClickListener(v -> Navigation.findNavController(v).navigate(
                    R.id.action_navigation_contacts_to_navigation_edit_contact, bundle));
        // hide the contact divider if it is the last contact in the section
        if (position == contacts.size() - 1) {
            holder.contactDivider.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {

        TextView contactTV;
        View contactDivider;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            contactTV = itemView.findViewById(R.id.text_view_user);
            contactDivider = itemView.findViewById(R.id.divider_contact);
        }
    }
}
