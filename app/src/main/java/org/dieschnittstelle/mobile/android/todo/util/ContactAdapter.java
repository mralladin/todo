package org.dieschnittstelle.mobile.android.todo.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dieschnittstelle.mobile.android.skeleton.R;

import java.util.List;

//Dadurch werden die Button Clicks und einzelnen Listen einträge für Kontakte umgesetzt
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>{

    private List<String> contactList;
    private OnItemClickListener onItemClickListener;


    public interface OnItemClickListener {
        void onDeleteClick(int position,String contact);
        void onSendMailClick(int position,String contact);
        void onSendSMSClick(int position,String contact);
    }
    public ContactAdapter(List<String> contactList, OnItemClickListener onDeleteItemClickListener) {
        this.contactList = contactList;
        this.onItemClickListener = onDeleteItemClickListener;

    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contacts_listitem_view, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        String contact = contactList.get(position);
        holder.contactName.setText(contact);

        // Button-Klicklistener
        holder.deleteButton.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onDeleteClick(position, contact);
            }
        });
        holder.sendMailButton.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onSendMailClick(position, contact);
            }
        });
        holder.sendSMSButton.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onSendSMSClick(position, contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateContacts(List<String> updatedContacts) {
        contactList.clear();
        contactList.addAll(updatedContacts);
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactName;
        Button deleteButton;
        Button sendMailButton;
        Button sendSMSButton;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contactName);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            sendMailButton = itemView.findViewById(R.id.sendMailButton);
            sendSMSButton = itemView.findViewById(R.id.sendSMSButton);
        }
    }
}

