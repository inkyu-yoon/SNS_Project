package likelion.sns.controller;

import likelion.sns.domain.dto.read.PostListDto;
import likelion.sns.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @GetMapping("")
    public String searchList (Model model, Pageable pageable) throws SQLException {
        Page<PostListDto> posts = postService.getPostList(pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());
        return "posts/list";
    }

}
