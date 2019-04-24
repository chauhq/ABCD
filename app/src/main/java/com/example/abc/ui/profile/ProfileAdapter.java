package com.example.abc.ui.profile;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.abc.R;
import com.example.abc.model.Post;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.PostViewHolder> {
    List<Post> posts;


    public ProfileAdapter(List<Post> posts) {
        this.posts = posts;
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
        private TextView tvPhone;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgLand);
            tvDes = itemView.findViewById(R.id.tvDes);
            tvPhone = itemView.findViewById(R.id.tvPhoneNumber);
        }

        public void bind(Post post) {
            Glide.with(itemView.getContext()).load(post.getUrl()).centerCrop().into(imageView);
            tvPhone.setText("Phone number: " + post.getPhoneNumber());
            tvDes.setText(post.getDes());
        }
    }
}
