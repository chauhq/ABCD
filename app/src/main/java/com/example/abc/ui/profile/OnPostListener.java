package com.example.abc.ui.profile;

import com.example.abc.model.Post;

public interface OnPostListener {
    public void deletePost(Post post);
    public void editPost(Post post);
}
