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

  private List<ReplyDto> items;
  private int currentPage;
  private int totalPages;
  private Integer prev;
  private Integer next;

  public static PostReplyResponseDto of(List<ReplyDto> replyDtoList, Page<Reply> replyPage) {
    Integer prev = (!replyPage.hasPrevious()) ? null : replyPage.getNumber();
    Integer next = (!replyPage.hasNext()) ? null : replyPage.getNumber() + 2;

    return PostReplyResponseDto.builder()
        .items(replyDtoList)
        .currentPage(replyPage.getNumber() + 1)
        .totalPages(replyPage.getTotalPages())
        .prev(prev)
        .next(next)
        .build();
  }
}
