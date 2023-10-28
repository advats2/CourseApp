package com.advats2.courseapp.model.repository;

import com.advats2.courseapp.model.*;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseRepository {
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public CourseRepository(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    public List<Course> getAll() {
        List<Course> courses = jdbcTemplate.query("SELECT * FROM Course", new BeanPropertyRowMapper<>(Course.class));
        for(int i = 0; i < courses.size(); i++) {
            courses.get(i).setTeachers(getTeachers(courses.get(i).getName()));
            courses.get(i).setVideos(getVideos(courses.get(i).getName()));
            courses.get(i).setBlogs(getBlogs(courses.get(i).getName()));
        }
        return courses;
    }

    public List<Educator> getTeachers(String cname) {
        List<String> names = jdbcTemplate.queryForList("SELECT username FROM Teacher WHERE CName = ?", new Object[]{cname}, String.class);
        List<Educator> teachers = new ArrayList<>();
        for(int i = 0; i < names.size(); i++) {
            teachers.add(userRepository.findEducator(names.get(i)).get());
        }
        System.out.println(teachers);
        return teachers;
    }

    public List<Video> getVideos(String cname) {
        return jdbcTemplate.query("SELECT * FROM Video WHERE CName = ?", new Object[]{cname}, new BeanPropertyRowMapper<>(Video.class));
    }

    public List<Blog> getBlogs(String cname) {
        return jdbcTemplate.query("SELECT * FROM Blog WHERE CName = ?", new Object[]{cname}, new BeanPropertyRowMapper<>(Blog.class));
    }

    public Boolean isEnrolled(String cname, String sname) {
        List<String> rows = jdbcTemplate.queryForList("SELECT CName FROM Enrollment WHERE CName = ? and username = ?", new Object[]{cname,sname}, String.class);
        return !rows.isEmpty();
    }

    public void addPendingCourses(Course course, String username) {
        jdbcTemplate.update("INSERT INTO PendingCourses VALUES (?,?,0,?,?)",
                course.getName(), course.getDescription(),
                course.getPrice(), course.getCategory());
        jdbcTemplate.update("INSERT INTO PendingTeachers VALUES (?,?)", username, course.getName());
    }

    public List<Pair<String,String>> getPendingCourses() {
        List<Pair<String,String>> pairs = jdbcTemplate.query("SELECT * FROM PendingTeachers", new RowMapper<Pair<String,String>>() {
            @Override
            public Pair<String,String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Pair.of(rs.getString("username"),rs.getString("CName"));
            }
        });
        return pairs;
    }

    public void approveCourse(String cname) {
        List<Course> courses = jdbcTemplate.query("SELECT * FROM PendingCourses WHERE Name = ?", new Object[]{cname}, new BeanPropertyRowMapper<>(Course.class));
        Course course = courses.get(0);
        jdbcTemplate.update("INSERT INTO Course VALUES (?,?,?,?,?)",
                course.getName(), course.getDescription(), course.getRating(),
                course.getPrice(), course.getCategory());
        List<String> names = jdbcTemplate.queryForList("SELECT username FROM PendingTeachers WHERE CName = ?", new Object[]{cname}, String.class);
        for(int i = 0; i < names.size(); i++) {
            jdbcTemplate.update("INSERT INTO Teacher VALUES (?,?)", names.get(0), cname);
        }
        jdbcTemplate.update("DELETE FROM PendingTeachers WHERE CName = ?", cname);
        jdbcTemplate.update("DELETE FROM PendingCourses WHERE Name = ?", cname);
    }

    public void removeCourse(String cname) {
        jdbcTemplate.update("DELETE FROM Teacher WHERE CName = ?", cname);
        jdbcTemplate.update("DELETE FROM Comment WHERE CName = ? ORDER BY CommentID DESC", cname);
        jdbcTemplate.update("DELETE FROM Video WHERE CName = ?", cname);
        jdbcTemplate.update("DELETE FROM Blog WHERE CName = ?", cname);
        jdbcTemplate.update("DELETE FROM Teacher WHERE CName = ?", cname);
        jdbcTemplate.update("DELETE FROM Course WHERE Name = ?", cname);
    }

    public void addTeacher(String cname, String ename) {
        jdbcTemplate.update("INSERT INTO Teacher VALUES (?,?)", ename, cname);
    }

    public void addVideoBookmark(String sname, String cname, int id) {
        jdbcTemplate.update("INSERT IGNORE INTO VideoBookmarks VALUES (?,?,?)", id, cname, sname);
    }

    public void addBlogBookmark(String sname, String cname, int id) {
        jdbcTemplate.update("INSERT IGNORE INTO BlogBookmarks VALUES (?,?,?)", id, cname, sname);
    }

    public void addWishlist(String sname, String cname) {
        jdbcTemplate.update("INSERT IGNORE INTO Wishlist VALUES (?,?)", cname, sname);
    }

    public List<Comment> getRootComments(String cname, int id, int borv) {
        if(borv == 0) {
            List<Comment> comments = jdbcTemplate.query("SELECT * FROM Comment WHERE CName = ? and VideoNo = ? and ParentID IS NULL", new Object[]{cname, id}, new BeanPropertyRowMapper<>(Comment.class));
            return comments;
        }
        else {
            List<Comment> comments = jdbcTemplate.query("SELECT * FROM Comment WHERE CName = ? and BlogNo = ? and ParentID IS NULL", new Object[]{cname, id}, new BeanPropertyRowMapper<>(Comment.class));
            return comments;
        }
    }

    public List<Comment> getReplies(int pid) {
        List<Comment> replies = jdbcTemplate.query("SELECT * FROM Comment WHERE ParentID = ?", new Object[]{pid}, new BeanPropertyRowMapper<>(Comment.class));
        return replies;
    }

    public void incrementLikes(int cid) {
        jdbcTemplate.update("UPDATE Comment SET CommentLikes = CommentLikes + 1 WHERE CommentID = ?", cid);
    }

    public void addReply(String cname, int id, String sname, String cbody, int cid, int borv) {
        if(borv == 0) {
            jdbcTemplate.update("INSERT INTO Comment(PostedDate, Body, CommentLikes, Susername, VideoNo, CName, ParentID) VALUES (now(),?,0,?,?,?,?)", cbody, sname, id, cname, cid);
        }
        else {
            jdbcTemplate.update("INSERT INTO Comment(PostedDate, Body, CommentLikes, Susername, BlogNo, CName, ParentID) VALUES (now(),?,0,?,?,?,?)", cbody, sname, id, cname, cid);
        }
    }

    public void addComment(String cname, int id, String sname, String cbody, int borv) {
        if(borv == 0) {
            jdbcTemplate.update("INSERT INTO Comment(PostedDate, Body, CommentLikes, Susername, VideoNo, CName) VALUES (now(),?,0,?,?,?)", cbody, sname, id, cname);
        }
        else {
            jdbcTemplate.update("INSERT INTO Comment(PostedDate, Body, CommentLikes, Susername, BlogNo, CName) VALUES (now(),?,0,?,?,?)", cbody, sname, id, cname);
        }
    }

    public void rate(String cname, int id, String sname, int rating, int borv) {
        if(borv == 0) {
            jdbcTemplate.update("INSERT IGNORE INTO VideoRating(VideoNo,CName,username) VALUES (?,?,?)", id, cname, sname);
            jdbcTemplate.update("UPDATE VideoRating SET Rating = ? WHERE VideoNo = ? and CName = ? and username = ?", rating, id, cname, sname);
            BigDecimal avgrating = jdbcTemplate.queryForObject("SELECT AVG(Rating) FROM VideoRating WHERE VideoNo = ? and CName = ? and Rating > 0", new Object[]{id, cname}, BigDecimal.class);
            jdbcTemplate.update("UPDATE Video SET Rating = ? WHERE VideoID = ? and CName = ?", avgrating, id, cname);
        }
        else {
            jdbcTemplate.update("INSERT IGNORE INTO BlogRating(BlogNo,CName,username) VALUES (?,?,?)", id, cname, sname);
            jdbcTemplate.update("UPDATE BlogRating SET Rating = ? WHERE BlogNo = ? and CName = ? and username = ?", rating, id, cname, sname);
            BigDecimal avgrating = jdbcTemplate.queryForObject("SELECT AVG(Rating) FROM BlogRating WHERE BlogNo = ? and CName = ? and Rating > 0", new Object[]{id, cname}, BigDecimal.class);
            jdbcTemplate.update("UPDATE Blog SET Rating = ? WHERE BlogID = ? and CName = ?", avgrating, id, cname);
        }
    }

    public void incrementViews(String cname, int id, int borv) {
        if(borv == 0) {
            jdbcTemplate.update("UPDATE Video SET Views = Views + 1 WHERE VideoID = ? and CName = ?", id, cname);
        }
        else {
            jdbcTemplate.update("UPDATE Blog SET Views = Views + 1 WHERE BlogID = ? and CName = ?", id, cname);
        }
    }

    public void addVideo(String cname, String videopath, String imagepath, String descr, String title) {
        Integer id = Integer.valueOf(1);
        Optional<Integer> oid = jdbcTemplate.queryForObject("SELECT MAX(VideoID) FROM Video WHERE CName = ?", new Object[]{cname}, Optional.class);
        if(oid != null && oid.isPresent()) {
            id = oid.get() + 1;
        }
        jdbcTemplate.update("INSERT INTO Video VALUES (?,?,?,0,?,0,?,?,now())", id.intValue(), cname, imagepath, videopath, descr, title);
    }

    public void addBlog(String cname, String title, String content) {
        Integer id = Integer.valueOf(1);
        Optional<Integer> oid = jdbcTemplate.queryForObject("SELECT MAX(BlogID) FROM Blog WHERE CName = ?", new Object[]{cname}, Optional.class);
        if(oid != null && oid.isPresent()) {
            id = oid.get() + 1;
        }
        jdbcTemplate.update("INSERT INTO Blog VALUES (?,?,0,now(),0,?,?)", id.intValue(), cname, content, title);
    }

    public Optional<Blog> findBlog(String cname, int id) {
        List<Blog> blogs = jdbcTemplate.query("SELECT * FROM Blog WHERE BlogID = ? and CName = ?", new Object[]{id, cname}, new BeanPropertyRowMapper<>(Blog.class));
        Optional<Blog> blogOptional = blogs.isEmpty() ? Optional.empty() : Optional.of(blogs.get(0));
        return blogOptional;
    }

    public Optional<Video> findVideo(String cname, int id) {
        List<Video> videos = jdbcTemplate.query("SELECT * FROM Video WHERE VideoID = ? and CName = ?", new Object[]{id, cname}, new BeanPropertyRowMapper<>(Video.class));
        Optional<Video> videoOptional = videos.isEmpty() ? Optional.empty() : Optional.of(videos.get(0));
        return videoOptional;
    }
}
