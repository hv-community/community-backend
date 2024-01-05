package com.hv.community.backend.controller;

import com.hv.community.backend.dto.community.PostCreateRequestDto;
import com.hv.community.backend.dto.community.PostDetailResponseDto;
import com.hv.community.backend.dto.community.PostUpdateRequestDto;
import com.hv.community.backend.dto.community.ReplyCreateRequestDto;
import com.hv.community.backend.dto.community.ReplyUpdateRequestDto;
import com.hv.community.backend.exception.CommunityException;
import com.hv.community.backend.service.community.CommunityService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/community")
public class CommunityController {

  private final CommunityService communityService;

  public CommunityController(CommunityService communityService) {
    this.communityService = communityService;
  }

  // GET /v1/list
  // 게시판 리스트 조회
  // page, page_size
  // id, community, description, thumbnail 반환

  // GET /v1/{community_id}/get
  // 게시글 목록 조회
  // community_id, page, page_size
  // 페이지 수, 게시글 목록 반환

  // GET /v1/{community_id}/{post_id}/get
  // 게시글 상세 조회
  // post_id, page, page_size
  // 게시글 id, title, 작성자, reply 배열 반환

  // POST /v1/{community_id}/create
  // 게시글 작성
  // accessToken, title, content, nickname, password

  // GET /v1/{community_id}/{post_id}/check?password=
  // 등록된 유저가 아닌 경우 글 수정 전에 비밀 번호 확인
  // community_id, post_id, password

  // POST /v1/{community_id}/{post_id}/update
  // 게시글 수정
  // accessToken, community_id, post_id, title, content, password

  // DELETE /v1/{community_id}/{post_id}/delete?password=
  // 게시글 삭제
  // accessToken, community_id, post_id, password

  // POST /v1/{community_id}/{post_id}/create
  // 댓글 작성
  // accessToken, community_id, post_id, reply, nickname, password

  // GET /v1/{community_id}/{post_id}/{reply_id}/check?password=
  // 등록된 유저가 아닌 경우 댓글 수정 전에 비밀 번호 확인
  // community_id, post_id, reply_id, password

  // POST /v1/{community_id}/{post_id}/{reply_id}/update
  // 댓글 수정
  // accessToken, community_id, post_id, reply_id, reply, password

  // DELETE /v1/{community_id}/{post_id}/{reply_id}/delete?password=
  // 댓글 삭제
  // accessToken, community_id, post_id, reply_id, password


  // GET /v1/list
  // 게시판 리스트 조회
  // page, page_size
  // id, community, description, thumbnail 반환
  @GetMapping("/v1/list")
  public ResponseEntity communityListV1(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int page_size) {
    Map<String, Object> communityList = communityService.communityListV1(page, page_size);
    return ResponseEntity.ok(communityList);
  }

  // GET /v1/{community_id}/get
  // 게시글 목록 조회
  // community_id, page, page_size
  // 페이지 수, 게시글 목록 반환
  @GetMapping("/v1/{community_id}/get")
  public ResponseEntity postListV1(@PathVariable Long community_id,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int page_size) {
    Map<String, Object> postList = communityService.postListV1(community_id, page, page_size);
    return ResponseEntity.ok(postList);
  }

  // GET /v1/{community_id}/{post_id}/get
  // 게시글 상세 조회
  // post_id, page, page_size
  // 게시글 id, title, 작성자, reply 배열 반환
  @GetMapping("/v1/{community_id}/{post_id}/get")
  public ResponseEntity postDetailV1(@PathVariable Long community_id, @PathVariable Long post_id,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int page_size) {
    PostDetailResponseDto postDetail = communityService.postDetailV1(post_id, page,
        page_size);
    return ResponseEntity.ok(postDetail);
  }

  // POST /v1/{community_id}/create
  // 게시글 작성
  // accessToken, title, content, nickname, password
  @PostMapping("/v1/{community_id}/create")
  public ResponseEntity postCreateV1(@AuthenticationPrincipal User user,
      @PathVariable Long community_id, @RequestBody PostCreateRequestDto postCreateRequestDto) {
    if (postCreateRequestDto.getTitle().trim().isEmpty() || postCreateRequestDto.getContent().trim()
        .isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_TITLE_OR_CONTENT");
    }
    String email = getEmail(user);

    Long postId = communityService.postCreateV1(email, community_id, postCreateRequestDto);
    Map<String, Object> response = new HashMap<>();
    response.put("id", postId);
    return ResponseEntity.ok(response);
  }

