package com.example.alexandria;
/**
 * The adapter used for the SearchActivity to manage the different kinds of results
 * @author Kyla Wong, ktwong@ualberta.ca
 */

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import javax.xml.transform.Result;


public class SearchAdapter extends RecyclerView.Adapter {
    private ArrayList<ResultModel> resultModels = new ArrayList<ResultModel>();
    private static ClickListener clickListener;

    /**
     * Constructor of the SearchAdapter
     * @param viewModels an ArrayList or ResultModels to be displayed
     */
    public SearchAdapter(ArrayList<ResultModel> viewModels) {
        if (viewModels != null) {
            this.resultModels.addAll(viewModels);
        }
    }

    /**
     * Instantiates the correct ViewHolder depending on the type of model
     * @param parent parent of ViewHolder
     * @param viewType layout id of the model in question
     * @return returns the created ViewHolder
     */
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

    /**
     * Binds the correct data to the ViewHolder depending on the type
     * @param holder the ViewHolder to be bound
     * @param position the position of the ViewHolder in the RecyclerView
     */
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

    /**
     * Gets the number of items in the list
     * @return number of items
     */
    @Override
    public int getItemCount() {
        return resultModels.size();
    }

    /**
     * Gets the view type of the model at the given position
     * @param position position in resultModels list
     * @return the layout id of the model
     */
    @Override
    public int getItemViewType(int position) {
        return resultModels.get(position).getViewType();
    }

    /**
     * updates the resultModels list
     * @param models the new list of models
     */
    public void updateData(ArrayList<ResultModel> models) {
        if (models != null) {
            this.resultModels.clear();
            this.resultModels.addAll(models);
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the clickListener to be used when an item is clicked
     * @param clickListener clickListener to be set
     */
    public void setOnItemClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * Interface for classes to use to implement a ClickListener and define the behaviour when that item is clicked
     */
    public interface ClickListener {
        void onItemClick(int position, View v, String info);
    }

    /** ViewHolder for book items
     * @author Kyla Wong, ktwong@ualberta.ca
     */
    class SearchBookItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView photoView;
        private TextView titleView;
        private TextView authorsView;
        private TextView ownerView;
        private TextView publicStatusView;
        private String bookID;

        /**
         * Constructor of SearchBookItemHolder
         * @param itemView the view the holder will be bound to
         */
        public SearchBookItemHolder(@NonNull final View itemView) {
            super(itemView);
            photoView = (ImageView) itemView.findViewById(R.id.search_bookitem_photo);
            titleView = (TextView) itemView.findViewById(R.id.search_bookitem_title);
            authorsView = (TextView) itemView.findViewById(R.id.search_bookitem_author);
            ownerView = (TextView) itemView.findViewById(R.id.search_bookitem_owner);
            publicStatusView = (TextView) itemView.findViewById(R.id.search_bookitem_status);
            itemView.setOnClickListener(this);
        }

        /**
         * Binds data to ViewHolder
         * @param viewModel the model to get the data from
         */
        public void bindData(final ResultModel.SearchBookItemModel viewModel) {
            Log.d("BINDING BOOK", viewModel.getTitle());
            //TODO: bind the image data!
            this.titleView.setText(viewModel.getTitle());
            this.authorsView.setText("By: " + TextUtils.join(", ", viewModel.getAuthors()));
            this.ownerView.setText("@" + viewModel.getOwner());
            this.publicStatusView.setText(viewModel.getPublicStatus());
            this.bookID = viewModel.getBookId();
        }

        /**
         * Pass the viewHolder's position, view, and bookID when clicked to the adapter to handle
         * @param v view that was clicked
         */
        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v, bookID);
        }
    }

    /**
     * ViewHolder for user items
     * @author Kyla Wong, ktwong@ualberta.ca
     */
    class SearchUserItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView usernameView;
        private TextView bioView;
        private String username;

        /**
         * Constructor of SearchUserItemHolder
         * @param itemView the view the holder will be bound to
         */
        public SearchUserItemHolder(@NonNull final View itemView) {
            super(itemView);
            usernameView = (TextView) itemView.findViewById(R.id.search_useritem_username);
            bioView = (TextView) itemView.findViewById(R.id.search_useritem_bio);
            itemView.setOnClickListener(this);
        }

        /**
         * Binds data to ViewHolder
         * @param viewModel the model to get the data from
         */
        public void bindData(final ResultModel.SearchUserItemModel viewModel) {
            Log.d("BINDING USER", viewModel.getUsername());
            this.usernameView.setText(viewModel.getUsername());
            this.bioView.setText(viewModel.getBio());
            this.username = viewModel.getUsername();
        }

        /**
         * Pass the viewHolder's position, view, and username when clicked to the adapter to handle
         * @param v view that was clicked
         */
        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v, username);
        }
    }

}
