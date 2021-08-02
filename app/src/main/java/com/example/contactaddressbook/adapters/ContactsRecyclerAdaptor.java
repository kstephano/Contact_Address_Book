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
    int noOfContacts;

    public ContactsRecyclerAdaptor(ArrayList<ContactSection> sectionList) {
        this.sectionList = sectionList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == R.layout.section_row) {
            itemView = layoutInflater.inflate(R.layout.section_row, parent, false);
        } else {
            itemView = layoutInflater.inflate(R.layout.recycler_footer, parent, false);
        }
        return new viewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        if (position == sectionList.size()) {
            String footerText = noOfContacts + " Contacts";
            holder.footerTextView.setText(footerText);
        } else {
            ContactSection section = sectionList.get(position);
            String sectionName = section.getSectionName();
            ArrayList<Contact> items = section.getSectionItems();
            noOfContacts += items.size();

            holder.sectionNameTextView.setText(sectionName);

            ChildRecyclerAdaptor childRecyclerAdaptor = new ChildRecyclerAdaptor(items);
            holder.childRecyclerView.setAdapter(childRecyclerAdaptor);
        }
    }

    @Override
    public int getItemCount() {
        return sectionList.size() + 1;
    }

    static class viewHolder extends ViewHolder {

        TextView sectionNameTextView;
        RecyclerView childRecyclerView;
        TextView footerTextView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            sectionNameTextView = itemView.findViewById(R.id.sectionNameTextView);
            childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
            footerTextView = itemView.findViewById(R.id.text_view_footer);
        }
    }

    @Override
    public int getItemViewType(int position) {
        super.getItemViewType(position);
        return (position == sectionList.size() ? R.layout.recycler_footer : R.layout.section_row);
    }
}