  // GET /v1/{community_id}/{post_id}/check?password=
  // 등록된 유저가 아닌 경우 글 수정 전에 비밀 번호 확인
  // community_id, post_id, password
  @GetMapping("/v1/{community_id}/{post_id}/check")
  public ResponseEntity postCheckPasswordV1(@PathVariable Long community_id,
      @PathVariable Long post_id, @RequestParam String password) {
    if (password.trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
    }
    if (communityService.postCheckPasswordV1(post_id, password)) {
      return ResponseEntity.ok(new HashMap<>());
    } else {
      throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
    }
  }

  // POST /v1/{community_id}/{post_id}/update
  // 게시글 수정
  // accessToken, community_id, post_id, title, content, password
  @PostMapping("/v1/{community_id}/{post_id}/update")
  public ResponseEntity postUpdateV1(@AuthenticationPrincipal User user,
      @PathVariable Long community_id, @PathVariable Long post_id,
      @RequestBody PostUpdateRequestDto postUpdateRequestDto) {
    if (postUpdateRequestDto.getTitle().trim().isEmpty() || postUpdateRequestDto.getContent().trim()
        .isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_TITLE_OR_CONTENT");
    }
    String email = getEmail(user);

    communityService.postUpdateV1(email, post_id, postUpdateRequestDto);
    return ResponseEntity.ok(new HashMap<>());
  }

  // DELETE /v1/{community_id}/{post_id}/delete?password=
  // 게시글 삭제
  // accessToken, community_id, post_id, password
  @DeleteMapping("/v1/{community_id}/{post_id}/delete")
  public ResponseEntity postDeleteV1(@AuthenticationPrincipal User user,
      @PathVariable Long community_id, @PathVariable Long post_id,
      @RequestParam String password) {
    String email = getEmail(user);

    communityService.postDeleteV1(email, post_id, password);
    return ResponseEntity.ok(new HashMap<>());
  }

  // POST /v1/{community_id}/{post_id}/create
  // 댓글 작성
  // accessToken, community_id, post_id, reply, nickname, password
  @PostMapping("/v1/{community_id}/{post_id}/create")
  public ResponseEntity replyCreateV1(@AuthenticationPrincipal User user,
      @PathVariable Long community_id, @PathVariable Long post_id,
      @RequestBody ReplyCreateRequestDto replyCreateRequestDto) {
    if (replyCreateRequestDto.getReply().trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_REPLY");
    }
    String email = getEmail(user);

    Long replyId = communityService.replyCreateV1(email, post_id, replyCreateRequestDto);
    Map<String, Object> response = new HashMap<>();
    response.put("id", replyId);
    return ResponseEntity.ok(response);
  }

  // GET /v1/{community_id}/{post_id}/{reply_id}/check?password=
  // 등록된 유저가 아닌 경우 댓글 수정 전에 비밀 번호 확인
  // community_id, post_id, reply_id, password
  @GetMapping("/v1/{community_id}/{post_id}/{reply_id}/check")
  public ResponseEntity replyCheckPasswordV1(@PathVariable Long community_id,
      @PathVariable Long post_id, @PathVariable Long reply_id, @RequestParam String password) {
    if (password.trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
    }
    if (communityService.replyCheckPasswordV1(reply_id, password)) {
      return ResponseEntity.ok(new HashMap<>());
    } else {
      throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
    }
  }

  // POST /v1/{community_id}/{post_id}/{reply_id}/update
  // 댓글 수정
  // accessToken, community_id, post_id, reply_id, reply, password
  @PostMapping("/v1/{community_id}/{post_id}/{reply_id}/update")
  public ResponseEntity replyUpdateV1(@AuthenticationPrincipal User user,
      @PathVariable Long community_id, @PathVariable Long post_id, @PathVariable Long reply_id,
      @RequestBody ReplyUpdateRequestDto replyUpdateRequestDto) {
    if (replyUpdateRequestDto.getReply().trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_REPLY");
    }
    String email = getEmail(user);

    communityService.replyUpdateV1(email, reply_id, replyUpdateRequestDto);
    return ResponseEntity.ok(new HashMap<>());
  }

  // DELETE /v1/{community_id}/{post_id}/{reply_id}/delete?password=
  // 댓글 삭제
  // accessToken, community_id, post_id, reply_id, password
  @DeleteMapping("/v1/{community_id}/{post_id}/{reply_id}/delete")
  public ResponseEntity replyDeleteV1(@AuthenticationPrincipal User user,
      @PathVariable Long community_id, @PathVariable Long post_id, @PathVariable Long reply_id,
      @RequestParam String password) {
    String email = getEmail(user);

    communityService.replyDeleteV1(email, reply_id, password);
    return ResponseEntity.ok(new HashMap<>());
  }

  private String getEmail(User user) {
    if (user == null) {
      return "";
    }
    return user.getUsername();
  }
}
