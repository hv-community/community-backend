package com.hv.community.backend.domain.community;

import com.hv.community.backend.repository.community.CommunityRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DataInitializer {

  private final CommunityRepository communityRepository;

  @Autowired
  public DataInitializer(CommunityRepository communityRepository) {
    this.communityRepository = communityRepository;
  }

  @PostConstruct
  public void initializeDefaultData() {
    // 데이터베이스에 이미 커뮤니티가 없는지 확인
    if (communityRepository.count() == 0) {
      // 기본 커뮤니티 엔터티를 생성하고 저장
      Community community1 = new Community();
      community1.setTitle("블루 아카이브 채널");
      community1.setDescription("\uD83C\uDF89귀여운 으헤 아저씨의 생일! (01/02) | \uD83C\uDF81새해 복 많이 받으세요");
      community1.setThumbnail(
          "https://play-lh.googleusercontent.com/xUQ8wVNuE-ZdubxWjs9K4MXTAFRDp1hpcpB8ozLUV5MK0HKzrWr12r4lJbLgiWDpDPo=w240-h480-rw");
      communityRepository.save(community1);

      Community community2 = new Community();
      community2.setTitle("트릭컬 RE:VIVE 채널");
      community2.setDescription("올해도 살아남을 트릭컬 / 새해 복 많이받는 챈럼 되기 / 신규 캐릭터 셀리네 이벤트 (~01/04)");
      community2.setThumbnail(
          "https://cdn-www.bluestacks.com/bs-images/999ff719bc4ab24360e500d4564d5f2e.png");
      communityRepository.save(community2);

      Community community3 = new Community();
      community3.setTitle("승리의 여신 니케 채널");
      community3.setDescription(
          "모더니아, 흑련(01-01~01-18) 픽업 NEW YEAR, NEW SWORD (12월28일~1월11일)이벤트진행중");
      community3.setThumbnail(
          "https://scontent-ssn1-1.xx.fbcdn.net/v/t39.30808-6/310499744_192983606524545_962030277438165984_n.jpg?_nc_cat=107&ccb=1-7&_nc_sid=efb6e6&_nc_ohc=IYcQ7XTuB9QAX-vP8iC&_nc_ht=scontent-ssn1-1.xx&oh=00_AfD7fizeNsaN-aY0ScNWwnID863d7vvQy20w2ThDYUvXsQ&oe=6598CCBB");
      communityRepository.save(community3);
    }
  }
}
