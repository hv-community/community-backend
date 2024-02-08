package com.hv.community.backend.repository.community;

import com.hv.community.backend.domain.community.Post;
import com.hv.community.backend.domain.community.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

  Page<Reply> findPageByPost(Post post, Pageable pageable);
  
  void deleteByPost(Post post);
}
