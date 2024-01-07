package com.hv.community.backend.controller;

import com.hv.community.backend.dto.community.PostCreateRequestDto;
import com.hv.community.backend.dto.community.PostDetailResponseDto;
import com.hv.community.backend.dto.community.PostReplyResponseDto;
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
  private static String passwordInvalid = "COMMUNITY:PASSWORD_INVALID";

  public CommunityController(CommunityService communityService) {
    this.communityService = communityService;
  }

  @GetMapping("/v1/list")
  public ResponseEntity<Map<String, Object>> communityListV1(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10", name = "page_size") int pageSize) {
    Map<String, Object> communityList = communityService.communityListV1(page, pageSize);
    return ResponseEntity.ok(communityList);
  }

  @GetMapping("/v1/{community_id}")
  public ResponseEntity<Map<String, Object>> postListV1(
      @PathVariable("community_id") Long communityId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10", name = "page_size") int pageSize) {
    Map<String, Object> postList = communityService.postListV1(communityId, page, pageSize);
    return ResponseEntity.ok(postList);
  }

  @GetMapping("/v1/{community_id}/{post_id}")
  public ResponseEntity<PostDetailResponseDto> postDetailV1(
      @PathVariable("community_id") Long communityId,
      @PathVariable("post_id") Long postId) {
    PostDetailResponseDto postDetail = communityService.postDetailV1(postId);
    return ResponseEntity.ok(postDetail);
  }

  @GetMapping("/v1/{community_id}/{post_id}/reply")
  public ResponseEntity<PostReplyResponseDto> postReplyV1(
      @PathVariable("community_id") Long communityId,
      @PathVariable("post_id") Long postId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10", name = "page_size") int pageSize) {
    PostReplyResponseDto postReply = communityService.postReplyV1(postId, page,
        pageSize);
    return ResponseEntity.ok(postReply);
  }

  @PostMapping("/v1/{community_id}/create")
  public ResponseEntity<Map<String, Object>> postCreateV1(@AuthenticationPrincipal User user,
      @PathVariable("community_id") Long communityId,
      @RequestBody PostCreateRequestDto postCreateRequestDto) {
    if (postCreateRequestDto.getTitle().trim().isEmpty() || postCreateRequestDto.getContent().trim()
        .isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_TITLE_OR_CONTENT");
    }
    String email = getEmail(user);

    Long postId = communityService.postCreateV1(email, communityId, postCreateRequestDto);
    Map<String, Object> response = new HashMap<>();
    response.put("id", postId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/v1/{community_id}/{post_id}/check")
  public ResponseEntity<Map<String, Object>> postCheckPasswordV1(
      @PathVariable("community_id") Long communityId,
      @PathVariable("post_id") Long postId, @RequestParam String password) {
    if (password.trim().isEmpty()) {
      throw new CommunityException(passwordInvalid);
    }
    if (communityService.postCheckPasswordV1(postId, password)) {
      return ResponseEntity.ok(new HashMap<>());
    } else {
      throw new CommunityException(passwordInvalid);
    }
  }

  @PostMapping("/v1/{community_id}/{post_id}/update")
  public ResponseEntity<Map<String, Object>> postUpdateV1(@AuthenticationPrincipal User user,
      @PathVariable("community_id") Long communityId, @PathVariable("post_id") Long postId,
      @RequestBody PostUpdateRequestDto postUpdateRequestDto) {
    if (postUpdateRequestDto.getTitle().trim().isEmpty() || postUpdateRequestDto.getContent().trim()
        .isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_TITLE_OR_CONTENT");
    }
    String email = getEmail(user);

    communityService.postUpdateV1(email, postId, postUpdateRequestDto);
    return ResponseEntity.ok(new HashMap<>());
  }

  @DeleteMapping("/v1/{community_id}/{post_id}/delete")
  public ResponseEntity<Map<String, Object>> postDeleteV1(@AuthenticationPrincipal User user,
      @PathVariable("community_id") Long communityId, @PathVariable("post_id") Long postId,
      @RequestParam String password) {
    String email = getEmail(user);

    communityService.postDeleteV1(email, postId, password);
    return ResponseEntity.ok(new HashMap<>());
  }

  @PostMapping("/v1/{community_id}/{post_id}/create")
  public ResponseEntity<Map<String, Object>> replyCreateV1(@AuthenticationPrincipal User user,
      @PathVariable("community_id") Long communityId, @PathVariable("post_id") Long postId,
      @RequestBody ReplyCreateRequestDto replyCreateRequestDto) {
    if (replyCreateRequestDto.getContent().trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_REPLY");
    }
    String email = getEmail(user);

    Long replyId = communityService.replyCreateV1(email, postId, replyCreateRequestDto);
    Map<String, Object> response = new HashMap<>();
    response.put("id", replyId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/v1/{community_id}/{post_id}/{reply_id}/check")
  public ResponseEntity<Map<String, Object>> replyCheckPasswordV1(
      @PathVariable("community_id") Long communityId,
      @PathVariable("post_id") Long postId, @PathVariable("reply_id") Long replyId,
      @RequestParam String password) {
    if (password.trim().isEmpty()) {
      throw new CommunityException(passwordInvalid);
    }
    if (communityService.replyCheckPasswordV1(replyId, password)) {
      return ResponseEntity.ok(new HashMap<>());
    } else {
      throw new CommunityException(passwordInvalid);
    }
  }

  @PostMapping("/v1/{community_id}/{post_id}/{reply_id}/update")
  public ResponseEntity<Map<String, Object>> replyUpdateV1(@AuthenticationPrincipal User user,
      @PathVariable("community_id") Long communityId, @PathVariable("post_id") Long postId,
      @PathVariable("reply_id") Long replyId,
      @RequestBody ReplyUpdateRequestDto replyUpdateRequestDto) {
    if (replyUpdateRequestDto.getContent().trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_REPLY");
    }
    String email = getEmail(user);

    communityService.replyUpdateV1(email, replyId, replyUpdateRequestDto);
    return ResponseEntity.ok(new HashMap<>());
  }

  @DeleteMapping("/v1/{community_id}/{post_id}/{reply_id}/delete")
  public ResponseEntity<Map<String, Object>> replyDeleteV1(@AuthenticationPrincipal User user,
      @PathVariable("community_id") Long communityId,
      @PathVariable("post_id") Long postId, @PathVariable("reply_id") Long replyId,
      @RequestParam String password) {
    String email = getEmail(user);

    communityService.replyDeleteV1(email, replyId, password);
    return ResponseEntity.ok(new HashMap<>());
  }

  private String getEmail(User user) {
    if (user == null) {
      return "";
    }
    return user.getUsername();
  }
}
