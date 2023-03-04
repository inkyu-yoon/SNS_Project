package likelion.sns.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.util.StringUtils;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import likelion.sns.domain.dto.post.read.PostListDto;
import likelion.sns.domain.entity.QPost;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static likelion.sns.domain.entity.QPost.*;

@Repository
@RequiredArgsConstructor
public class PostCustomRepositoryImpl implements PostCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostListDto> getPostLists(String searchCondition, String keyword, Pageable pageable) {


        QueryResults<PostListDto> results = queryFactory.select(Projections.constructor(PostListDto.class, post))
                .from(post)
                .where(titleContains(searchCondition, keyword), userNameContains(searchCondition, keyword))
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        return new PageImpl<>(results.getResults(), pageable, results.getTotal());

    }

    private Predicate userNameContains(String searchCondition, String keyword) {
        if (searchCondition.equals("회원명 검색")) {
            return keyword == null ? null : post.user.userName.contains(keyword);
        } else {
            return null;
        }
    }

    private Predicate titleContains(String searchCondition, String keyword) {
        if (searchCondition.equals("제목 검색")) {
            return keyword == null ? null : post.title.contains(keyword);
        } else {
            return null;
        }
    }


}
