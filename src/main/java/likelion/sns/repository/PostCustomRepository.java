package likelion.sns.repository;

import likelion.sns.domain.dto.post.read.PostListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostCustomRepository {
    Page<PostListDto> getPostLists(String searchCondition, Pageable pageable);
}
