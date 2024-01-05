package com.hv.community.backend.controller;

import com.hv.community.backend.dto.community.CheckPostPasswordRequestDto;
import com.hv.community.backend.dto.community.CheckReplyPasswordRequestDto;
import com.hv.community.backend.dto.community.CreatePostRequestDto;
import com.hv.community.backend.dto.community.CreateReplyRequestDto;
import com.hv.community.backend.dto.community.GetPostDetailResponseDto;
import com.hv.community.backend.dto.community.UpdatePostRequestDto;
import com.hv.community.backend.dto.community.UpdateReplyRequestDto;
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

  // POST createCommunity

  // GET getCommunityList
  // 게시판 리스트 조회
  // id, community, description, thumbnail반환

  // GET getPostList/{community-id}
  // 게시글 목록 조회
  // 게시글 id, title, reply갯수, 작성자

  // GET getPostDetail/{post-id}
  // 게시글 상세 조회
  // 게시글 id, title, 작성자, reply배열 반환

  // POST createPost
  // 게시글 작성
  // title, content, owner, code

  // POST checkPostPassword
  // 등록된 유저가 아닌 경우 글 수정전에 비밀번호 확인
  // post_id, password

  // POST updatePost
  // 게시글 수정
  // title, content, owner, code

  // DELETE deletePost
  // 게시글 삭제
  // accessToken, code

  // POST createReply
  // 댓글 작성
  // reply, owner, code

  // POST checkReplyPassword
  // 등록된 유저가 아닌 경우 댓글 수정전에 비밀번호 확인
  // reply_id, password

  // POST updateReply
  // 댓글 수정
  // reply, owner, code

  // DELETE deleteReply
  // 댓글 삭제
  // accessToken, code

  // GET getCommunityList
  // 게시판 리스트 조회
  // id, community, description, thumbnail반환
  @GetMapping("/v1/get-community-list")
  public ResponseEntity getCommunityListV1(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int page_size) {
    Map<String, Object> communityList = communityService.getCommunityListV1(page, page_size);
    return ResponseEntity.ok(communityList);
  }

  // GET getPostList/{community-id}
  // 게시글 목록 조회
  // 게시글 id, title, reply갯수, 작성자
  @GetMapping("/v1/get-post-list/{id}")
  public ResponseEntity getPostListV1(@PathVariable Long id,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int page_size) {
    Map<String, Object> postList = communityService.getPostListV1(id, page, page_size);
    return ResponseEntity.ok(postList);
  }

  // GET getPostDetail/{post-id}
  // 게시글 상세 조회
  // 게시글 id, title, 작성자, reply배열 반환
  @GetMapping("/v1/get-post-detail/{id}")
  public ResponseEntity getPostDetailV1(@PathVariable Long id,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int page_size) {
    GetPostDetailResponseDto postDetail = communityService.getPostDetailV1(id, page, page_size);
    return ResponseEntity.ok(postDetail);
  }

  // POST createPost
  // 게시글 작성
  // title, content, owner, code
  @PostMapping("/v1/create-post")
  public ResponseEntity createPostV1(@AuthenticationPrincipal User user,
      @RequestBody CreatePostRequestDto createPostRequestDto) {
    if (createPostRequestDto.getTitle().trim().isEmpty() || createPostRequestDto.getContent().trim()
        .isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_TITLE_OR_CONTENT");
    }
    String email = getEmail(user);

    Long postId = communityService.createPostV1(email, createPostRequestDto);
    Map<String, Object> response = new HashMap<>();
    response.put("id", postId);
    return ResponseEntity.ok(response);
  }

  // POST checkPostPassword
  // 등록된 유저가 아닌 경우 글 수정전에 비밀번호 확인
  // post_id, password
  @PostMapping("/v1/check-post-password")
  public ResponseEntity checkPostPasswordV1(
      @RequestBody CheckPostPasswordRequestDto checkPostPasswordRequestDto) {
    if (checkPostPasswordRequestDto.getPassword().trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
    }
    if (communityService.checkPostPasswordV1(checkPostPasswordRequestDto)) {
      return ResponseEntity.ok(new HashMap<>());
    } else {
      throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
    }
  }

  // POST updatePost
  // 게시글 수정
  // title, content, owner, code
  @PostMapping("/v1/update-post")
  public ResponseEntity updatePostV1(@AuthenticationPrincipal User user,
      @RequestBody UpdatePostRequestDto updatePostRequestDto) {
    if (updatePostRequestDto.getTitle().trim().isEmpty() || updatePostRequestDto.getContent().trim()
        .isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_TITLE_OR_CONTENT");
    }
    String email = getEmail(user);

    communityService.updatePostV1(email, updatePostRequestDto);
    return ResponseEntity.ok(new HashMap<>());
  }

  // DELETE deletePost
  // 게시글 삭제
  // accessToken, code
  @DeleteMapping("/v1/delete-post")
  public ResponseEntity deletePostV1(@AuthenticationPrincipal User user, @RequestParam Long id,
      @RequestParam String password) {
    String email = getEmail(user);

    communityService.deletePostV1(email, id, password);
    return ResponseEntity.ok(new HashMap<>());
  }

  // POST createReply
  // 댓글 작성
  // reply, owner, code
  @PostMapping("/v1/create-reply")
  public ResponseEntity createReplyV1(@AuthenticationPrincipal User user,
      @RequestBody CreateReplyRequestDto createReplyRequestDto) {
    if (createReplyRequestDto.getReply().trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_REPLY");
    }
    String email = getEmail(user);

    Long replyId = communityService.createReplyV1(email, createReplyRequestDto);
    Map<String, Object> response = new HashMap<>();
    response.put("reply_id", replyId);
    return ResponseEntity.ok(response);
  }

  // POST checkReplyPassword
  // 등록된 유저가 아닌 경우 댓글 수정전에 비밀번호 확인
  // reply_id, password
  @PostMapping("/v1/check-reply-password")
  public ResponseEntity checkReplyPasswordV1(
      @RequestBody CheckReplyPasswordRequestDto checkReplyPasswordRequestDto) {
    if (checkReplyPasswordRequestDto.getPassword().trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
    }
    if (communityService.checkReplyPasswordV1(checkReplyPasswordRequestDto)) {
      return ResponseEntity.ok(new HashMap<>());
    } else {
      throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
    }
  }

  // POST updateReply
  // 댓글 수정
  // reply, owner, code
  @PostMapping("/v1/update-reply")
  public ResponseEntity updateReplyV1(@AuthenticationPrincipal User user, @RequestBody
  UpdateReplyRequestDto updateReplyRequestDto) {
    if (updateReplyRequestDto.getReply().trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_REPLY");
    }
    String email = getEmail(user);

    communityService.updateReplyV1(email, updateReplyRequestDto);
    return ResponseEntity.ok(new HashMap<>());
  }

  // DELETE deleteReply
  // 댓글 삭제
  // accessToken, code
  @DeleteMapping("/v1/delete-reply")
  public ResponseEntity deleteReplyV1(@AuthenticationPrincipal User user,
      @RequestParam Long id,
      @RequestParam String password) {
    String email = getEmail(user);

    communityService.deleteReplyV1(email, id, password);
    return ResponseEntity.ok(new HashMap<>());
  }

  private String getEmail(User user) {
    if (user == null) {
      return "";
    }
    return user.getUsername();
  }
}
