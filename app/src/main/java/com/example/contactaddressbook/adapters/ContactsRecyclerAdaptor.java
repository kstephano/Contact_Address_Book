package com.example.contactaddressbook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.example.contactaddressbook.R;
import com.example.contactaddressbook.modelClasses.ContactSection;
import com.example.contactaddressbook.modelClasses.Contact;

import java.util.ArrayList;

public class ContactsRecyclerAdaptor extends RecyclerView.Adapter<ContactsRecyclerAdaptor.viewHolder> {

    ArrayList<ContactSection> sectionList;

    public ContactsRecyclerAdaptor(ArrayList<ContactSection> sectionList) {
        this.sectionList = sectionList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.section_row, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        ContactSection section = sectionList.get(position);
        String sectionName = section.getSectionName();
        ArrayList<Contact> items = section.getSectionItems();

        holder.sectionNameTextView.setText(sectionName);

        ChildRecyclerAdaptor childRecyclerAdaptor = new ChildRecyclerAdaptor(items);
        holder.childRecyclerView.setAdapter(childRecyclerAdaptor);
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    class viewHolder extends ViewHolder {

        TextView sectionNameTextView;
        RecyclerView childRecyclerView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            sectionNameTextView = itemView.findViewById(R.id.sectionNameTextView);
            childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
        }
    }
}
