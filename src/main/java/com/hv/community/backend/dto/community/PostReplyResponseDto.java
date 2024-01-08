package com.hv.community.backend.dto.community;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hv.community.backend.domain.community.Reply;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostReplyResponseDto {

  private Integer next;
  private Integer prev;
  private int totalPage;
  private int page;
  private int pageSize;
  private List<ReplyDto> items;


  public static PostReplyResponseDto of(List<ReplyDto> replyDtoList, Page<Reply> replyPage,
      int pageSize) {
    int currentPage = replyPage.getNumber() + 1;

    Integer prev = (!replyPage.hasPrevious()) ? null : currentPage - 1;
    Integer next = (!replyPage.hasNext()) ? null : currentPage + 1;

    return PostReplyResponseDto.builder()
        .next(next)
        .prev(prev)
        .totalPage(replyPage.getTotalPages())
        .page(currentPage)
        .pageSize(pageSize)
        .items(replyDtoList)
        .build();
  }
}
