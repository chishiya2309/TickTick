package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.ContactModel;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements Filterable {

    private List<ContactModel> contactList;
    private List<ContactModel> contactListFull;
    private OnContactActionListener listener;

    public interface OnContactActionListener {
        void onCallClick(ContactModel contact);
        void onSmsClick(ContactModel contact);
        void onAddTaskClick(ContactModel contact);
    }

    public ContactAdapter(List<ContactModel> contactList, OnContactActionListener listener) {
        this.contactList = contactList;
        this.contactListFull = new ArrayList<>(contactList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ContactModel contact = contactList.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhoneNumber());

        if (contact.getPhotoUri() != null) {
            holder.imgAvatar.setImageURI(Uri.parse(contact.getPhotoUri()));
        } else {
            holder.imgAvatar.setImageResource(R.drawable.avatar);
        }

        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) listener.onCallClick(contact);
        });

        holder.btnSms.setOnClickListener(v -> {
            if (listener != null) listener.onSmsClick(contact);
        });

        holder.btnAddTask.setOnClickListener(v -> {
            if (listener != null) listener.onAddTaskClick(contact);
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    private final Filter contactFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ContactModel> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(contactListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ContactModel item : contactListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) ||
                            item.getPhoneNumber().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactList.clear();
            contactList.addAll((List<ContactModel>) results.values);
            notifyDataSetChanged();
        }
    };

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvPhone;
        ImageButton btnCall, btnSms, btnAddTask;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvPhone = itemView.findViewById(R.id.tvPhoneNumber);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnSms = itemView.findViewById(R.id.btnSms);
            btnAddTask = itemView.findViewById(R.id.btnAddNote);
        }
    }
}
