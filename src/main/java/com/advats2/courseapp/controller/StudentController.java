package com.advats2.courseapp.controller;

import com.advats2.courseapp.model.Blog;
import com.advats2.courseapp.model.Course;
import com.advats2.courseapp.model.Student;
import com.advats2.courseapp.model.Video;
import com.advats2.courseapp.model.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final UserRepository userRepository;

    public StudentController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @GetMapping("")
    public String studentHome(Model model, Principal principal) {
        List<Course> courses = userRepository.getStudentCourses(principal.getName());
        List<Course> wcourses = userRepository.getWishlistCourses(principal.getName());
        List<Video> bvs = userRepository.getBookmarkedVideos(principal.getName());
        List<Blog> bbs = userRepository.getBookmarkedBlogs(principal.getName());
        Student student = userRepository.findStudent(principal.getName()).get();
        model.addAttribute("student", student);
        model.addAttribute("courses", courses);
        model.addAttribute("wcourses", wcourses);
        model.addAttribute("bvs", bvs);
        model.addAttribute("bbs", bbs);
        return "profile_student";
    }

    @PostMapping("")
    public String educatorPost(Principal principal, @RequestParam(value = "pno", required = false) BigDecimal pno, @RequestParam(value = "email", required = false) String email) {
        if(pno != null) {
            userRepository.addStudentPno(principal.getName(), pno);
        }
        if(email != null) {
            userRepository.addStudentEmail(principal.getName(), email);
        }
        return "redirect:/student";
    }
}
