package com.hv.community.backend.controller;

import com.hv.community.backend.dto.ResponseDto;
import com.hv.community.backend.dto.ResponseListDto;
import com.hv.community.backend.dto.community.CheckPostPasswordRequestDto;
import com.hv.community.backend.dto.community.CheckReplyPasswordRequestDto;
import com.hv.community.backend.dto.community.CreateCommunityRequestDto;
import com.hv.community.backend.dto.community.CreatePostRequestDto;
import com.hv.community.backend.dto.community.CreateReplyRequestDto;
import com.hv.community.backend.dto.community.DeletePostRequestDto;
import com.hv.community.backend.dto.community.DeleteReplyRequestDto;
import com.hv.community.backend.dto.community.GetPostDetailResponseDto;
import com.hv.community.backend.dto.community.UpdatePostRequestDto;
import com.hv.community.backend.dto.community.UpdateReplyRequestDto;
import com.hv.community.backend.service.community.CommunityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

  // POST createCommunity
  @PostMapping("/create-community")
  public ResponseEntity createCommunity(
      @RequestBody CreateCommunityRequestDto createCommunityRequestDto) {
    try {
      communityService.createCommunity(createCommunityRequestDto);
      return ResponseEntity.ok()
          .body(ResponseDto.builder().status("200").message("CREATE_COMMUNITY_SUCCESS").build());
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("CREATE_COMMUNITY_FAIL")
              .detail(e.getMessage()).build());
    }
  }


  // GET getCommunityList
  // 게시판 리스트 조회
  // id, community, description, thumbnail반환
  @GetMapping("/get-community-list")
  public ResponseEntity getCommunityList() {
    try {
      ResponseListDto communityList = communityService.getCommunityList();
      return ResponseEntity.ok(communityList);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("GET_COMMUNITY_LIST_FAIL")
              .detail(e.getMessage()).build());
    }
  }

  // GET getPostList/{community-id}
  // 게시글 목록 조회
  // 게시글 id, title, reply갯수, 작성자

  @GetMapping("/get-post-list/{communityId}")
  public ResponseEntity getPostList(@PathVariable Long communityId) {
    try {
      ResponseListDto postList = communityService.getPostList(communityId);
      return ResponseEntity.ok(postList);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("GET_COMMUNITY_LIST_FAIL")
              .detail(e.getMessage()).build());
    }
  }

  // GET getPostDetail/{post-id}
  // 게시글 상세 조회
  // 게시글 id, title, 작성자, reply배열 반환
  @GetMapping("/get-post-detail/{postId}")
  public ResponseEntity getPostDetail(@PathVariable Long postId) {
    try {
      GetPostDetailResponseDto postDetail = communityService.getPostDetail(postId);
      return ResponseEntity.ok(postDetail);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("GET_POST_DETAIL_FAIL")
              .detail(e.getMessage())
              .build());
    }
  }

  // POST createPost
  // 게시글 작성
  // title, content, owner, code
  @PostMapping("/create-post")
  public ResponseEntity createPost(@AuthenticationPrincipal User user, @RequestBody
  CreatePostRequestDto createPostRequestDto) {
    if (createPostRequestDto.getTitle().trim().isEmpty() || createPostRequestDto.getContent().trim()
        .isEmpty()) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("CREATE_PROJECT_FAIL")
              .detail("제목 혹은 내용이 비어있습니다.").build());
    }
    String email = getEmail(user);
    try {
      communityService.createPost(email, createPostRequestDto);
      return ResponseEntity.ok()
          .body(ResponseDto.builder().status("200").message("CREATE_POST_SUCCESS").build());
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("CREATE_POST_FAIL")
              .detail(e.getMessage()).build());
    }
  }

  // POST checkPostPassword
  // 등록된 유저가 아닌 경우 글 수정전에 비밀번호 확인
  // post_id, password
  @PostMapping("/check-post-password")
  public ResponseEntity checkPostPassword(
      @RequestBody CheckPostPasswordRequestDto checkPostPasswordRequestDto) {
    if (checkPostPasswordRequestDto.getPassword().trim().isEmpty()) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("CHECK_POST_PASSWORD_FAIL")
              .detail("비밀번호가 비어있습니다.").build());
    }
    try {
      if (communityService.checkPostPassword(checkPostPasswordRequestDto)) {
        return ResponseEntity.ok()
            .body(
                ResponseDto.builder().status("200").message("CHECK_POST_PASSWORD_SUCCESS").build());
      } else {
        return ResponseEntity.ok()
            .body(ResponseDto.builder().status("400").message("CHECK_POST_PASSWORD_FAIL").build());
      }
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("CHECK_POST_PASSWORD_FAIL")
              .detail(e.getMessage()).build());
    }
  }

  // POST updatePost
  // 게시글 수정
  // title, content, owner, code
  @PostMapping("/update-post")
  public ResponseEntity updatePost(@AuthenticationPrincipal User user, @RequestBody
  UpdatePostRequestDto updatePostRequestDto) {
    if (updatePostRequestDto.getTitle().trim().isEmpty() || updatePostRequestDto.getContent().trim()
        .isEmpty()) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("UPDATE_POST_FAIL")
              .detail("제목 혹은 내용이 비어있습니다.").build());
    }
    String email = getEmail(user);
    try {
      communityService.updatePost(email, updatePostRequestDto);
      return ResponseEntity.ok()
          .body(ResponseDto.builder().status("200").message("UPDATE_POST_SUCCESS").build());
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("UPDATE_POST_FAIL")
              .detail(e.getMessage()).build());
    }
  }

  // DELETE deletePost
  // 게시글 삭제
  // accessToken, code
  @DeleteMapping("/delete-post")
  public ResponseEntity deletePost(@AuthenticationPrincipal User user, @RequestBody
  DeletePostRequestDto deletePostRequestDto) {
    String email = getEmail(user);
    try {
      communityService.deletePost(email, deletePostRequestDto);
      return ResponseEntity.ok()
          .body(ResponseDto.builder().status("200").message("DELETE_POST_SUCCESS").build());
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("DELETE_POST_FAIL")
              .detail(e.getMessage()).build());
    }
  }

  // POST createReply
  // 댓글 작성
  // reply, owner, code
  @PostMapping("/create-reply")
  public ResponseEntity createReply(@AuthenticationPrincipal User user, @RequestBody
  CreateReplyRequestDto createReplyRequestDto) {
    if (createReplyRequestDto.getReply().trim().isEmpty()) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("CREATE_REPLY_FAIL")
              .detail("댓글이 비어있습니다.").build());
    }
    String email = getEmail(user);
    try {
      communityService.createReply(email, createReplyRequestDto);
      return ResponseEntity.ok()
          .body(ResponseDto.builder().status("200").message("CREATE_REPLY_SUCCESS").build());
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("CREATE_REPLY_FAIL")
              .detail(e.getMessage()).build());
    }

  }

  // POST checkReplyPassword
  // 등록된 유저가 아닌 경우 댓글 수정전에 비밀번호 확인
  // reply_id, password
  @PostMapping("/check-reply-password")
  public ResponseEntity checkReplyPassword(
      @RequestBody CheckReplyPasswordRequestDto checkReplyPasswordRequestDto) {
    if (checkReplyPasswordRequestDto.getPassword().trim().isEmpty()) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("CHECK_REPLY_PASSWORD_FAIL")
              .detail("비밀번호가 비어있습니다.").build());
    }
    try {
      if (communityService.checkReplyPassword(checkReplyPasswordRequestDto)) {
        return ResponseEntity.ok()
            .body(
                ResponseDto.builder().status("200").message("CHECK_REPLY_PASSWORD_SUCCESS")
                    .build());
      } else {
        return ResponseEntity.ok()
            .body(ResponseDto.builder().status("400").message("CHECK_REPLY_PASSWORD_FAIL").build());
      }
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("CHECK_REPLY_PASSWORD_FAIL")
              .detail(e.getMessage()).build());
    }
  }

  // POST updateReply
  // 댓글 수정
  // reply, owner, code
  @PostMapping("/update-reply")
  public ResponseEntity updateReply(@AuthenticationPrincipal User user, @RequestBody
  UpdateReplyRequestDto updateReplyRequestDto) {
    if (updateReplyRequestDto.getReply().trim().isEmpty()) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("UPDATE_REPLY_FAIL")
              .detail("댓글이 비어있습니다.").build());
    }
    String email = getEmail(user);
    try {
      communityService.updateReply(email, updateReplyRequestDto);
      return ResponseEntity.ok()
          .body(ResponseDto.builder().status("200").message("UPDATE_REPLY_SUCCESS").build());
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("UPDATE_REPLY_FAIL")
              .detail(e.getMessage()).build());
    }
  }

  // DELETE deleteReply
  // 댓글 삭제
  // accessToken, code
  @DeleteMapping("/delete-reply")
  public ResponseEntity deleteReply(@AuthenticationPrincipal User user, @RequestBody
  DeleteReplyRequestDto deleteReplyRequestDto) {
    String email = getEmail(user);
    try {
      communityService.deleteReply(email, deleteReplyRequestDto);
      return ResponseEntity.ok()
          .body(ResponseDto.builder().status("200").message("DELETE_REPLY_SUCCESS").build());
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
          ResponseDto.builder().status("400").message("DELETE_REPLY_FAIL")
              .detail(e.getMessage()).build());
    }
  }


  private String getEmail(User user) {
    if (user == null) {
      return "";
    }
    return user.getUsername();
  }
}
