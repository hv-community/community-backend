package com.hv.community.backend.service.community;

import com.hv.community.backend.domain.community.Community;
import com.hv.community.backend.domain.community.Post;
import com.hv.community.backend.domain.community.Reply;
import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.dto.community.CheckPostPasswordRequestDto;
import com.hv.community.backend.dto.community.CheckReplyPasswordRequestDto;
import com.hv.community.backend.dto.community.CreateCommunityRequestDto;
import com.hv.community.backend.dto.community.CreatePostRequestDto;
import com.hv.community.backend.dto.community.CreateReplyRequestDto;
import com.hv.community.backend.dto.community.DeletePostRequestDto;
import com.hv.community.backend.dto.community.DeleteReplyRequestDto;
import com.hv.community.backend.dto.community.GetCommunityListResponseDto;
import com.hv.community.backend.dto.community.GetPostDetailResponseDto;
import com.hv.community.backend.dto.community.PostDto;
import com.hv.community.backend.dto.community.ReplyDto;
import com.hv.community.backend.dto.community.UpdatePostRequestDto;
import com.hv.community.backend.dto.community.UpdateReplyRequestDto;
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

  // POST createCommunity
  public void createCommunity(CreateCommunityRequestDto createCommunityRequestDto) {
    Community community = new Community();
    community.setTitle(createCommunityRequestDto.getTitle());
    community.setDescription(community.getDescription());
    communityRepository.save(community);
  }


  // GET getCommunityList
  // 게시판 리스트 조회
  // id, community, description, thumbnail반환
  public Map<String, Object> getCommunityList(Integer page, Integer pageSize) {
    if (page == null || page < 1 || pageSize == null || pageSize < 1) {
      // 1보다 작거나 없는경우 에러리턴
      throw new RuntimeException("0이하값");
    }

    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Community> communityList = communityRepository.findAll(pageable);
    List<GetCommunityListResponseDto> communityListResponseDtos = communityList.stream()
        .map(GetCommunityListResponseDto::of).toList();
    Map<String, Object> responseData = new HashMap<>();

    if (page > communityList.getTotalPages()) {
      // 초과에러 리턴
      throw new RuntimeException("초과값");
    }

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
  }

  // GET getPostList/{community-id}
  // 게시글 목록 조회
  // 게시글 id, title, reply갯수, 작성자
  public Map<String, Object> getPostList(Long communityId, Integer page, Integer pageSize) {
    if (page == null || page < 1 || pageSize == null || pageSize < 1) {
      // 1보다 작거나 없는경우 에러리턴
      throw new RuntimeException("0이하값");
    }
    Community community = communityRepository.findById(communityId)
        .orElseThrow(() -> new RuntimeException("커뮤니티 정보가 없습니다"));

    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Post> postPage = postRepository.findByCommunity(community, pageable);
    if (page > postPage.getTotalPages()) {
      // 초과에러 리턴
      throw new RuntimeException("초과값");
    }
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
  }


  // GET getPostDetail/{post-id}
  // 게시글 상세 조회
  // 게시글 id, title, 작성자, reply배열 반환
  public GetPostDetailResponseDto getPostDetail(Long postId, Integer page, Integer pageSize) {
    if (page == null || page < 1 || pageSize == null || pageSize < 1) {
      // 1보다 작거나 없는경우 에러리턴
      throw new RuntimeException("0이하값");
    }

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("게시글 정보가 없습니다"));
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Reply> replyPage = replyRepository.findByPost(post, pageable);

    if (page > replyPage.getTotalPages()) {
      // 초과에러 리턴
      throw new RuntimeException("초과값");
    }

    List<ReplyDto> replyDtoList = replyPage.stream().map(ReplyDto::of).toList();
    return GetPostDetailResponseDto.of(post,
        replyDtoList,
        replyPage.getNumber(),
        replyPage.getTotalPages(),
        replyPage.hasPrevious(),
        replyPage.hasNext()
    );
  }

  // POST createPost
  // 게시글 작성
  // title, content, member, password
  public Long createPost(String email, CreatePostRequestDto createPostRequestDto) {
    Community community = communityRepository.findById(createPostRequestDto.getCommunity_id())
        .orElseThrow(() -> new RuntimeException("커뮤니티 정보가 없습니다"));

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
          .orElseThrow(() -> new RuntimeException("유저정보가 없습니다"));
      post.setMember(member);
    }
    Calendar calendar = Calendar.getInstance();
    Date currentDate = calendar.getTime();
    post.setCreationTime(currentDate);

    post.setCommunity(community);
    postRepository.save(post);
    return post.getId();
  }

  // POST checkPostPassword
  // 등록된 유저가 아닌 경우 글 수정전에 비밀번호 확인
  // post_id, password
  public boolean checkPostPassword(CheckPostPasswordRequestDto checkPostPasswordRequestDto) {
    Long postId = checkPostPasswordRequestDto.getPost_id();
    String password = checkPostPasswordRequestDto.getPassword();
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("게시글 정보가 없습니다"));
    if (passwordEncoder.matches(password, post.getPassword())) {
      return true;
    } else {
      return false;
    }
  }

  // POST updatePost
  // 게시글 수정
  // post_id, title, content, member, password
  public void updatePost(String email, UpdatePostRequestDto updatePostRequestDto) {
    Post post = postRepository.findById(updatePostRequestDto.getPost_id())
        .orElseThrow(() -> new RuntimeException("게시글 정보가 없습니다"));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      if (passwordEncoder.matches(updatePostRequestDto.getPassword(), post.getPassword())) {
        post.setTitle(updatePostRequestDto.getTitle());
        post.setContent(updatePostRequestDto.getContent());

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        post.setModificationTime(currentDate);
        postRepository.save(post);
      } else {
        throw new RuntimeException("비밀번호가 일치하지 않습니다");
      }
    } else {
      // 유저일 경우
      if (!Objects.equals(post.getMember().getEmail(), email)) {
        throw new RuntimeException("해당 게시글에 대한 권한이 없습니다");
      } else {
        post.setTitle(updatePostRequestDto.getTitle());
        post.setContent(updatePostRequestDto.getContent());

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        post.setModificationTime(currentDate);
        postRepository.save(post);
      }
    }
  }

  // DELETE deletePost
  // 게시글 삭제
  // accessToken, code
  public void deletePost(String email, DeletePostRequestDto deletePostRequestDto) {
    Post post = postRepository.findById(deletePostRequestDto.getPost_id())
        .orElseThrow(() -> new RuntimeException("게시글 정보가 없습니다"));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      if (passwordEncoder.matches(deletePostRequestDto.getPassword(), post.getPassword())) {
        postRepository.delete(post);
      } else {
        throw new RuntimeException("비밀번호가 일치하지 않습니다");
      }
    } else {
      // 유저일 경우
      if (!Objects.equals(post.getMember().getEmail(), email)) {
        throw new RuntimeException("해당 게시글에 대한 권한이 없습니다");
      } else {
        postRepository.delete(post);
      }
    }
  }

  // POST createReply
  // 댓글 작성
  // reply, owner, code
  public Long createReply(String email, CreateReplyRequestDto createReplyRequestDto) {
    Post post = postRepository.findById(createReplyRequestDto.getPost_id())
        .orElseThrow(() -> new RuntimeException("게시글 정보가 없습니다"));
    Reply reply = new Reply();
    reply.setReply(createReplyRequestDto.getReply());
    if (email.isEmpty()) {
      reply.setNickname(createReplyRequestDto.getNickname());
      reply.setPassword(passwordEncoder.encode(createReplyRequestDto.getPassword()));
    } else {
      Member member = memberRepository.findByEmail(email)
          .orElseThrow(() -> new RuntimeException("유저정보가 없습니다"));
      reply.setMember(member);
    }
    Calendar calendar = Calendar.getInstance();
    Date currentDate = calendar.getTime();
    reply.setCreationTime(currentDate);

    reply.setCommunity(post.getCommunity());
    reply.setPost(post);
    replyRepository.save(reply);
    post.setReplyCount(post.getReplyCount() + 1);
    postRepository.save(post);
    return reply.getId();
  }

  // POST checkReplyPassword
  // 등록된 유저가 아닌 경우 댓글 수정전에 비밀번호 확인
  // reply_id, password
  public boolean checkReplyPassword(CheckReplyPasswordRequestDto checkReplyPasswordRequestDto) {
    Long replyId = checkReplyPasswordRequestDto.getReply_id();
    String password = checkReplyPasswordRequestDto.getPassword();
    Reply reply = replyRepository.findById(replyId)
        .orElseThrow(() -> new RuntimeException("댓글 정보가 없습니다"));
    if (passwordEncoder.matches(password, reply.getPassword())) {
      return true;
    } else {
      return false;
    }
  }

  // POST updateReply
  // 댓글 수정
  // reply, owner, code
  public void updateReply(String email, UpdateReplyRequestDto updateReplyRequestDto) {
    Reply reply = replyRepository.findById(updateReplyRequestDto.getReply_id())
        .orElseThrow(() -> new RuntimeException("댓글 정보가 없습니다"));
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      if (passwordEncoder.matches(updateReplyRequestDto.getPassword(), reply.getPassword())) {
        reply.setReply(updateReplyRequestDto.getReply());

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        reply.setModificationTime(currentDate);
        replyRepository.save(reply);
      } else {
        throw new RuntimeException("비밀번호가 일치하지 않습니다");
      }
    } else {
      // 유저일 경우
      if (!Objects.equals(reply.getMember().getEmail(), email)) {
        throw new RuntimeException("해당 댓글에 대한 권한이 없습니다");
      } else {
        reply.setReply(updateReplyRequestDto.getReply());

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        reply.setModificationTime(currentDate);
        replyRepository.save(reply);
      }
    }
  }

  // DELETE deleteReply
  // 댓글 삭제
  // accessToken, code
  public void deleteReply(String email, DeleteReplyRequestDto deleteReplyRequestDto) {
    Reply reply = replyRepository.findById(deleteReplyRequestDto.getReply_id())
        .orElseThrow(() -> new RuntimeException("댓글 정보가 없습니다"));
    Post post = reply.getPost();
    // 등록된 유저가 아닐 경우
    if (email.isEmpty()) {
      if (passwordEncoder.matches(deleteReplyRequestDto.getPassword(), reply.getPassword())) {
        replyRepository.delete(reply);
        post.setReplyCount(post.getReplyCount() - 1);
        postRepository.save(post);
      } else {
        throw new RuntimeException("비밀번호가 일치하지 않습니다");
      }
    } else {
      // 유저일 경우
      if (!Objects.equals(reply.getMember().getEmail(), email)) {
        throw new RuntimeException("해당 댓글에 대한 권한이 없습니다");
      } else {

        replyRepository.delete(reply);
        post.setReplyCount(post.getReplyCount() - 1);
        postRepository.save(post);
      }
    }
  }
}
