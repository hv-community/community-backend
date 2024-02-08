package com.hv.community.backend.service.community;

import com.hv.community.backend.domain.community.Community;
import com.hv.community.backend.domain.community.Post;
import com.hv.community.backend.domain.community.Reply;
import com.hv.community.backend.domain.member.Member;
import com.hv.community.backend.dto.community.CommunityDto;
import com.hv.community.backend.dto.community.CommunityListResponseDto;
import com.hv.community.backend.dto.community.IdResponseDto;
import com.hv.community.backend.dto.community.PostCreateRequestDto;
import com.hv.community.backend.dto.community.PostDetailResponseDto;
import com.hv.community.backend.dto.community.PostDto;
import com.hv.community.backend.dto.community.PostListResponseDto;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommunityService {

  private final CommunityRepository communityRepository;
  private final PostRepository postRepository;
  private final ReplyRepository replyRepository;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  private static final String COMMUNITY_PAGE_INVALID = "COMMUNITY:PAGE_INVALID";
  private static final String COMMUNITY_COMMUNITY_INVALID = "COMMUNITY:COMMUNITY_INVALID";
  private static final String COMMUNITY_POST_INVALID = "COMMUNITY:POST_INVALID";
  private static final String COMMUNITY_REPLY_INVALID = "COMMUNITY:REPLY_INVALID";
  private static final String COMMUNITY_PASSWORD_INVALID = "COMMUNITY:PASSWORD_INVALID";
  private static final String COMMUNITY_PERMISSION_INVALID = "COMMUNITY:PERMISSION_INVALID";

  public CommunityListResponseDto communityListV1(Integer page, Integer pageSize) {
    Pageable pageable = PageRequest.of(page - 1, pageSize);

    Page<Community> communityPage = communityRepository.findAll(pageable);
    validatePage(page, communityPage);

    int currentPage = communityPage.getNumber() + 1;
    Integer next = (!communityPage.hasNext()) ? null : currentPage + 1;
    Integer prev = (!communityPage.hasPrevious()) ? null : currentPage - 1;

    List<CommunityDto> communityDtoList = communityPage.stream()
        .map(Community::buildCommunityDto).toList();

    return CommunityListResponseDto.builder()
        .next(next)
        .prev(prev)
        .totalPage(communityPage.getTotalPages())
        .page(currentPage)
        .pageSize(pageSize)
        .items(communityDtoList)
        .build();
  }

  public CommunityDto communityDetailV1(Long communityId) {
    Community community = communityRepository.findById(communityId)
        .orElseThrow(() -> new CommunityException(COMMUNITY_COMMUNITY_INVALID));
    return community.buildCommunityDto();
  }

  public PostListResponseDto postListV1(Long communityId, Integer page, Integer pageSize) {
    Community community = communityRepository.findById(communityId)
        .orElseThrow(() -> new CommunityException(COMMUNITY_COMMUNITY_INVALID));

    Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
    Page<Post> postPage = postRepository.findByCommunity(community, pageable);
    validatePage(page, postPage);

    int currentPage = postPage.getNumber() + 1;
    Integer next = (!postPage.hasNext()) ? null : currentPage + 1;
    Integer prev = (!postPage.hasPrevious()) ? null : currentPage - 1;

    List<PostDto> postDtoList = postPage.stream().map(Post::buildPostDto).toList();

    return PostListResponseDto.builder()
        .next(next)
        .prev(prev)
        .totalPage(postPage.getTotalPages())
        .page(page)
        .pageSize(pageSize)
        .items(postDtoList)
        .build();
  }

  public PostDetailResponseDto postDetailV1(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(COMMUNITY_POST_INVALID));
    Long previousPostId = post.findPreviousPostId(postRepository);
    Long nextPostId = post.findNextPostId(postRepository);

    return post.buildPostDetailResponseDto(previousPostId, nextPostId);
  }

  public PostReplyResponseDto postReplyV1(Long postId, Integer page, Integer pageSize) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(COMMUNITY_POST_INVALID));
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Reply> replyPage = replyRepository.findPageByPost(post, pageable);
    validatePage(page, replyPage);

    int currentPage = replyPage.getNumber() + 1;
    Integer next = (!replyPage.hasNext()) ? null : currentPage + 1;
    Integer prev = (!replyPage.hasPrevious()) ? null : currentPage - 1;

    List<ReplyDto> replyDtoList = replyPage.stream().map(Reply::buildReplyDto).toList();

    return PostReplyResponseDto.builder()
        .next(next)
        .prev(prev)
        .totalPage(replyPage.getTotalPages())
        .page(page)
        .pageSize(pageSize)
        .items(replyDtoList)
        .build();
  }

  public IdResponseDto postCreateV1(String email, Long communityId,
      PostCreateRequestDto postCreateRequestDto) {
    Community community = communityRepository.findById(communityId)
        .orElseThrow(() -> new CommunityException(COMMUNITY_COMMUNITY_INVALID));

    Member member = null;
    if (email != null) {
      member = memberRepository.findByEmail(email)
          .orElseThrow(() -> new MemberException("MEMBER:MEMBER_UNREGISTERED"));
    }
    String password =
        (member == null) ? passwordEncoder.encode(postCreateRequestDto.getPassword()) : null;
    Post post = community.createPost(postCreateRequestDto, password, member);

    postRepository.save(post);
    return post.buildIdResponseDto();
  }

  public boolean postCheckPasswordV1(Post post, Long postId, String password) {
    // postId로 post를 가져올 때
    if (post == null) {
      post = postRepository.findById(postId)
          .orElseThrow(() -> new CommunityException(COMMUNITY_POST_INVALID));
    }
    return post.checkPassword(passwordEncoder, password);
  }

  public void postUpdateV1(String email, Long postId, PostUpdateRequestDto postUpdateRequestDto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(COMMUNITY_POST_INVALID));
    // 멤버요청일 경우 글작성자가 맞는지 확인
    if (email != null) {
      if (post.checkMember(email)) {
        post.editPost(postUpdateRequestDto);
        postRepository.save(post);
        return;
      }
      throw new CommunityException(COMMUNITY_PERMISSION_INVALID);
    }

    // 등록된 유저가 아닐 경우
    // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
    if (postCheckPasswordV1(post, null, postUpdateRequestDto.getPassword())) {
      post.editPost(postUpdateRequestDto);
      postRepository.save(post);
      return;
    }
    throw new CommunityException(COMMUNITY_PASSWORD_INVALID);
  }


  public void postDeleteV1(String email, Long postId, String password) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(COMMUNITY_POST_INVALID));
    // 멤버요청일 경우 글작성자가 맞는지 확인
    if (email != null) {
      if (post.checkMember(email)) {
        replyRepository.deleteByPost(post);
        postRepository.delete(post);
        return;
      }
      throw new CommunityException(COMMUNITY_PERMISSION_INVALID);
    }

    // 등록된 유저가 아닐 경우
    // 게시물에 비밀번호가없다? > 유저가쓴글 > 권한없음
    if (postCheckPasswordV1(post, null, password)) {
      replyRepository.deleteByPost(post);
      postRepository.delete(post);
      return;
    }
    throw new CommunityException(COMMUNITY_PASSWORD_INVALID);
  }

  public IdResponseDto replyCreateV1(String email, Long postId,
      ReplyCreateRequestDto replyCreateRequestDto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CommunityException(COMMUNITY_POST_INVALID));

    Member member = null;
    if (email != null) {
      member = memberRepository.findByEmail(email)
          .orElseThrow(() -> new MemberException("MEMBER:MEMBER_UNREGISTERED"));
    }

    String password =
        (member == null) ? passwordEncoder.encode(replyCreateRequestDto.getPassword()) : null;
    Reply reply = post.createReply(replyCreateRequestDto, password, member);
    replyRepository.save(reply);
    postRepository.save(post);

    return reply.buildIdResponseDto();
  }

  public boolean replyCheckPasswordV1(Reply reply, Long replyId, String password) {
    if (reply == null) {
      reply = replyRepository.findById(replyId)
          .orElseThrow(() -> new CommunityException(COMMUNITY_REPLY_INVALID));
    }
    return reply.checkPassword(passwordEncoder, password);
  }

  public void replyUpdateV1(String email, Long replyId,
      ReplyUpdateRequestDto replyUpdateRequestDto) {
    Reply reply = replyRepository.findById(replyId)
        .orElseThrow(() -> new CommunityException(COMMUNITY_REPLY_INVALID));
    // 멤버요청일 경우 댓글작성자가 맞는지 확인
    if (email != null) {
      if (reply.checkMember(email)) {
        reply.editReply(replyUpdateRequestDto);
        replyRepository.save(reply);
        return;
      }
      throw new CommunityException(COMMUNITY_PERMISSION_INVALID);
    }

    // 등록된 유저가 아닐 경우
    if (replyCheckPasswordV1(reply, null, replyUpdateRequestDto.getPassword())) {
      reply.editReply(replyUpdateRequestDto);
      replyRepository.save(reply);
      return;
    }
    throw new CommunityException(COMMUNITY_PASSWORD_INVALID);
  }

  public void replyDeleteV1(String email, Long replyId, String password) {
    Reply reply = replyRepository.findById(replyId)
        .orElseThrow(() -> new CommunityException(COMMUNITY_REPLY_INVALID));
    // 멤버요청일 경우 댓글작성자가 맞는지 확인
    if (email != null) {
      if (reply.checkMember(email)) {
        reply.deleteReply();
        replyRepository.delete(reply);
        return;
      }
      throw new CommunityException(COMMUNITY_PERMISSION_INVALID);
    }

    // 등록된 유저가 아닐 경우
    if (replyCheckPasswordV1(reply, null, password)) {
      reply.deleteReply();
      replyRepository.delete(reply);
      return;
    }
    throw new CommunityException(COMMUNITY_PASSWORD_INVALID);
  }

  private void validatePage(Integer page, Page<?> pageList) {
    if ((page - 1 > pageList.getTotalPages() && !pageList.isEmpty())
        || (page > 1 && pageList.isEmpty())) {
      // 초과 에러 리턴
      throw new CommunityException(COMMUNITY_PAGE_INVALID);
    }
  }
}
