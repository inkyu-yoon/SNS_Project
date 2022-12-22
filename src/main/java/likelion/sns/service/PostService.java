package likelion.sns.service;


import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.modify.PostModifyRequestDto;
import likelion.sns.domain.dto.read.PostDetailDto;
import likelion.sns.domain.dto.read.PostListDto;
import likelion.sns.domain.dto.write.PostWriteRequestDto;
import likelion.sns.domain.dto.write.PostWriteResponseDto;
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

    public Page<PostListDto> getPostList(Pageable pageable) throws SQLException {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable).map(post -> new PostListDto(post));
    }

    public PostDetailDto getPostById(Long id) throws SQLException {
        Post foundPost = postRepository.findById(id).orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND, id + "번 게시글은 존재하지 않습니다."));
        return new PostDetailDto(foundPost);
    }

    @Transactional
    public PostWriteResponseDto writePost(PostWriteRequestDto postWriteRequestDto, String userName) throws SQLException {
        User user = userRepository.findByUserName(userName).orElseThrow(()->new SNSAppException(ErrorCode.USERNAME_NOT_FOUND,userName+"에 해당하는 회원을 찾을 수 없습니다."));

        Post post = new Post(postWriteRequestDto.getTitle(), postWriteRequestDto.getBody(), user);

        return new PostWriteResponseDto(postRepository.save(post));

    }

    @Transactional
    public void modifyPost(PostModifyRequestDto postModifyRequestDto, Long postId,String requestUserName) throws SQLException {

        //유저가 존재하지 않음
        User requestUser = userRepository.findByUserName(requestUserName).orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND, requestUserName + "에 해당하는 회원을 찾을 수 없습니다."));

        //포스트가 존재하지 않음
        Post foundPost = postRepository.findById(postId).orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND, postId + "번 게시글이 존재하지 않습니다."));

        //작성자가 없음
        if (foundPost.getUser() == null) {
            throw new SNSAppException(ErrorCode.USERNAME_NOT_FOUND,"작성자가 존재하지 않습니다.");
        }

        log.info("role : {} ",requestUser.getRole().toString());

        //작성자와 유저가 일치하지 않음 (단 ADMIN이면 수정 가능함)
        User foundUser = foundPost.getUser();

        if (!requestUser.getRole().toString().equals(UserRole.ADMIN.toString()) && !foundUser.getUserName().equals(requestUserName)) {
            throw new SNSAppException(ErrorCode.INVALID_PERMISSION,"작성자와 수정 요청자가 일치하지 않습니다.");
        }

        String newTitle = postModifyRequestDto.getTitle();
        String newBody = postModifyRequestDto.getBody();

        foundPost.modifyPost(newTitle, newBody);

    }
}
