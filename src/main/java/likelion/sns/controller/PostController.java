package likelion.sns.controller;

import likelion.sns.domain.dto.read.PostDetailDto;
import likelion.sns.domain.dto.read.PostListDto;
import likelion.sns.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
@ApiIgnore
public class PostController {

    private final PostService postService;

    /**
     * 게시글 리스트 (페이지)
     **/
    @GetMapping("")
    public String searchList(Model model, Pageable pageable, HttpServletRequest request) throws SQLException {
        Page<PostListDto> posts = postService.getPostList(pageable);
        HttpSession session = request.getSession(true);
        if (session.getAttribute("userName") != null) {
            model.addAttribute("loginUserName", session.getAttribute("userName"));
        }
        model.addAttribute("posts", posts);
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());
        return "posts/list";
    }

    /**
     * 게시글 상세 페이지
     **/
    @GetMapping("/{postId}")
    public String showDetail(@PathVariable(name = "postId") Long postId, Model model) throws SQLException {
        PostDetailDto post = postService.getPostById(postId);
        model.addAttribute("post", post);
        return "posts/details";
    }

    /**
     * 게시글 작성
     **/
    @GetMapping("/write")
    public String writePost() {
        return "posts/write";
    }

    /**
     * 게시글 수정
     **/
    @GetMapping("/modify/{postId}")
    public String modifyPost(@PathVariable(name = "postId") Long postId, Model model) throws SQLException {
        PostDetailDto post = postService.getPostById(postId);
        model.addAttribute("post", post);
        return "posts/modify";
    }


}
