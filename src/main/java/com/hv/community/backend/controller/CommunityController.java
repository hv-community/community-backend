package com.hv.community.backend.controller;

import com.hv.community.backend.dto.EmptyResponseDto;
import com.hv.community.backend.dto.community.CommunityDto;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
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
@RequiredArgsConstructor
@Validated
public class CommunityController {

  private final CommunityService communityService;
  private static final String COMMUNITY_PASSWORD_INVALID = "COMMUNITY:PASSWORD_INVALID";
  private static final String PAGE_INVALID = "VALID:PAGE_INVALID";
  private static final String COMMUNITY_INVALID = "VALID:COMMUNITY_INVALID";
  private static final String POST_INVALID = "VALID:POST_INVALID";
  private static final String REPLY_INVALID = "VALID:REPLY_INVALID";
  private static final String PASSWORD_INVALID = "VALID:PASSWORD_INVALID";


  @GetMapping("/v1/list")
  @Operation(summary = "View Community List", responses = {
      @ApiResponse(responseCode = "200", description = "Successful response",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CommunityListResponseDto.class)))})
  public ResponseEntity<CommunityListResponseDto> communityListV1(
      @NotNull(message = PAGE_INVALID)
      @Min(value = 1, message = PAGE_INVALID)
      @RequestParam(defaultValue = "1")
      @Parameter(name = "page", description = "Page Parameter", example = "1", in = ParameterIn.QUERY)
      int page,
      @NotNull(message = PAGE_INVALID)
      @Min(value = 5, message = PAGE_INVALID)
      @RequestParam(defaultValue = "10", name = "page_size")
      @Parameter(name = "pageSize", description = "Page size Parameter", example = "5", in = ParameterIn.QUERY)
      int pageSize) {
    CommunityListResponseDto communityListResponseDto = communityService.communityListV1(page,
        pageSize);
    return ResponseEntity.ok(communityListResponseDto);
  }

  @GetMapping("/v1/{community_id}/detail")
  @Operation(summary = "View Community Detail", responses = {
      @ApiResponse(responseCode = "200", description = "Successful response",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CommunityDto.class)))})
  public ResponseEntity<CommunityDto> communityDetailV1(
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId) {
    CommunityDto communityDto = communityService.communityDetailV1(communityId);
    return ResponseEntity.ok(communityDto);
  }

  @GetMapping("/v1/{community_id}")
  @Operation(summary = "View Post List on Community", responses = {
      @ApiResponse(responseCode = "200", description = "Successful response",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PostListResponseDto.class)))})
  public ResponseEntity<PostListResponseDto> postListV1(
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @NotNull(message = PAGE_INVALID)
      @Min(value = 1, message = PAGE_INVALID)
      @RequestParam(defaultValue = "1")
      @Parameter(name = "page", description = "Page Parameter", example = "1", in = ParameterIn.QUERY)
      int page,
      @NotNull(message = PAGE_INVALID)
      @Min(value = 5, message = PAGE_INVALID)
      @RequestParam(defaultValue = "10", name = "page_size")
      @Parameter(name = "page_size", description = "Page size Parameter", example = "5", in = ParameterIn.QUERY)
      int pageSize) {
    PostListResponseDto postListResponseDto = communityService.postListV1(communityId, page,
        pageSize);
    return ResponseEntity.ok(postListResponseDto);
  }

  @GetMapping("/v1/{community_id}/{post_id}")
  @Operation(summary = "View Post Detail", responses = {
      @ApiResponse(responseCode = "200", description = "Successful response",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PostDetailResponseDto.class)))})
  public ResponseEntity<PostDetailResponseDto> postDetailV1(
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @NotNull(message = POST_INVALID)
      @Min(value = 1, message = POST_INVALID)
      @PathVariable("post_id")
      @Parameter(name = "post_id", description = "Post ID Parameter", example = "1", in = ParameterIn.PATH)
      Long postId) {
    PostDetailResponseDto postDetail = communityService.postDetailV1(postId);
    return ResponseEntity.ok(postDetail);
  }

  @GetMapping("/v1/{community_id}/{post_id}/reply")
  @Operation(summary = "View Reply List on Post", responses = {
      @ApiResponse(responseCode = "200", description = "Successful response",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PostReplyResponseDto.class)))})
  public ResponseEntity<PostReplyResponseDto> postReplyV1(
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @NotNull(message = POST_INVALID)
      @Min(value = 1, message = POST_INVALID)
      @PathVariable("post_id")
      @Parameter(name = "post_id", description = "Post ID Parameter", example = "1", in = ParameterIn.PATH)
      Long postId,
      @NotNull(message = PAGE_INVALID)
      @Min(value = 1, message = PAGE_INVALID)
      @RequestParam(defaultValue = "1")
      @Parameter(name = "page", description = "Page Parameter", example = "1", in = ParameterIn.QUERY)
      int page,
      @NotNull(message = PAGE_INVALID)
      @Min(value = 5, message = PAGE_INVALID)
      @RequestParam(defaultValue = "10", name = "page_size")
      @Parameter(name = "page_size", description = "Page size Parameter", example = "5", in = ParameterIn.QUERY)
      int pageSize) {
    PostReplyResponseDto postReply = communityService.postReplyV1(postId, page, pageSize);
    return ResponseEntity.ok(postReply);
  }

  @PostMapping("/v1/{community_id}/create")
  @Operation(summary = "Post Create", security = {
      @SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdResponseDto.class)))
  })
  public ResponseEntity<IdResponseDto> postCreateV1(@AuthenticationPrincipal User user,
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @RequestBody @Valid
      PostCreateRequestDto postCreateRequestDto) {
    String email = getEmail(user);

    IdResponseDto idResponseDto = communityService.postCreateV1(email, communityId,
        postCreateRequestDto);
    return ResponseEntity.ok(idResponseDto);
  }

  @GetMapping("/v1/{community_id}/{post_id}/check")
  @Operation(summary = "Post Check Password", security = {
      @SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> postCheckPasswordV1(
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @NotNull(message = POST_INVALID)
      @Min(value = 1, message = POST_INVALID)
      @PathVariable("post_id")
      @Parameter(name = "post_id", description = "Post ID Parameter", example = "1", in = ParameterIn.PATH)
      Long postId,
      @NotNull(message = PASSWORD_INVALID)
      @Parameter(name = "password", description = "Password Parameter", example = "1q2w3e4r!", in = ParameterIn.QUERY)
      @RequestParam
      String password) {
    if (communityService.postCheckPasswordV1(null, postId, password)) {
      return ResponseEntity.ok(null);
    }
    throw new CommunityException(COMMUNITY_PASSWORD_INVALID);
  }

  @PostMapping("/v1/{community_id}/{post_id}/update")
  @Operation(summary = "Post Update", security = {
      @SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> postUpdateV1(@AuthenticationPrincipal User user,
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @NotNull(message = POST_INVALID)
      @Min(value = 1, message = POST_INVALID)
      @PathVariable("post_id")
      @Parameter(name = "post_id", description = "Post ID Parameter", example = "1", in = ParameterIn.PATH)
      Long postId,
      @RequestBody @Valid
      PostUpdateRequestDto postUpdateRequestDto) {
    String email = getEmail(user);

    communityService.postUpdateV1(email, postId, postUpdateRequestDto);
    return ResponseEntity.ok(null);
  }

  @DeleteMapping("/v1/{community_id}/{post_id}/delete")
  @Operation(summary = "Post Delete", security = {
      @SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> postDeleteV1(@AuthenticationPrincipal User user,
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @NotNull(message = POST_INVALID)
      @Min(value = 1, message = POST_INVALID)
      @PathVariable("post_id")
      @Parameter(name = "post_id", description = "Post ID Parameter", example = "1", in = ParameterIn.PATH)
      Long postId,
      @Parameter(name = "password", description = "Password Parameter", example = "1q2w3e4r!", in = ParameterIn.QUERY)
      @RequestParam
      String password) {
    String email = getEmail(user);

    communityService.postDeleteV1(email, postId, password);
    return ResponseEntity.ok(null);
  }

  @PostMapping("/v1/{community_id}/{post_id}/create")
  @Operation(summary = "Reply Create", security = {
      @SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdResponseDto.class)))
  })
  public ResponseEntity<IdResponseDto> replyCreateV1(@AuthenticationPrincipal User user,
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @NotNull(message = POST_INVALID)
      @Min(value = 1, message = POST_INVALID)
      @PathVariable("post_id")
      @Parameter(name = "post_id", description = "Post ID Parameter", example = "1", in = ParameterIn.PATH)
      Long postId,
      @RequestBody @Valid
      ReplyCreateRequestDto replyCreateRequestDto) {
    String email = getEmail(user);

    IdResponseDto idResponseDto = communityService.replyCreateV1(email, postId,
        replyCreateRequestDto);
    return ResponseEntity.ok(idResponseDto);
  }

  @GetMapping("/v1/{community_id}/{post_id}/{reply_id}/check")
  @Operation(summary = "Reply Check Password", security = {
      @SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> replyCheckPasswordV1(
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @NotNull(message = POST_INVALID)
      @Min(value = 1, message = POST_INVALID)
      @PathVariable("post_id")
      @Parameter(name = "post_id", description = "Post ID Parameter", example = "1", in = ParameterIn.PATH)
      Long postId,
      @NotNull(message = REPLY_INVALID)
      @Min(value = 1, message = REPLY_INVALID)
      @PathVariable("reply_id")
      @Parameter(name = "reply_id", description = "Reply ID Parameter", example = "1", in = ParameterIn.PATH)
      Long replyId,
      @NotNull(message = PASSWORD_INVALID)
      @Parameter(name = "password", description = "Password Parameter", example = "1q2w3e4r!", in = ParameterIn.QUERY)
      @RequestParam
      String password) {
    if (communityService.replyCheckPasswordV1(null, replyId, password)) {
      return ResponseEntity.ok(null);
    }
    throw new CommunityException(COMMUNITY_PASSWORD_INVALID);
  }

  @PostMapping("/v1/{community_id}/{post_id}/{reply_id}/update")
  @Operation(summary = "Reply Update", security = {
      @SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> replyUpdateV1(@AuthenticationPrincipal User user,
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @NotNull(message = POST_INVALID)
      @Min(value = 1, message = POST_INVALID)
      @PathVariable("post_id")
      @Parameter(name = "post_id", description = "Post ID Parameter", example = "1", in = ParameterIn.PATH)
      Long postId,
      @NotNull(message = REPLY_INVALID)
      @Min(value = 1, message = REPLY_INVALID)
      @PathVariable("reply_id")
      @Parameter(name = "reply_id", description = "Reply ID Parameter", example = "1", in = ParameterIn.PATH)
      Long replyId,
      @RequestBody @Valid
      ReplyUpdateRequestDto replyUpdateRequestDto) {
    String email = getEmail(user);

    communityService.replyUpdateV1(email, replyId, replyUpdateRequestDto);
    return ResponseEntity.ok(null);
  }

  @DeleteMapping("/v1/{community_id}/{post_id}/{reply_id}/delete")
  @Operation(summary = "Reply Delete", security = {
      @SecurityRequirement(name = "bearer-key")}, responses = {
      @ApiResponse(description = "Success", responseCode = "200",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmptyResponseDto.class)))
  })
  public ResponseEntity<EmptyResponseDto> replyDeleteV1(@AuthenticationPrincipal User user,
      @NotNull(message = COMMUNITY_INVALID)
      @Min(value = 1, message = COMMUNITY_INVALID)
      @PathVariable("community_id")
      @Parameter(name = "community_id", description = "Community ID Parameter", example = "1", in = ParameterIn.PATH)
      Long communityId,
      @NotNull(message = POST_INVALID)
      @Min(value = 1, message = POST_INVALID)
      @PathVariable("post_id")
      @Parameter(name = "post_id", description = "Post ID Parameter", example = "1", in = ParameterIn.PATH)
      Long postId,
      @NotNull(message = REPLY_INVALID)
      @Min(value = 1, message = REPLY_INVALID)
      @PathVariable("reply_id")
      @Parameter(name = "reply_id", description = "Reply ID Parameter", example = "1", in = ParameterIn.PATH)
      Long replyId,
      @Parameter(name = "password", description = "Password Parameter", example = "1q2w3e4r!", in = ParameterIn.QUERY)
      @RequestParam
      String password) {
    String email = getEmail(user);

    communityService.replyDeleteV1(email, replyId, password);
    return ResponseEntity.ok(null);
  }

  private String getEmail(User user) {
    if (user == null) {
      return null;
    }
    return user.getUsername();
  }
}
