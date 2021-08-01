package com.example.contactaddressbook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.itemTextView.setText(
                contacts.get(position).getFirstName() + " " + contacts.get(position).getLastName());
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
