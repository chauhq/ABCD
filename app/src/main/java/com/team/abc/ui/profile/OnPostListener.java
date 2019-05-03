package com.team.abc.ui.profile;

import com.team.abc.model.Post;

public interface OnPostListener {
    public void deletePost(Post post);
    public void editPost(Post post);
}
