package com.hv.community.backend.service.community;

import com.hv.community.backend.domain.community.Community;
import com.hv.community.backend.domain.community.Post;
import com.hv.community.backend.domain.community.Reply;
import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.dto.community.CheckPostPasswordRequestDto;
import com.hv.community.backend.dto.community.CheckReplyPasswordRequestDto;
import com.hv.community.backend.dto.community.CreatePostRequestDto;
import com.hv.community.backend.dto.community.CreateReplyRequestDto;
import com.hv.community.backend.dto.community.GetCommunityListResponseDto;
import com.hv.community.backend.dto.community.GetPostDetailResponseDto;
import com.hv.community.backend.dto.community.PostDto;
import com.hv.community.backend.dto.community.ReplyDto;
import com.hv.community.backend.dto.community.UpdatePostRequestDto;
import com.hv.community.backend.dto.community.UpdateReplyRequestDto;
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
  // title, content, member, password

  // POST checkPostPassword
  // 등록된 유저가 아닌 경우 글 수정전에 비밀번호 확인
  // post_id, password

  // POST updatePost
  // 게시글 수정
  // post_id, title, content, member, password

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

  // test code
  // POST createCommunity
//  public void createCommunity(CreateCommunityRequestDto createCommunityRequestDto) {
//    Community community = new Community();
//    community.setTitle(createCommunityRequestDto.getTitle());
//    community.setDescription(community.getDescription());
//    communityRepository.save(community);
//  }


  // GET getCommunityList
  // 게시판 리스트 조회
  // id, community, description, thumbnail반환
  public Map<String, Object> getCommunityListV1(Integer page, Integer pageSize) {
    if (page == null || page < 1 || pageSize == null || pageSize < 1) {
      // 1보다 작거나 없는경우 에러리턴
      throw new CommunityException("COMMUNITY:PAGE_INVALID");
    }

    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Community> communityList = communityRepository.findAll(pageable);
    List<GetCommunityListResponseDto> communityListResponseDtos = communityList.stream()
        .map(GetCommunityListResponseDto::of).toList();
    Map<String, Object> responseData = new HashMap<>();

    if (page > communityList.getTotalPages()) {
      // 초과에러 리턴
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
      responseData.put("communities", communityListResponseDtos);

      return responseData;
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:GET_COMMUNITY_LIST_FAIL");
    }
  }

  // GET getPostList/{community-id}
  // 게시글 목록 조회
  // 게시글 id, title, reply갯수, 작성자
  public Map<String, Object> getPostListV1(Long communityId, Integer page, Integer pageSize) {
    if (page == null || page < 1 || pageSize == null || pageSize < 1) {
      // 1보다 작거나 없는경우 에러리턴
      throw new CommunityException("COMMUNITY:PAGE_INVALID");
    }
    Community community = communityRepository.findById(communityId)
        .orElseThrow(() -> new CommunityException("COMMUNITY:COMMUNITY_INVALID"));

    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Post> postPage = postRepository.findByCommunity(community, pageable);
    if (page > postPage.getTotalPages()) {
      // 초과에러 리턴
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
      responseData.put("posts", postDtos);

      return responseData;
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:GET_POST_LIST_FAIL");
    }
  }


  // GET getPostDetail/{post-id}
  // 게시글 상세 조회
  // 게시글 id, title, 작성자, reply배열 반환
  public GetPostDetailResponseDto getPostDetailV1(Long postId, Integer page, Integer pageSize) {
    if (page == null || page < 1 || pageSize == null || pageSize < 1) {
      // 1보다 작거나 없는경우 에러리턴
      throw new CommunityException("COMMUNITY:PAGE_INVALID");
    }

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException("COMMUNITY:POST_INVALID"));
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Reply> replyPage = replyRepository.findByPost(post, pageable);

    if (page > replyPage.getTotalPages()) {
      // 초과에러 리턴
      throw new CommunityException("COMMUNITY:PAGE_INVALID");
    }
    try {
      List<ReplyDto> replyDtoList = replyPage.stream().map(ReplyDto::of).toList();
      return GetPostDetailResponseDto.of(post,
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

  // POST createPost
  // 게시글 작성
  // title, content, member, password
  public Long createPostV1(String email, CreatePostRequestDto createPostRequestDto) {
    Community community = communityRepository.findById(createPostRequestDto.getCommunity_id())
        .orElseThrow(() -> new CommunityException("COMMUNITY:COMMUNITY_INVALID"));

    Post post = new Post();
    post.setTitle(createPostRequestDto.getTitle());
    post.setContent(createPostRequestDto.getContent());
    post.setReplyCount(0);
    // 유저일때만 email저장 아니면 code만 저장
    if (email.isEmpty()) {
      post.setNickname(createPostRequestDto.getNickname());
      post.setPassword(passwordEncoder.encode(createPostRequestDto.getPassword()));
    } else {
      Member member = memberRepository.findByEmail(email)
          .orElseThrow(() -> new MemberException("MEMBER:MEMBER_UNREGISTERED"));
      post.setMember(member);
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

  // POST checkPostPassword
  // 등록된 유저가 아닌 경우 글 수정전에 비밀번호 확인
  // post_id, password
  public boolean checkPostPasswordV1(CheckPostPasswordRequestDto checkPostPasswordRequestDto) {
    Long postId = checkPostPasswordRequestDto.getPost_id();
    String password = checkPostPasswordRequestDto.getPassword();
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException("COMMUNITY:POST_INVALID"));
    if (post.getPassword() == null) {
      return false;
    } else {
      return passwordEncoder.matches(password, post.getPassword());
    }
  }

  // POST updatePost
  // 게시글 수정
  // post_id, title, content, member, password
  public void updatePostV1(String email, UpdatePostRequestDto updatePostRequestDto) {
    Post post = postRepository.findById(updatePostRequestDto.getPost_id())
        .orElseThrow(() -> new CommunityException("COMMUNITY:POST_INVALID"));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
      if (post.getPassword() == null) {
        throw new CommunityException("COMMUNITY:PERMISSION_INVALID");
      } else {
        if (passwordEncoder.matches(updatePostRequestDto.getPassword(), post.getPassword())) {
          try {
            post.setTitle(updatePostRequestDto.getTitle());
            post.setContent(updatePostRequestDto.getContent());

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
          post.setTitle(updatePostRequestDto.getTitle());
          post.setContent(updatePostRequestDto.getContent());

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

  // DELETE deletePost
  // 게시글 삭제
  // accessToken, code
  public void deletePostV1(String email, Long postId, String password) {
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

  // POST createReply
  // 댓글 작성
  // reply, owner, code
  public Long createReplyV1(String email, CreateReplyRequestDto createReplyRequestDto) {
    Post post = postRepository.findById(createReplyRequestDto.getPost_id())
        .orElseThrow(() -> new CommunityException("COMMUNITY:POST_INVALID"));
    Reply reply = new Reply();
    reply.setReply(createReplyRequestDto.getReply());
    if (email.isEmpty()) {
      reply.setNickname(createReplyRequestDto.getNickname());
      reply.setPassword(passwordEncoder.encode(createReplyRequestDto.getPassword()));
    } else {
      Member member = memberRepository.findByEmail(email)
          .orElseThrow(() -> new MemberException("MEMBER:MEMBER_UNREGISTERED"));
      reply.setMember(member);
    }
    try {
      Calendar calendar = Calendar.getInstance();
      Date currentDate = calendar.getTime();
      reply.setCreationTime(currentDate);

      reply.setCommunity(post.getCommunity());
      reply.setPost(post);
      replyRepository.save(reply);
      post.setReplyCount(post.getReplyCount() + 1);
      postRepository.save(post);
      return reply.getId();
    } catch (Exception e) {
      throw new CommunityException("COMMUNITY:CREATE_REPLY_FAIL");
    }
  }

  // POST checkReplyPassword
  // 등록된 유저가 아닌 경우 댓글 수정전에 비밀번호 확인
  // reply_id, password
  public boolean checkReplyPasswordV1(CheckReplyPasswordRequestDto checkReplyPasswordRequestDto) {
    Long replyId = checkReplyPasswordRequestDto.getReply_id();
    String password = checkReplyPasswordRequestDto.getPassword();
    Reply reply = replyRepository.findById(replyId)
        .orElseThrow(() -> new CommunityException("COMMUNITY:REPLY_INVALID"));
    if (reply.getPassword() == null) {
      return false;
    } else {
      return passwordEncoder.matches(password, reply.getPassword());
    }
  }

  // POST updateReply
  // 댓글 수정
  // reply, owner, code
  public void updateReplyV1(String email, UpdateReplyRequestDto updateReplyRequestDto) {
    Reply reply = replyRepository.findById(updateReplyRequestDto.getReply_id())
        .orElseThrow(() -> new CommunityException("COMMUNITY:REPLY_INVALID"));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
      if (reply.getPassword() == null) {
        throw new CommunityException("COMMUNITY:PASSWORD_INVALID");
      } else {
        // 이메일없는 유저요청 + 등록된 유저가 쓴글이 아닐때 > 비밀번호 검사
        if (passwordEncoder.matches(updateReplyRequestDto.getPassword(), reply.getPassword())) {
          try {
            reply.setReply(updateReplyRequestDto.getReply());
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
          reply.setReply(updateReplyRequestDto.getReply());
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

  // DELETE deleteReply
  // 댓글 삭제
  // accessToken, code
  public void deleteReplyV1(String email, Long replyId, String password) {
    Reply reply = replyRepository.findById(replyId)
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
            post.setReplyCount(post.getReplyCount() - 1);
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
          post.setReplyCount(post.getReplyCount() - 1);
          postRepository.save(post);
        } catch (Exception e) {
          throw new CommunityException("COMMUNITY:DELETE_REPLY_FAIL");
        }
      }
    }
  }
}
