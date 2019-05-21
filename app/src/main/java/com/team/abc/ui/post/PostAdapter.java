package com.team.abc.ui.post;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    List<Post> posts;
    boolean check ;


    public PostAdapter(List<Post> posts, Context context, boolean check) {
        this.posts = posts;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.check = check;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PostViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_posts, viewGroup, false));
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
        private ImageView imgPhone;
        private TextView tvAddress;
        private TextView tvCreate;
        private TextView tvPrice;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgLand);
            tvDes = itemView.findViewById(R.id.tvDes);
            imgPhone = itemView.findViewById(R.id.tvPhoneNumber);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvCreate = itemView.findViewById(R.id.tvCreate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        public void bind(final Post post) {
            Glide.with(itemView.getContext()).load(post.getUrl()).centerCrop().into(imageView);
            tvDes.setText(post.getDes());
            tvAddress.setText("State: " + post.getState());
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
            tvPrice.setText("Price: " + formatter.format(post.getPrice()) + "vnđ");
            tvCreate.setText(post.getCreate());
            imgPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + post.getPhoneNumber()));
                    itemView.getContext().startActivity(intent);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailPostActivity.startDetailPostActivity(itemView.getContext(), String.valueOf(post.getId()),check );
                }
            });
        }
    }
}

