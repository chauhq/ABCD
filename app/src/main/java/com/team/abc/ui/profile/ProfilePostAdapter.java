package com.team.abc.ui.profile;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.team.abc.R;
import com.team.abc.model.Post;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.PostViewHolder> {
    List<Post> posts;
    private int width;
    private int height;
    OnPostListener listener;

    public ProfilePostAdapter(List<Post> posts, Context context, OnPostListener listener) {
        this.posts = posts;
        this.listener = listener;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = context.getResources().getDimensionPixelSize(R.dimen.img_size_medium);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PostViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_post, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder postViewHolder, int i) {
        postViewHolder.bind(posts.get(i));

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView tvDes;
        private TextView tvPhoneNumber;
        private TextView tvAddress;
        private TextView tvCreate;
        private TextView tvPrice;
        private TextView tvEdit;
        private TextView tvDelete;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgLand);
            tvDes = itemView.findViewById(R.id.tvDes);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvCreate = itemView.findViewById(R.id.tvCreate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDelete = itemView.findViewById(R.id.tvDelete);
            tvEdit = itemView.findViewById(R.id.tvEdit);

            tvEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.editPost(posts.get(getAdapterPosition()));
                }
            });

            tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.deletePost(posts.get(getAdapterPosition()));
                }
            });
        }

        public void bind(final Post post) {
            Glide.with(itemView.getContext()).load(post.getUrl()).override(width, height).centerCrop().into(imageView);
            tvDes.setText(post.getDes());
            tvAddress.setText("State: " + post.getState());
            tvPhoneNumber.setText("Phone: " + post.getPhoneNumber());
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
            tvPrice.setText("Price: " + formatter.format(post.getPrice()) + "vnđ");
            tvCreate.setText(post.getCreate());
        }
    }
}

