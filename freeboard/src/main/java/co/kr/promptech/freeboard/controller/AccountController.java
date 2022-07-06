package co.kr.promptech.freeboard.controller;

import co.kr.promptech.freeboard.dto.AccountDTO;
import co.kr.promptech.freeboard.dto.ArticleSummaryDTO;
import co.kr.promptech.freeboard.dto.CommentDTO;
import co.kr.promptech.freeboard.model.Account;
import co.kr.promptech.freeboard.service.AccountService;
import co.kr.promptech.freeboard.service.ArticleService;
import co.kr.promptech.freeboard.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    private final ArticleService articleService;

    private final CommentService commentService;

    @GetMapping("/login")
    public String login(HttpSession session){
        return "pages/accounts/login";
    }

    @GetMapping("/login/success")
    public String loginSuccess(HttpSession session, Principal principal){
        Account account = accountService.findAccountByUsername(principal.getName());
        session.setAttribute("account", account);
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signupForm(Model model){
        model.addAttribute("accountDTO", new AccountDTO());
        return "pages/accounts/new";
    }

    @PostMapping("/signup")
    public String post(@Validated AccountDTO accountDTO, BindingResult result){
        Account account = accountService.findAccountByUsername(accountDTO.getUsername());
        if(account != null){
            result.rejectValue("username", "error.username", "Username already in use");
        }
        if(result.hasErrors()){
            return "pages/accounts/new";
        }
        accountService.save(accountDTO);
        return "redirect:/login";
    }

    @GetMapping("/accounts")
    public String show(Model model, HttpSession httpSession){
        Account account = (Account) httpSession.getAttribute("account");
        List<ArticleSummaryDTO> articles = articleService.findAllByAccount(account);
        List<CommentDTO> comments = commentService.findByAccount(account);
        model.addAttribute("articles", articles);
        model.addAttribute("comments", comments);
        return "pages/accounts/show";
    }

    @GetMapping("/accounts/articles")
    public String showArticles(Model model, HttpSession httpSession, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size){
        Account account = (Account) httpSession.getAttribute("account");
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(5);

        Pageable pageable = PageRequest.of(currentPage-1, pageSize);
        Page<ArticleSummaryDTO> articlePage = articleService.findAllByAccountByPaginated(account, pageable);
        model.addAttribute("articles", articlePage);
        int totalPages = articlePage.getTotalPages();
        if(totalPages > 0) {
            List<Integer> pageNums = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNums", pageNums);
        }
        return "pages/accounts/articles";
    }

    @GetMapping("/accounts/comments")
    public String showComments(Model model, HttpSession httpSession, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size){
        Account account = (Account) httpSession.getAttribute("account");
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(5);

        Pageable pageable = PageRequest.of(currentPage-1, pageSize);
        Page<CommentDTO> commentPage = commentService.findAllByAccountByPaginated(account, pageable);
        model.addAttribute("comments", commentPage);
        int totalPages = commentPage.getTotalPages();
        if(totalPages > 0) {
            List<Integer> pageNums = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNums", pageNums);
        }
        return "pages/accounts/comments";
    }
}