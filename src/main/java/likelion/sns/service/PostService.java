package likelion.sns.service;


import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.read.PostDetailDto;
import likelion.sns.domain.dto.read.PostListDto;
import likelion.sns.domain.dto.write.PostWriteResponseDto;
import likelion.sns.domain.dto.write.PostWriteRequestDto;
import likelion.sns.domain.entity.Post;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Page<PostListDto> getPostList(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable).map(post -> new PostListDto(post));
    }

    public PostDetailDto getPostById(Long id) {
        Post foundPost = postRepository.findById(id).orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND, id + "번 게시글은 존재하지 않습니다."));
        return new PostDetailDto(foundPost);
    }


}
