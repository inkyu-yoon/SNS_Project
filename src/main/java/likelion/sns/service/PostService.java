package likelion.sns.service;


import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.post.modify.PostModifyRequestDto;
import likelion.sns.domain.dto.post.read.PostDetailDto;
import likelion.sns.domain.dto.post.read.PostListDto;
import likelion.sns.domain.dto.post.write.PostWriteRequestDto;
import likelion.sns.domain.dto.post.write.PostWriteResponseDto;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;


    /**
     * 게시글 리스트 조회
     */
    public Page<PostListDto> getPostList(Pageable pageable) throws SQLException {
        return postRepository.findByOrderByCreatedAtDesc(pageable).map(post -> new PostListDto(post));
    }

    /**
     * 게시글 단건 조회
     */
    public PostDetailDto getPostById(Long postId) throws SQLException {
        // post 유효성 검사하고 찾아오기
        Post foundPost = postValid(postId);

        return new PostDetailDto(foundPost);
    }

    /**
     * 게시글 작성
     */
    @Transactional
    public PostWriteResponseDto writePost(PostWriteRequestDto requestDto, String requestUserName) throws SQLException {
        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        // 정적 팩토리 메서드 방식
        Post post = Post.writePost(requestDto.getTitle(), requestDto.getBody(), requestUser);

        return new PostWriteResponseDto(postRepository.save(post));

    }

    /**
     * 게시글 수정
     */
    @Transactional
    public void modifyPost(PostModifyRequestDto requestDto, Long postId, String requestUserName) throws SQLException {

        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        // post 유효성 검사하고 찾아오기
        Post foundPost = postValid(postId);

        UserRole requestUserRole = requestUser.getRole();
        String author = foundPost.getUser().getUserName();
        log.info("게시글 수정 요청자 ROLE = {} 게시글 작성자 author = {}", requestUserRole, author);

        // 작성자와 요청자 유효성 검사
        checkAuth(requestUserName, author, requestUserRole);

        foundPost.modifyPost(requestDto.getTitle(), requestDto.getBody());
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long postId, String requestUserName) throws SQLException {
        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        // post 유효성 검사하고 찾아오기
        Post foundPost = postValid(postId);


        UserRole requestUserRole = requestUser.getRole();
        String author = foundPost.getUser().getUserName();
        log.info("게시글 수정 요청자 ROLE = {} 게시글 작성자 author = {}", requestUserRole, author);

        // 작성자와 요청자 유효성 검사
        checkAuth(requestUserName, author, requestUserRole);

        //soft delete 적용
        postRepository.delete(foundPost);

    }

    /**
     * 마이 피드 기능 (작성한 글 페이징)
     */
    public Page<PostListDto> getMyPosts(String requestUserName, Pageable pageable) {

        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        return postRepository.findByUser_IdOrderByCreatedAtDesc(requestUser.getId(), pageable).map(post -> new PostListDto(post));

    }

    /**
     * UI 용 메서드 (게시글 검색)
     * 게시글 리스트 조회 (제목으로 검색)
     */
    public Page<PostListDto> getPosts(String condition,String keyword, Pageable pageable) throws SQLException {
        return postRepository.getPostLists(condition,keyword, pageable);
    }

    /**
     * UI 용 메서드 (게시글 검색)
     * 게시글 리스트 조회 (회원명 으로 검색)
     */
    public Page<PostListDto> getPostsByUserName(String userName, Pageable pageable) throws SQLException {
        User requestUser = userRepository.findByUserName(userName).orElse(null);

        Page<PostListDto> posts = null;

        if (requestUser != null) {
            Long requestUserId = requestUser.getId();
            log.info("마이 피드 조회 요청 userId : {} ", requestUserId);
            posts = postRepository.findByUser_IdOrderByCreatedAtDesc(requestUserId, pageable).map(post -> new PostListDto(post));
        }

        return posts;
    }

    /*
    아래 메서드는 유효성 검사 및 중복 메서드 정리
     */

    /**
     * 해당하는 회원이 없을 시, 예외 처리
     */
    public User userValid(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));
    }

    /**
     * 해당하는 게시글이 없을 시, 그리고 deletedAt 데이터가 있을 시 예외 처리
     */
    public Post postValid(Long postId) {

        //DB에 저장되어 있는 게시글이 없는 경우
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND));
        log.info("삭제 날짜 {} ",foundPost.getDeletedAt());
        // deletedAt 에 데이터가 채워져서 삭제 처리가 된 경우
        if (foundPost.getDeletedAt() != null) {
            throw new SNSAppException(ErrorCode.POST_NOT_FOUND);
        }

        return foundPost;
    }

    /**
     * 권한이 ADMIN 이 아니면서 요청자와 작성자가 다른 경우
     */
    public void checkAuth(String requestUserName, String author, UserRole requestUserRole) {
        if (!requestUserRole.equals(UserRole.ROLE_ADMIN) && !author.equals(requestUserName)) {
            throw new SNSAppException(ErrorCode.USER_NOT_MATCH);
        }
    }
}
