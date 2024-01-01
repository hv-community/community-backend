package com.hv.community.backend.repository.community;

import com.hv.community.backend.domain.community.Community;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {

}
