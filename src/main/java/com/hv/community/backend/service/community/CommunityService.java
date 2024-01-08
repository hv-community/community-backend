package com.hv.community.backend.service.community;

import com.hv.community.backend.domain.community.Community;
import com.hv.community.backend.domain.community.Post;
import com.hv.community.backend.domain.community.Reply;
import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.dto.community.CommunityListResponseDto;
import com.hv.community.backend.dto.community.PostCreateRequestDto;
import com.hv.community.backend.dto.community.PostDetailResponseDto;
import com.hv.community.backend.dto.community.PostDto;
import com.hv.community.backend.dto.community.PostReplyResponseDto;
import com.hv.community.backend.dto.community.PostUpdateRequestDto;
import com.hv.community.backend.dto.community.ReplyCreateRequestDto;
import com.hv.community.backend.dto.community.ReplyDto;
import com.hv.community.backend.dto.community.ReplyUpdateRequestDto;
import com.hv.community.backend.exception.CommunityException;
import com.hv.community.backend.exception.MemberException;
import com.hv.community.backend.repository.community.CommunityRepository;
import com.hv.community.backend.repository.community.PostRepository;
import com.hv.community.backend.repository.community.ReplyRepository;
import com.hv.community.backend.repository.member.MemberRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class CommunityService {

  private final CommunityRepository communityRepository;
  private final PostRepository postRepository;
  private final ReplyRepository replyRepository;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  private static String pageInvalid = "COMMUNITY:PAGE_INVALID";
  private static String postInvalid = "COMMUNITY:POST_INVALID";
  private static String replyInvalid = "COMMUNITY:REPLY_INVALID";
  private static String passwordInvalid = "COMMUNITY:PASSWORD_INVALID";
  private static String permissionInvalid = "COMMUNITY:PERMISSION_INVALID";

  public CommunityService(CommunityRepository communityRepository, PostRepository postRepository,
      ReplyRepository replyRepository, MemberRepository memberRepository,
      PasswordEncoder passwordEncoder) {
    this.communityRepository = communityRepository;
    this.postRepository = postRepository;
    this.replyRepository = replyRepository;
    this.memberRepository = memberRepository;
    this.passwordEncoder = passwordEncoder;
  }


  private void validatePageInput(Integer page, Integer pageSize) {
    if (page == null || page < 1 || pageSize == null || pageSize < 1) {
      // 1보다 작거나 없는 경우 에러 리턴
      throw new CommunityException(pageInvalid);
    }

  }

  private void validatePage(Integer page, Page<?> pageList) {
    if ((page - 1 > pageList.getTotalPages() && !pageList.isEmpty())
        || (page > 1 && pageList.isEmpty())) {
      // 초과 에러 리턴
      throw new CommunityException(pageInvalid);
    }
  }

  public Map<String, Object> communityListV1(Integer page, Integer pageSize) {
    validatePageInput(page, pageSize);
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Community> communityList = communityRepository.findAll(pageable);
    List<CommunityListResponseDto> communityListResponseDtos = communityList.stream()
        .map(CommunityListResponseDto::of).toList();
    Map<String, Object> responseData = new HashMap<>();

    validatePage(page, communityList);
    try {
      int currentPage = communityList.getNumber() + 1;
      Integer prev = (!communityList.hasPrevious()) ? null : currentPage - 1;
      Integer next = (!communityList.hasNext()) ? null : currentPage + 1;

      responseData.put("page", currentPage);
      responseData.put("total_page", communityList.getTotalPages());
      responseData.put("page_size", pageSize);
      responseData.put("prev", prev);
      responseData.put("next", next);
      responseData.put("items", communityListResponseDtos);

      return responseData;
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:COMMUNITY_LIST_FAIL");
    }
  }

  public Map<String, Object> postListV1(Long communityId, Integer page, Integer pageSize) {
    validatePageInput(page, pageSize);
    Community community = communityRepository.findById(communityId)
        .orElseThrow(() -> new CommunityException("COMMUNITY:COMMUNITY_INVALID"));

    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Post> postPage = postRepository.findByCommunity(community, pageable);
    validatePage(page, postPage);

    try {
      List<PostDto> postDtos = postPage.stream()
          .map(PostDto::of).toList();
      Map<String, Object> responseData = new HashMap<>();

      int currentPage = postPage.getNumber() + 1;
      Integer prev = (!postPage.hasPrevious()) ? null : currentPage - 1;
      Integer next = (!postPage.hasNext()) ? null : currentPage + 1;

      responseData.put("page", currentPage);
      responseData.put("total_page", postPage.getTotalPages());
      responseData.put("page_size", pageSize);
      responseData.put("prev", prev);
      responseData.put("next", next);
      responseData.put("items", postDtos);

      return responseData;
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:POST_LIST_FAIL");
    }
  }

  public PostDetailResponseDto postDetailV1(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(postInvalid));
    Long previousPostId = null;
    Long nextPostId = null;
    Post previousPost = postRepository.findTopByCommunityIdAndIdLessThanOrderByIdDesc(
        post.getCommunity().getId(), postId).orElse(null);
    if (previousPost != null) {
      previousPostId = previousPost.getId();
    }
    Post nextPost = postRepository.findTopByCommunityIdAndIdGreaterThanOrderByIdAsc(
        post.getCommunity().getId(), postId).orElse(null);
    if (nextPost != null) {
      nextPostId = nextPost.getId();
    }

    try {
      return PostDetailResponseDto.of(post, previousPostId, nextPostId);
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:POST_DETAIL_FAIL");
    }
  }

  public PostReplyResponseDto postReplyV1(Long postId, Integer page, Integer pageSize) {
    validatePageInput(page, pageSize);
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(postInvalid));
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Reply> replyPage = replyRepository.findByPost(post, pageable);

    validatePage(page, replyPage);
    try {
      List<ReplyDto> replyDtoList = replyPage.stream().map(ReplyDto::of).toList();

      return PostReplyResponseDto.of(replyDtoList, replyPage, pageSize);
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:POST_REPLY_FAIL");
    }

  }

  public Long postCreateV1(String email, Long communityId,
      PostCreateRequestDto postCreateRequestDto) {
    Community community = communityRepository.findById(communityId)
        .orElseThrow(() -> new CommunityException("COMMUNITY:COMMUNITY_INVALID"));

    Post post = new Post();
    post.setTitle(postCreateRequestDto.getTitle());
    post.setContent(postCreateRequestDto.getContent());
    // 유저일때만 email저장 아니면 code만 저장
    if (email.isEmpty()) {
      post.setNickname(postCreateRequestDto.getNickname());
      post.setPassword(passwordEncoder.encode(postCreateRequestDto.getPassword()));
    } else {
      Member member = memberRepository.findByEmail(email)
          .orElseThrow(() -> new MemberException("MEMBER:MEMBER_UNREGISTERED"));
      post.setMember(member);
      post.setNickname(member.getNickname());
    }
    try {
      Calendar calendar = Calendar.getInstance();
      Date currentDate = calendar.getTime();
      post.setCreationTime(currentDate);

      post.setCommunity(community);
      postRepository.save(post);
      return post.getId();
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:POST_CREATE_FAIL");
    }
  }

  public boolean postCheckPasswordV1(Long postId, String password) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(postInvalid));
    if (post.getPassword() == null) {
      return false;
    } else {
      return passwordEncoder.matches(password, post.getPassword());
    }
  }

  public void postUpdateV1(String email, Long postId, PostUpdateRequestDto postUpdateRequestDto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(postInvalid));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
      if (post.getPassword() == null) {
        throw new CommunityException(permissionInvalid);
      }
      if (passwordEncoder.matches(postUpdateRequestDto.getPassword(), post.getPassword())) {
        try {
          post.setTitle(postUpdateRequestDto.getTitle());
          post.setContent(postUpdateRequestDto.getContent());

          Calendar calendar = Calendar.getInstance();
          Date currentDate = calendar.getTime();
          post.setModificationTime(currentDate);
          postRepository.save(post);
          return;
        } catch (Exception e) {
          throw new CommunityException("COMMUNITY:POST_UPDATE_FAIL");
        }
      } else {
        throw new CommunityException(passwordInvalid);
      }
    }
    // 유저일 경우
    if ((post.getMember() == null) || (!Objects.equals(post.getMember().getEmail(), email))) {
      throw new CommunityException(permissionInvalid);
    } else {
      try {
        post.setTitle(postUpdateRequestDto.getTitle());
        post.setContent(postUpdateRequestDto.getContent());

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        post.setModificationTime(currentDate);
        postRepository.save(post);
      } catch (Exception e) {
        throw new CommunityException("COMMUNITY:POST_UPDATE_FAIL");
      }
    }
  }

  public void postDeleteV1(String email, Long postId, String password) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(postInvalid));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
      if (post.getPassword() == null) {
        throw new CommunityException(permissionInvalid);
      }
      if (passwordEncoder.matches(password, post.getPassword())) {
        try {
          postRepository.delete(post);
          return;
        } catch (Exception e) {
          throw new CommunityException("COMMUNITY:POST_DELETE_FAIL");
        }
      } else {
        throw new CommunityException(passwordInvalid);
      }
    }
    // 유저일 경우
    if ((post.getMember() == null) || (!Objects.equals(post.getMember().getEmail(), email))) {
      throw new CommunityException(permissionInvalid);
    } else {
      try {
        postRepository.delete(post);
      } catch (Exception e) {
        throw new CommunityException("COMMUNITY:POST_DELETE_FAIL");
      }
    }
  }

  public Long replyCreateV1(String email, Long postId,
      ReplyCreateRequestDto replyCreateRequestDto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(postInvalid));
    Reply reply = new Reply();
    reply.setContent(replyCreateRequestDto.getContent());
    if (email.isEmpty()) {
      reply.setNickname(replyCreateRequestDto.getNickname());
      reply.setPassword(passwordEncoder.encode(replyCreateRequestDto.getPassword()));
    } else {
      Member member = memberRepository.findByEmail(email)
          .orElseThrow(() -> new MemberException("MEMBER:MEMBER_UNREGISTERED"));
      reply.setMember(member);
      reply.setNickname(member.getNickname());
    }
    try {
      Calendar calendar = Calendar.getInstance();
      Date currentDate = calendar.getTime();
      reply.setCreationTime(currentDate);

      reply.setCommunity(post.getCommunity());
      reply.setPost(post);
      replyRepository.save(reply);
      postRepository.save(post);
      return reply.getId();
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:REPLY_CREATE_FAIL");
    }
  }

  public boolean replyCheckPasswordV1(Long replyId, String password) {
    Reply reply = replyRepository.findById(replyId)
        .orElseThrow(() -> new CommunityException(replyInvalid));
    if (reply.getPassword() == null) {
      return false;
    } else {
      return passwordEncoder.matches(password, reply.getPassword());
    }
  }

  public void replyUpdateV1(String email, Long replyId,
      ReplyUpdateRequestDto replyUpdateRequestDto) {
    Reply reply = replyRepository.findById(replyId)
        .orElseThrow(() -> new CommunityException(replyInvalid));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
      if (reply.getPassword() == null) {
        throw new CommunityException(passwordInvalid);
      }
      // 이메일없는 유저요청 + 등록된 유저가 쓴글이 아닐때 > 비밀번호 검사
      if (passwordEncoder.matches(replyUpdateRequestDto.getPassword(), reply.getPassword())) {
        try {
          reply.setContent(replyUpdateRequestDto.getContent());
          Calendar calendar = Calendar.getInstance();
          Date currentDate = calendar.getTime();
          reply.setModificationTime(currentDate);
          replyRepository.save(reply);
          return;
        } catch (Exception e) {
          throw new CommunityException("COMMUNITY:REPLY_UPDATE_FAIL");
        }
      } else {
        throw new CommunityException(passwordInvalid);
      }
    }
    // 유저일 경우
    if ((reply.getMember() == null) || (!Objects.equals(reply.getMember().getEmail(), email))) {
      throw new CommunityException(permissionInvalid);
    } else {
      try {
        reply.setContent(replyUpdateRequestDto.getContent());
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        reply.setModificationTime(currentDate);
        replyRepository.save(reply);
      } catch (Exception e) {
        throw new CommunityException("COMMUNITY:REPLY_UPDATE_FAIL");
      }
    }
  }

  public void replyDeleteV1(String email, Long replyId, String password) {
    Reply reply = replyRepository.findById(replyId)
        .orElseThrow(() -> new CommunityException(replyInvalid));
    Post post = reply.getPost();
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
      if (reply.getPassword() == null) {
        throw new CommunityException(passwordInvalid);
      }
      if (passwordEncoder.matches(password, reply.getPassword())) {
        try {
          replyRepository.delete(reply);
          postRepository.save(post);
          return;
        } catch (Exception e) {
          throw new CommunityException("COMMUNITY:REPLY_DELETE_FAIL");
        }
      } else {
        throw new CommunityException(passwordInvalid);
      }
    }
    // 유저일 경우
    if ((reply.getMember() == null) || (!Objects.equals(reply.getMember().getEmail(), email))) {
      throw new CommunityException(permissionInvalid);
    } else {
      try {
        replyRepository.delete(reply);
        postRepository.save(post);
      } catch (Exception e) {
        throw new CommunityException("COMMUNITY:REPLY_DELETE_FAIL");
      }
    }
  }
}
