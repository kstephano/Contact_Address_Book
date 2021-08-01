package com.example.contactaddressbook.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactaddressbook.R;
import com.example.contactaddressbook.modelClasses.Contact;

import java.util.ArrayList;

public class ChildRecyclerAdaptor extends RecyclerView.Adapter<ChildRecyclerAdaptor.viewHolder> {

    ArrayList<Contact> contacts;

    public ChildRecyclerAdaptor(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.user_row, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        String firstName = contacts.get(position).getFirstName();
        String lastName = contacts.get(position).getLastName();
        String name = firstName + " " + lastName;
        holder.itemTextView.setText(name);
        Bundle bundle = new Bundle();
        bundle.putString("contactID", contacts.get(position).getContactID());
        bundle.putString("firstName", contacts.get(position).getFirstName());
        bundle.putString("lastName", contacts.get(position).getLastName());

        holder.itemTextView.setOnClickListener(v -> Navigation.findNavController(v).navigate(
                    R.id.action_navigation_contacts_to_navigation_edit_contact, bundle));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {

        TextView itemTextView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            itemTextView = itemView.findViewById(R.id.itemTextView);
        }
    }
}
