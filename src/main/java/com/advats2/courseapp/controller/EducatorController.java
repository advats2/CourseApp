package com.advats2.courseapp.controller;

import com.advats2.courseapp.model.Course;
import com.advats2.courseapp.model.Educator;
import com.advats2.courseapp.model.Video;
import com.advats2.courseapp.model.repository.CourseRepository;
import com.advats2.courseapp.model.repository.UserRepository;
import jakarta.servlet.ServletContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/educator")
public class EducatorController {
    private UserRepository userRepository;
    private CourseRepository courseRepository;

    public EducatorController(UserRepository userRepository, CourseRepository courseRepository, ServletContext context) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("")
    public String educatorHome(Principal principal, Model model) {
        Optional<Educator> educatorOptional = userRepository.findEducator(principal.getName());
        if(!educatorOptional.isPresent()) {
            return "educator_pending";
        }
        Educator educator = educatorOptional.get();
        List<Course> courses = userRepository.getEducatorCourses(educator.getUsername());
        model.addAttribute("educator", educator);
        model.addAttribute("courses", courses);
        return "profile_educator";
    }

    @PostMapping("")
    public String educatorPost(Principal principal, @RequestParam(value = "foe", required = false) String foe, @RequestParam(value = "email", required = false) String email) {
        if(foe != null) {
            userRepository.addEducatorFOE(principal.getName(), foe);
        }
        if(email != null) {
            userRepository.addEducatorEmail(principal.getName(), email);
        }
        return "redirect:/educator";
    }

    @GetMapping("/newvideo")
    public String newVideo(@RequestParam("index") Integer index, Principal principal, Model model) {
        List<Course> courses = courseRepository.getAll();
        if(index < 0 || index >= courses.size()) {
            return "redirect:/error";
        }
        String cname = courses.get(index).getName();
        Educator educator = userRepository.findEducator(principal.getName()).get();
        if(!courseRepository.getTeachers(cname).contains(educator)) {
            return "redirect:/login";
        }
        model.addAttribute("index", index);
        return "new_video";
    }

    @PostMapping("/uploadvideo")
    public String newVideoPost(@RequestParam("index") Integer index, @RequestParam("title") String title, @RequestParam("description") String description, @RequestParam("videofile") MultipartFile video, @RequestParam("thumbnailfile") MultipartFile image, Principal principal) throws IOException {
        List<Course> courses = courseRepository.getAll();
        if(index < 0 || index >= courses.size()) {
            return "redirect:/error";
        }
        String cname = courses.get(index).getName();
        Educator educator = userRepository.findEducator(principal.getName()).get();
        if(!courseRepository.getTeachers(cname).contains(educator)) {
            return "redirect:/login";
        }
        String videoDir = "./upload/video";
        String imageDir = "./upload/thumbnail";
        Path videopath = Paths.get(videoDir);
        Path imagepath = Paths.get(imageDir);
        if(!Files.exists(videopath)) {
            Files.createDirectories(videopath);
        }
        if(!Files.exists(imagepath)) {
            Files.createDirectories(imagepath);
        }
        InputStream inputStream = video.getInputStream();
        Path filePath = videopath.resolve(StringUtils.cleanPath(video.getOriginalFilename()));
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        inputStream = image.getInputStream();
        filePath = imagepath.resolve(StringUtils.cleanPath(image.getOriginalFilename()));
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        courseRepository.addVideo(cname, StringUtils.cleanPath(video.getOriginalFilename()), StringUtils.cleanPath(image.getOriginalFilename()), description, title);
        return "redirect:/courses/course?index=" + index;
    }

    @GetMapping("/newblog")
    public String newBlog(@RequestParam("index") Integer index, Principal principal, Model model) {
        List<Course> courses = courseRepository.getAll();
        if(index < 0 || index >= courses.size()) {
            return "redirect:/error";
        }
        String cname = courses.get(index).getName();
        Educator educator = userRepository.findEducator(principal.getName()).get();
        if(!courseRepository.getTeachers(cname).contains(educator)) {
            return "redirect:/login";
        }
        model.addAttribute("index", index);
        return "new_blog";
    }

    @PostMapping("/uploadblog")
    public String newBlogPost(@RequestParam("index") Integer index, @RequestParam("title") String title, @RequestParam("content") String content, Principal principal) {
        List<Course> courses = courseRepository.getAll();
        if(index < 0 || index >= courses.size()) {
            return "redirect:/error";
        }
        String cname = courses.get(index).getName();
        Educator educator = userRepository.findEducator(principal.getName()).get();
        if(!courseRepository.getTeachers(cname).contains(educator)) {
            return "redirect:/login";
        }
        courseRepository.addBlog(cname, title, content);
        return "redirect:/courses/course?index=" + index;
    }
}
