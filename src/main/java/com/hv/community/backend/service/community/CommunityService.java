package com.hv.community.backend.service.community;

import com.hv.community.backend.domain.community.Community;
import com.hv.community.backend.domain.community.Post;
import com.hv.community.backend.domain.community.Reply;
import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.dto.community.CommunityListResponseDto;
import com.hv.community.backend.dto.community.PostCreateRequestDto;
import com.hv.community.backend.dto.community.PostDetailResponseDto;
import com.hv.community.backend.dto.community.PostDto;
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

  public CommunityService(CommunityRepository communityRepository, PostRepository postRepository,
      ReplyRepository replyRepository, MemberRepository memberRepository,
      PasswordEncoder passwordEncoder) {
    this.communityRepository = communityRepository;
    this.postRepository = postRepository;
    this.replyRepository = replyRepository;
    this.memberRepository = memberRepository;
    this.passwordEncoder = passwordEncoder;
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
  public Map<String, Object> communityListV1(Integer page, Integer pageSize) {
    if (page == null || page < 1 || pageSize == null || pageSize < 1) {
      // 1보다 작거나 없는 경우 에러 리턴
      throw new CommunityException("COMMUNITY:PAGE_INVALID");
    }

    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Community> communityList = communityRepository.findAll(pageable);
    List<CommunityListResponseDto> communityListResponseDtos = communityList.stream()
        .map(CommunityListResponseDto::of).toList();
    Map<String, Object> responseData = new HashMap<>();

    if ((page - 1 > communityList.getTotalPages() && !communityList.isEmpty())
        || (page > 1 && communityList.isEmpty())) {
      // 초과 에러 리턴
      throw new CommunityException("COMMUNITY:PAGE_INVALID");
    }

    try {
      int currentPage = communityList.getNumber();
      Integer prev = (!communityList.hasPrevious()) ? null : currentPage;
      Integer next = (!communityList.hasNext()) ? null : currentPage + 2;

      responseData.put("page", currentPage);
      responseData.put("total_page", communityList.getTotalPages() + 1);
      responseData.put("page_size", pageSize);
      responseData.put("prev", prev);
      responseData.put("next", next);
      responseData.put("items", communityListResponseDtos);

      return responseData;
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:GET_COMMUNITY_LIST_FAIL");
    }
  }


  // GET /v1/{community_id}/get
  // 게시글 목록 조회
  // community_id, page, page_size
  // 페이지 수, 게시글 목록 반환
  public Map<String, Object> postListV1(Long communityId, Integer page, Integer pageSize) {
    if (page == null || page < 1 || pageSize == null || pageSize < 1) {
      // 1보다 작거나 없는 경우 에러 리턴
      throw new CommunityException("COMMUNITY:PAGE_INVALID");
    }
    Community community = communityRepository.findById(communityId)
        .orElseThrow(() -> new CommunityException("COMMUNITY:COMMUNITY_INVALID"));

    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Post> postPage = postRepository.findByCommunity(community, pageable);
    if ((page - 1 > postPage.getTotalPages() && !postPage.isEmpty())
        || (page > 1 && postPage.isEmpty())) {
      // 초과 에러 리턴
      throw new CommunityException("COMMUNITY:PAGE_INVALID");
    }
    try {
      List<PostDto> postDtos = postPage.stream()
          .map(PostDto::of).toList();
      Map<String, Object> responseData = new HashMap<>();

      int currentPage = postPage.getNumber();
      Integer prev = (!postPage.hasPrevious()) ? null : currentPage;
      Integer next = (!postPage.hasNext()) ? null : currentPage + 2;

      responseData.put("page", currentPage);
      responseData.put("total_page", postPage.getTotalPages());
      responseData.put("page_size", pageSize);
      responseData.put("prev", prev);
      responseData.put("next", next);
      responseData.put("items", postDtos);

      return responseData;
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:GET_POST_LIST_FAIL");
    }
  }

  // GET /v1/{community_id}/{post_id}/get
  // 게시글 상세 조회
  // post_id, page, page_size
  // 게시글 id, title, 작성자, reply 배열 반환
  public PostDetailResponseDto postDetailV1(Long postId, Integer page, Integer pageSize) {
    if (page == null || page < 1 || pageSize == null || pageSize < 1) {
      // 1보다 작거나 없는 경우 에러 리턴
      throw new CommunityException("COMMUNITY:PAGE_INVALID");
    }

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException("COMMUNITY:POST_INVALID"));
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Reply> replyPage = replyRepository.findByPost(post, pageable);

    if ((page - 1 > replyPage.getTotalPages() && !replyPage.isEmpty())
        || (page > 1 && replyPage.isEmpty())) {
      // 초과 에러 리턴
      throw new CommunityException("COMMUNITY:PAGE_INVALID");
    }
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
      List<ReplyDto> replyDtoList = replyPage.stream().map(ReplyDto::of).toList();
      return PostDetailResponseDto.of(post, previousPostId, nextPostId,
          replyDtoList,
          replyPage.getNumber(),
          replyPage.getTotalPages(),
          replyPage.hasPrevious(),
          replyPage.hasNext()
      );
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:GET_POST_DETAIL_FAIL");
    }
  }


  // POST /v1/{community_id}/create
  // 게시글 작성
  // accessToken, title, content, nickname, password
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
      throw new CommunityException("COMMUNITY:CREATE_POST_FAIL");
    }
  }

  // GET /v1/{community_id}/{post_id}/check?password=
  // 등록된 유저가 아닌 경우 글 수정 전에 비밀 번호 확인
  // community_id, post_id, password
  public boolean postCheckPasswordV1(Long post_id, String password) {
    Post post = postRepository.findById(post_id)
        .orElseThrow(() -> new CommunityException("COMMUNITY:POST_INVALID"));
    if (post.getPassword() == null) {
      return false;
    } else {
      return passwordEncoder.matches(password, post.getPassword());
    }
  }


  // POST /v1/{community_id}/{post_id}/update
  // 게시글 수정
  // accessToken, community_id, post_id, title, content, password
  public void postUpdateV1(String email, Long postId, PostUpdateRequestDto postUpdateRequestDto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException("COMMUNITY:POST_INVALID"));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
      if (post.getPassword() == null) {
        throw new CommunityException("COMMUNITY:PERMISSION_INVALID");
      } else {
        if (passwordEncoder.matches(postUpdateRequestDto.getPassword(), post.getPassword())) {
          try {
            post.setTitle(postUpdateRequestDto.getTitle());
            post.setContent(postUpdateRequestDto.getContent());

            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();
            post.setModificationTime(currentDate);
            postRepository.save(post);
          } catch (Exception e) {
            throw new CommunityException("COMMUNITY:UPDATE_POST_FAIL");
          }
        } else {
          throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
        }
      }
    } else {
      // 유저일 경우
      if ((post.getMember() == null) || (!Objects.equals(post.getMember().getEmail(), email))) {
        throw new CommunityException("COMMUNITY:PERMISSION_INVALID");
      } else {
        try {
          post.setTitle(postUpdateRequestDto.getTitle());
          post.setContent(postUpdateRequestDto.getContent());

          Calendar calendar = Calendar.getInstance();
          Date currentDate = calendar.getTime();
          post.setModificationTime(currentDate);
          postRepository.save(post);
        } catch (Exception e) {
          throw new CommunityException("COMMUNITY:UPDATE_POST_FAIL");
        }
      }
    }
  }


  // DELETE /v1/{community_id}/{post_id}/delete?password=
  // 게시글 삭제
  // accessToken, community_id, post_id, password
  public void postDeleteV1(String email, Long postId, String password) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException("COMMUNITY:POST_INVALID"));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
      if (post.getPassword() == null) {
        throw new CommunityException("COMMUNITY:PERMISSION_INVALID");
      } else {
        if (passwordEncoder.matches(password, post.getPassword())) {
          try {
            postRepository.delete(post);
          } catch (Exception e) {
            throw new CommunityException("COMMUNITY:DELETE_POST_FAIL");
          }
        } else {
          throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
        }
      }
    } else {
      // 유저일 경우
      if ((post.getMember() == null) || (!Objects.equals(post.getMember().getEmail(), email))) {
        throw new CommunityException("COMMUNITY:PERMISSION_INVALID");
      } else {
        try {
          postRepository.delete(post);
        } catch (Exception e) {
          throw new CommunityException("COMMUNITY:DELETE_POST_FAIL");
        }
      }
    }
  }

  // POST /v1/{community_id}/{post_id}/create
  // 댓글 작성
  // accessToken, community_id, post_id, reply, nickname, password
  public Long replyCreateV1(String email, Long post_id,
      ReplyCreateRequestDto replyCreateRequestDto) {
    Post post = postRepository.findById(post_id)
        .orElseThrow(() -> new CommunityException("COMMUNITY:POST_INVALID"));
    Reply reply = new Reply();
    reply.setReply(replyCreateRequestDto.getReply());
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
      throw new CommunityException("COMMUNITY:CREATE_REPLY_FAIL");
    }
  }

  // GET /v1/{community_id}/{post_id}/{reply_id}/check?password=
  // 등록된 유저가 아닌 경우 댓글 수정 전에 비밀 번호 확인
  // community_id, post_id, reply_id, password
  public boolean replyCheckPasswordV1(Long reply_id, String password) {
    Reply reply = replyRepository.findById(reply_id)
        .orElseThrow(() -> new CommunityException("COMMUNITY:REPLY_INVALID"));
    if (reply.getPassword() == null) {
      return false;
    } else {
      return passwordEncoder.matches(password, reply.getPassword());
    }
  }

  // POST /v1/{community_id}/{post_id}/{reply_id}/update
  // 댓글 수정
  // accessToken, community_id, post_id, reply_id, reply, password
  public void replyUpdateV1(String email, Long reply_id,
      ReplyUpdateRequestDto replyUpdateRequestDto) {
    Reply reply = replyRepository.findById(reply_id)
        .orElseThrow(() -> new CommunityException("COMMUNITY:REPLY_INVALID"));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
      if (reply.getPassword() == null) {
        throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
      } else {
        // 이메일없는 유저요청 + 등록된 유저가 쓴글이 아닐때 > 비밀번호 검사
        if (passwordEncoder.matches(replyUpdateRequestDto.getPassword(), reply.getPassword())) {
          try {
            reply.setReply(replyUpdateRequestDto.getReply());
            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();
            reply.setModificationTime(currentDate);
            replyRepository.save(reply);
          } catch (Exception e) {
            throw new CommunityException("COMMUNITY:UPDATE_REPLY_FAIL");
          }
        } else {
          throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
        }
      }
    } else {
      // 유저일 경우
      if ((reply.getMember() == null) || (!Objects.equals(reply.getMember().getEmail(), email))) {
        throw new CommunityException("COMMUNITY:PERMISSION_INVALID");
      } else {
        try {
          reply.setReply(replyUpdateRequestDto.getReply());
          Calendar calendar = Calendar.getInstance();
          Date currentDate = calendar.getTime();
          reply.setModificationTime(currentDate);
          replyRepository.save(reply);
        } catch (Exception e) {
          throw new CommunityException("COMMUNITY:UPDATE_REPLY_FAIL");
        }
      }
    }
  }

  // DELETE /v1/{community_id}/{post_id}/{reply_id}/delete?password=
  // 댓글 삭제
  // accessToken, community_id, post_id, reply_id, password
  public void replyDeleteV1(String email, Long reply_id, String password) {
    Reply reply = replyRepository.findById(reply_id)
        .orElseThrow(() -> new CommunityException("COMMUNITY:REPLY_INVALID"));
    Post post = reply.getPost();
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
      if (reply.getPassword() == null) {
        throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
      } else {
        if (passwordEncoder.matches(password, reply.getPassword())) {
          try {
            replyRepository.delete(reply);
            postRepository.save(post);
          } catch (Exception e) {
            throw new CommunityException("COMMUNITY:DELETE_REPLY_FAIL");
          }
        } else {
          throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
        }
      }
    } else {
      // 유저일 경우
      if ((reply.getMember() == null) || (!Objects.equals(reply.getMember().getEmail(), email))) {
        throw new CommunityException("COMMUNITY:PERMISSION_INVALID");
      } else {
        try {
          replyRepository.delete(reply);
          postRepository.save(post);
        } catch (Exception e) {
          throw new CommunityException("COMMUNITY:DELETE_REPLY_FAIL");
        }
      }
    }
  }
}
