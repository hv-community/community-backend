package com.hv.community.backend.repository.community;

import com.hv.community.backend.domain.community.Community;
import com.hv.community.backend.domain.community.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

  Page<Post> findByCommunity(Community community, Pageable pageable);

}
