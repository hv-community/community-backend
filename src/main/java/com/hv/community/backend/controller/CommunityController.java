package com.hv.community.backend.controller;

import com.hv.community.backend.dto.EmptyResponseDto;
import com.hv.community.backend.dto.community.CommunityListResponseDto;
import com.hv.community.backend.dto.community.IdResponseDto;
import com.hv.community.backend.dto.community.PostCreateRequestDto;
import com.hv.community.backend.dto.community.PostDetailResponseDto;
import com.hv.community.backend.dto.community.PostListResponseDto;
import com.hv.community.backend.dto.community.PostReplyResponseDto;
import com.hv.community.backend.dto.community.PostUpdateRequestDto;
import com.hv.community.backend.dto.community.ReplyCreateRequestDto;
import com.hv.community.backend.dto.community.ReplyUpdateRequestDto;
import com.hv.community.backend.exception.CommunityException;
import com.hv.community.backend.service.community.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful response",
          content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
              schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CommunityListResponseDto.class)))
  })
  public ResponseEntity<CommunityListResponseDto> communityListV1(
      @Parameter(name = "page", description = "Page Parameter", example = "1", in = ParameterIn.QUERY)
      @RequestParam(defaultValue = "1") int page,
      @Parameter(name = "pageSize", description = "Page size Parameter", example = "5", in = ParameterIn.QUERY)
      @RequestParam(defaultValue = "10", name = "page_size") int pageSize) {
    CommunityListResponseDto communityListResponseDto = communityService.communityListV1(page,
        pageSize);
    return ResponseEntity.ok(communityListResponseDto);
  }

  @GetMapping("/v1/{community_id}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful response",
          content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
              schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostListResponseDto.class)))
  })
  public ResponseEntity<PostListResponseDto> postListV1(
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @Parameter(name = "page", description = "Page Parameter", example = "1", in = ParameterIn.QUERY)
      @RequestParam(defaultValue = "1") int page,
      @Parameter(name = "page_size", description = "Page size Parameter", example = "5", in = ParameterIn.QUERY)
      @RequestParam(defaultValue = "10", name = "page_size") int pageSize) {

    PostListResponseDto postListResponseDto = communityService.postListV1(communityId, page,
        pageSize);
    return ResponseEntity.ok(postListResponseDto);
  }

  @GetMapping("/v1/{community_id}/{post_id}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful response",
          content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
              schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostDetailResponseDto.class)))
  })
  public ResponseEntity<PostDetailResponseDto> postDetailV1(
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @Parameter(name = "post_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("post_id") Long postId) {
    PostDetailResponseDto postDetail = communityService.postDetailV1(postId);
    return ResponseEntity.ok(postDetail);
  }

  @GetMapping("/v1/{community_id}/{post_id}/reply")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful response",
          content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
              schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostReplyResponseDto.class)))
  })
  public ResponseEntity<PostReplyResponseDto> postReplyV1(
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @Parameter(name = "post_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("post_id") Long postId,
      @Parameter(name = "page", description = "Page Parameter", example = "1", in = ParameterIn.QUERY)
      @RequestParam(defaultValue = "1") int page,
      @Parameter(name = "page_size", description = "Page size Parameter", example = "5", in = ParameterIn.QUERY)
      @RequestParam(defaultValue = "10", name = "page_size") int pageSize) {
    PostReplyResponseDto postReply = communityService.postReplyV1(postId, page,
        pageSize);
    return ResponseEntity.ok(postReply);
  }

  @PostMapping("/v1/{community_id}/create")
  @Operation(security = {@SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdResponseDto.class)))
  })
  public ResponseEntity<IdResponseDto> postCreateV1(@AuthenticationPrincipal User user,
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @RequestBody PostCreateRequestDto postCreateRequestDto) {
    if (postCreateRequestDto.getTitle().trim().isEmpty() || postCreateRequestDto.getContent().trim()
        .isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_TITLE_OR_CONTENT");
    }
    String email = getEmail(user);

    IdResponseDto idResponseDto = communityService.postCreateV1(email, communityId,
        postCreateRequestDto);
    return ResponseEntity.ok(idResponseDto);
  }

  @GetMapping("/v1/{community_id}/{post_id}/check")
  @Operation(security = {@SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> postCheckPasswordV1(
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @Parameter(name = "post_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("post_id") Long postId,
      @Parameter(name = "password", description = "Password Parameter", example = "1q2w3e4r!", in = ParameterIn.QUERY)
      @RequestParam String password) {
    if (password.trim().isEmpty()) {
      throw new CommunityException(passwordInvalid);
    }
    if (communityService.postCheckPasswordV1(postId, password)) {
      EmptyResponseDto emptyResponseDto = null;
      return ResponseEntity.ok(emptyResponseDto);
    } else {
      throw new CommunityException(passwordInvalid);
    }
  }

  @PostMapping("/v1/{community_id}/{post_id}/update")
  @Operation(security = {@SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> postUpdateV1(@AuthenticationPrincipal User user,
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @Parameter(name = "post_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("post_id") Long postId,
      @RequestBody PostUpdateRequestDto postUpdateRequestDto) {
    if (postUpdateRequestDto.getTitle().trim().isEmpty() || postUpdateRequestDto.getContent().trim()
        .isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_TITLE_OR_CONTENT");
    }
    String email = getEmail(user);

    communityService.postUpdateV1(email, postId, postUpdateRequestDto);
    EmptyResponseDto emptyResponseDto = null;
    return ResponseEntity.ok(emptyResponseDto);
  }

  @DeleteMapping("/v1/{community_id}/{post_id}/delete")
  @Operation(security = {@SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> postDeleteV1(@AuthenticationPrincipal User user,
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @Parameter(name = "post_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("post_id") Long postId,
      @Parameter(name = "password", description = "Password Parameter", example = "1q2w3e4r!", in = ParameterIn.QUERY)
      @RequestParam String password) {
    String email = getEmail(user);

    communityService.postDeleteV1(email, postId, password);
    EmptyResponseDto emptyResponseDto = null;
    return ResponseEntity.ok(emptyResponseDto);
  }

  @PostMapping("/v1/{community_id}/{post_id}/create")
  @Operation(security = {@SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdResponseDto.class)))
  })
  public ResponseEntity<IdResponseDto> replyCreateV1(@AuthenticationPrincipal User user,
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @Parameter(name = "post_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("post_id") Long postId,
      @RequestBody ReplyCreateRequestDto replyCreateRequestDto) {
    if (replyCreateRequestDto.getContent().trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_REPLY");
    }
    String email = getEmail(user);

    IdResponseDto idResponseDto = communityService.replyCreateV1(email, postId, replyCreateRequestDto);
    return ResponseEntity.ok(idResponseDto);
  }

  @GetMapping("/v1/{community_id}/{post_id}/{reply_id}/check")
  @Operation(security = {@SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> replyCheckPasswordV1(
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @Parameter(name = "post_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("post_id") Long postId,
      @Parameter(name = "reply_id", description = "Reply ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("reply_id") Long replyId,
      @Parameter(name = "password", description = "Password Parameter", example = "1q2w3e4r!", in = ParameterIn.QUERY)
      @RequestParam String password) {
    if (password.trim().isEmpty()) {
      throw new CommunityException(passwordInvalid);
    }
    if (communityService.replyCheckPasswordV1(replyId, password)) {
      EmptyResponseDto emptyResponseDto = null;
      return ResponseEntity.ok(emptyResponseDto);
    } else {
      throw new CommunityException(passwordInvalid);
    }
  }

  @PostMapping("/v1/{community_id}/{post_id}/{reply_id}/update")
  @Operation(security = {@SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> replyUpdateV1(@AuthenticationPrincipal User user,
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @Parameter(name = "post_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("post_id") Long postId,
      @Parameter(name = "reply_id", description = "Reply ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("reply_id") Long replyId,
      @RequestBody ReplyUpdateRequestDto replyUpdateRequestDto) {
    if (replyUpdateRequestDto.getContent().trim().isEmpty()) {
      throw new CommunityException("COMMUNITY:EMPTY_REPLY");
    }
    String email = getEmail(user);

    communityService.replyUpdateV1(email, replyId, replyUpdateRequestDto);
    EmptyResponseDto emptyResponseDto = null;
    return ResponseEntity.ok(emptyResponseDto);
  }

  @DeleteMapping("/v1/{community_id}/{post_id}/{reply_id}/delete")
  @Operation(security = {@SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> replyDeleteV1(@AuthenticationPrincipal User user,
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("community_id") Long communityId,
      @Parameter(name = "post_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("post_id") Long postId,
      @Parameter(name = "reply_id", description = "Reply ID Parameter", example = "1", in = ParameterIn.PATH)
      @PathVariable("reply_id") Long replyId,
      @Parameter(name = "password", description = "Password Parameter", example = "1q2w3e4r!", in = ParameterIn.QUERY)
      @RequestParam String password) {
    String email = getEmail(user);

    communityService.replyDeleteV1(email, replyId, password);
    EmptyResponseDto emptyResponseDto = null;
    return ResponseEntity.ok(emptyResponseDto);
  }

  private String getEmail(User user) {
    if (user == null) {
      return "";
    }
    return user.getUsername();
  }
}
