package com.example.alexandria;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class SearchAdapter extends RecyclerView.Adapter {
    private ArrayList<ResultModel> resultModels = new ArrayList<ResultModel>();

    public SearchAdapter(ArrayList<ResultModel> viewModels) {
        if (viewModels != null) {
            this.resultModels.addAll(viewModels);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        RecyclerView.ViewHolder viewHolder;
        if (viewType == R.layout.activity_search_useritem) {
            viewHolder = new SearchUserItemHolder(view);
        } else if (viewType == R.layout.activity_search_bookitem) {
            viewHolder = new SearchBookItemHolder(view);
        } else {
            throw new IllegalStateException();
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        Log.w("BINDING HOLDER:", holder.toString());
        if (viewType == R.layout.activity_search_useritem) {
            ((SearchUserItemHolder) holder).bindData((ResultModel.SearchUserItemModel) resultModels.get(position));
        } else if (viewType == R.layout.activity_search_bookitem) {
            ((SearchBookItemHolder) holder).bindData((ResultModel.SearchBookItemModel) resultModels.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return resultModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        return resultModels.get(position).getViewType();
    }

    class SearchBookItemHolder extends RecyclerView.ViewHolder {
        private ImageView photoView;
        private TextView titleView;
        private TextView authorsView;
        private TextView ownerView;
        private TextView publicStatusView;

        public SearchBookItemHolder(@NonNull final View itemView) {
            super(itemView);
            photoView = (ImageView) itemView.findViewById(R.id.search_bookitem_photo);
            titleView = (TextView) itemView.findViewById(R.id.search_bookitem_title);
            authorsView = (TextView) itemView.findViewById(R.id.search_bookitem_author);
            ownerView = (TextView) itemView.findViewById(R.id.search_bookitem_owner);
            publicStatusView = (TextView) itemView.findViewById(R.id.search_bookitem_status);
        }
        public void bindData(final ResultModel.SearchBookItemModel viewModel) {
            Log.d("BINDING BOOK", viewModel.getTitle());
            //TODO: bind the image data!
            this.titleView.setText(viewModel.getTitle());
            this.authorsView.setText(TextUtils.join(", ", viewModel.getAuthors()));
            this.ownerView.setText(viewModel.getOwner());
            this.publicStatusView.setText(viewModel.getPublicStatus());
        }
    }

    class SearchUserItemHolder extends RecyclerView.ViewHolder {
        private TextView usernameView;
        private TextView bioView;

        public SearchUserItemHolder(@NonNull final View itemView) {
            super(itemView);
            usernameView = (TextView) itemView.findViewById(R.id.search_useritem_username);
            bioView = (TextView) itemView.findViewById(R.id.search_useritem_bio);
        }
        public void bindData(final ResultModel.SearchUserItemModel viewModel) {
            Log.d("BINDING USER", viewModel.getUsername());
            this.usernameView.setText(viewModel.getUsername());
            this.bioView.setText(viewModel.getBio());
        }
    }

}
