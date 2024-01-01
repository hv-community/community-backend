package com.hv.community.backend.repository.community;

import com.hv.community.backend.domain.community.Post;
import com.hv.community.backend.domain.community.Reply;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

  List<Reply> findByPost(Post post);
}
