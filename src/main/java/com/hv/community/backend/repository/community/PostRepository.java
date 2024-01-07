package com.hv.community.backend.repository.community;

import com.hv.community.backend.domain.community.Community;
import com.hv.community.backend.domain.community.Post;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

  Page<Post> findByCommunity(Community community, Pageable pageable);

  Optional<Post> findTopByCommunityIdAndIdLessThanOrderByIdDesc(Long communityId, Long postId);

  Optional<Post> findTopByCommunityIdAndIdGreaterThanOrderByIdAsc(Long communityId, Long postId);

}
