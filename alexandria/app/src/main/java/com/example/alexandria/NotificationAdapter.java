package com.example.alexandria;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter {
    private ArrayList<MessageActivity.NotificationModel> data = new ArrayList<MessageActivity.NotificationModel>();
    private Context context;
    public ClickListener clickListener;

    public interface ClickListener {
        void onItemClick(int position, View v, String message, int messageType, String bookId);
    }

    public NotificationAdapter(ArrayList<MessageActivity.NotificationModel> viewModels, Context context) {
        this.context = context;
        if (viewModels != null) {
            data.addAll(viewModels);
        }
    }

    public void setOnItemClickListener(NotificationAdapter.ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void updateData(ArrayList<MessageActivity.NotificationModel> models) {
        if (models != null) {
            this.data.clear();
            this.data.addAll(models);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_message_notificationitem, parent, false);
        RecyclerView.ViewHolder viewHolder = new NotificationItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        Log.w("BINDING HOLDER:", holder.toString());
        ((NotificationAdapter.NotificationItemHolder) holder).bindData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class NotificationItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView photoView;
        private TextView messageView;

        private String message;
        private int messageType;
        private String bookId;

        public NotificationItemHolder(@NonNull final View itemView) {
            super(itemView);
            this.photoView = (ImageView) itemView.findViewById(R.id.message_notifPhoto);
            this.messageView = (TextView) itemView.findViewById(R.id.message_notifMessage);
            itemView.setOnClickListener(this);
        }

        public void bindData(MessageActivity.NotificationModel model) {
            this.messageView.setText(model.getMessage());
            this.message = model.getMessage();
            this.messageType = model.getMessageType();
            this.bookId = model.getBookId();
            //TODO: set notification image

        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v, message, messageType, bookId);
        }
    }
}
