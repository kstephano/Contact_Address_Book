package com.example.contactaddressbook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.example.contactaddressbook.R;
import com.example.contactaddressbook.model.ContactSection;
import com.example.contactaddressbook.model.Contact;

import java.util.ArrayList;

public class ContactsRecyclerAdaptor extends RecyclerView.Adapter<ContactsRecyclerAdaptor.viewHolder> {

    private final ArrayList<ContactSection> sectionList;
    private int noOfContacts;

    public ContactsRecyclerAdaptor(ArrayList<ContactSection> sectionList) {
        this.sectionList = sectionList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // inflate the section row
        if (viewType == R.layout.section_row) {
            itemView = layoutInflater.inflate(R.layout.section_row, parent, false);
        } else {
            // inflate the footer if all sections have been displayed
            itemView = layoutInflater.inflate(R.layout.recycler_footer, parent, false);
        }
        return new viewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        // if view is last in the list, set the footer variables
        if (position == sectionList.size()) {
            String footerText = noOfContacts + " Contacts";
            holder.footerTextView.setText(footerText);
        } else {
            // set the variables for the section title and its contents
            ContactSection section = sectionList.get(position);
            String sectionName = section.getSectionName();
            ArrayList<Contact> items = section.getSectionItems();
            noOfContacts += items.size();
            holder.sectionNameTextView.setText(sectionName);
            // create and initialise the child recycler view of contacts for the section
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
        // return section row layout or the recycler footer depending on given position
        return (position == sectionList.size() ? R.layout.recycler_footer : R.layout.section_row);
    }
}
