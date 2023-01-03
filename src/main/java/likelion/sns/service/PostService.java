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
        return postRepository.findAllByOrderByCreatedAtDesc(pageable).map(post -> new PostListDto(post));
    }

    /**
     * 게시글 단건 조회
     */
    public PostDetailDto getPostById(Long id) throws SQLException {
        // id에 해당하는 포스트 존재하지 않을 시 예외 처리
        Post foundPost = postRepository.findById(id).orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND, id + "번 게시글은 존재하지 않습니다."));
        return new PostDetailDto(foundPost);
    }

    /**
     * 게시글 작성
     */
    @Transactional
    public PostWriteResponseDto writePost(PostWriteRequestDto requestDto, String requestUserName) throws SQLException {
        // 해당하는 회원이 없을 시, 예외 처리
        User requestUser = userRepository.findByUserName(requestUserName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND, requestUserName + "에 해당하는 회원을 찾을 수 없습니다."));

        Post post = new Post(requestDto.getTitle(), requestDto.getBody(), requestUser);

        return new PostWriteResponseDto(postRepository.save(post));

    }

    /**
     * 게시글 수정
     */
    @Transactional
    public void modifyPost(PostModifyRequestDto requestDto, Long postId, String requestUserName) throws SQLException {

        //유저가 존재하지 않음
        User requestUser = userRepository.findByUserName(requestUserName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND, requestUserName + "에 해당하는 회원을 찾을 수 없습니다."));

        //포스트가 존재하지 않음
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND, postId + "번 게시글이 존재하지 않습니다."));


        User foundUser = foundPost.getUser();

        UserRole requestUserRole = requestUser.getRole();
        String userName = foundUser.getUserName();
        log.info("게시글 수정 요청자 ROLE = {}", requestUserRole);
        log.info("게시글 작성자 userName = {}", userName);

        //작성자와 유저가 일치하지 않음 (단 ADMIN이면 수정 가능함)
        if (!requestUserRole.equals(UserRole.ROLE_ADMIN) && !userName.equals(requestUserName)) {
            throw new SNSAppException(ErrorCode.USER_NOT_MATCH);
        }

        String newTitle = requestDto.getTitle();
        String newBody = requestDto.getBody();
        log.info("게시글 수정 요청 제목 = {} , 내용 = {}", newTitle,newBody);

        foundPost.modifyPost(newTitle, newBody);
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long postId, String requestUserName) throws SQLException {
        //유저가 존재하지 않음
        User requestUser = userRepository.findByUserName(requestUserName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND, requestUserName + "에 해당하는 회원을 찾을 수 없습니다."));

        //포스트가 존재하지 않음
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND, postId + "번 게시글이 존재하지 않습니다."));

        User foundUser = foundPost.getUser();

        UserRole requestUserRole = requestUser.getRole();
        String userName = foundUser.getUserName();
        log.info("게시글 삭제 요청자 ROLE = {}", requestUserRole);
        log.info("게시글 작성자 userName = {}", userName);

        //작성자와 유저가 일치하지 않음 (단 ADMIN이면 삭제 가능함)
        if (!requestUserRole.equals(UserRole.ROLE_ADMIN) && !userName.equals(requestUserName)) {
            throw new SNSAppException(ErrorCode.USER_NOT_MATCH);
        }

        postRepository.delete(foundPost);
    }
}
