package com.advats2.courseapp.model.repository;

import com.advats2.courseapp.config.MyUserDetails;
import com.advats2.courseapp.model.*;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public Optional<User> findUser(String username) {
        List<User> users =  jdbcTemplate.query("SELECT * from User WHERE username = ?", new Object[]{username}, new BeanPropertyRowMapper<>(User.class));
        Optional<User> userOptional = users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        System.out.println("got");
        return userOptional;
    }

    public Optional<Admin> findAdmin(String username) {
        List<Admin> admins = jdbcTemplate.query("SELECT * from Admin WHERE username = ?", new Object[]{username}, new BeanPropertyRowMapper<>(Admin.class));
        Optional<Admin> adminOptional = admins.isEmpty() ? Optional.empty() : Optional.of(admins.get(0));
        if(adminOptional.isPresent()) {
            adminOptional.get().setRole("ADMIN");
        }
        return adminOptional;
    }

    public Optional<Educator> findEducator(String username) {
        List<Educator> educators = jdbcTemplate.query("SELECT * from Educator WHERE username = ?", new Object[]{username}, new BeanPropertyRowMapper<>(Educator.class));
        Optional<Educator> educatorOptional = educators.isEmpty() ? Optional.empty() : Optional.of(educators.get(0));
        if(educatorOptional.isPresent()) {
            educatorOptional.get().setRole("EDUCATOR");
            educatorOptional.get().setEmails(getEducatorEmails(educatorOptional.get().getUsername()));
            educatorOptional.get().setFields(getEducatorFields(educatorOptional.get().getUsername()));
        }
        return educatorOptional;
    }

    public Optional<Student> findStudent(String username) {
        List<Student> students = jdbcTemplate.query("SELECT * from Student WHERE username = ?", new Object[]{username}, new BeanPropertyRowMapper<>(Student.class));
        Optional<Student> studentOptional = students.isEmpty() ? Optional.empty() : Optional.of(students.get(0));
        if(studentOptional.isPresent()) {
            studentOptional.get().setRole("STUDENT");
            studentOptional.get().setEmails(getStudentEmails(studentOptional.get().getUsername()));
            studentOptional.get().setPhoneNos(getStudentPhNos(studentOptional.get().getUsername()));
        }
        return studentOptional;
    }

    public void save(User user) {
        jdbcTemplate.update("INSERT INTO User VALUES (?,?,?,?,?)", user.getUsername(),user.getFName(),user.getLName(),user.getPassword(),user.getRole());
    }

    public void addAdmin(Admin admin) {
        jdbcTemplate.update("INSERT INTO Admin VALUES (?,?,?,now(),?)",admin.getUsername(),admin.getFName(),admin.getLName(),admin.getPassword());
    }

    public void addPendingEducator(Educator educator) {
        jdbcTemplate.update("INSERT INTO PendingEducators VALUES (?,?,?,?,?,?,?,?,?,?)",
                educator.getUsername(),educator.getGender(),educator.getFName(),
                educator.getLName(),educator.getDegree(),educator.getYear(),
                educator.getUniversity(),educator.getAbout(),educator.getPassword(),educator.getAdminUserName());
    }

    public void approveEducator(String username) {
        System.out.println(username);
        List<Educator> educators = jdbcTemplate.query("SELECT * from PendingEducators WHERE username = ?", new Object[]{username}, new BeanPropertyRowMapper<>(Educator.class));
        Optional<Educator> educatorOptional = educators.isEmpty() ? Optional.empty() : Optional.of(educators.get(0));
        if(!educatorOptional.isPresent()) {
            System.out.println("not");
        }
        Educator educator = educatorOptional.get();
        jdbcTemplate.update("INSERT INTO Educator VALUES (?,?,?,?,?,?,?,?,?,?)",
                educator.getUsername(),educator.getGender(),educator.getFName(),
                educator.getLName(),educator.getDegree(),educator.getYear(),
                educator.getUniversity(),educator.getAbout(),educator.getPassword(),educator.getAdminUserName());
        jdbcTemplate.update("DELETE FROM PendingEducators WHERE username=?", educator.getUsername());
    }

    public void addStudent(Student student) {
        jdbcTemplate.update("INSERT INTO Student VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                student.getUsername(),student.getFName(),student.getLName(),student.getPassword(),
                student.getAge(),student.getCity(),student.getState(),student.getPincode(),
                student.getSpecialisation(),student.getDegree(),student.getCollege(),student.getDOB());
    }

    public List<Educator> getPendingEducators(String adminName) {
        return jdbcTemplate.query("SELECT * from PendingEducators WHERE AdminUserName=?", new Object[]{adminName}, new BeanPropertyRowMapper<>(Educator.class));
    }

    public List<Course> getEducatorCourses(String username) {
        List<String> names = jdbcTemplate.queryForList("SELECT CName FROM Teacher WHERE username = ?", new Object[]{username}, String.class);
        List<Course> courses = new ArrayList<>();
        for(int i = 0; i < names.size(); i++) {
            List<Course> course = jdbcTemplate.query("SELECT * FROM Course WHERE Name = ?", new Object[]{names.get(i)}, new BeanPropertyRowMapper<>(Course.class));
            if(!course.isEmpty()) {
                courses.add(course.get(0));
            }
        }
        return courses;
    }

    public List<Course> getStudentCourses(String username) {
        List<String> names = jdbcTemplate.queryForList("SELECT CName FROM Enrollment WHERE username = ?", new Object[]{username}, String.class);
        List<Course> courses = new ArrayList<>();
        for(int i = 0; i < names.size(); i++) {
            List<Course> course = jdbcTemplate.query("SELECT * FROM Course WHERE Name = ?", new Object[]{names.get(i)}, new BeanPropertyRowMapper<>(Course.class));
            if(!course.isEmpty()) {
                courses.add(course.get(0));
            }
        }
        return courses;
    }

    public List<Educator> getAllEducators() {
        List<Educator> educators = jdbcTemplate.query("SELECT * FROM Educator", new BeanPropertyRowMapper<>(Educator.class));
        for(int i = 0; i < educators.size(); i++) {
            educators.get(i).setRole("EDUCATOR");
            educators.get(i).setEmails(getEducatorEmails(educators.get(i).getUsername()));
            educators.get(i).setFields(getEducatorFields(educators.get(i).getUsername()));
        }
        return educators;
    }

    public List<String> getEducatorEmails(String username) {
        return jdbcTemplate.queryForList("SELECT EmailID FROM EducatorEmailIDs WHERE username = ?", new Object[]{username}, String.class);
    }

    public List<String> getEducatorFields(String username) {
        return jdbcTemplate.queryForList("SELECT FieldOfExpertise FROM EducatorFieldOExpertise WHERE username = ?", new Object[]{username}, String.class);
    }

    public List<String> getStudentEmails(String username) {
        return jdbcTemplate.queryForList("SELECT EmailID FROM StudentEmailIDs WHERE username = ?", new Object[]{username}, String.class);
    }

    public List<Long> getStudentPhNos(String username) {
        return jdbcTemplate.queryForList("SELECT PhoneNo FROM StudentPhNos WHERE username = ?", new Object[]{username}, Long.class);
    }

    public void addTransaction(String sname, Integer id, String cname) {
        int price = jdbcTemplate.queryForObject("SELECT Price FROM Course WHERE Name = ?", new Object[]{cname}, Integer.class).intValue();
        jdbcTemplate.update("INSERT INTO Transaction(TransactionAmount, TransactionDate, TrCourseName, TrStudentUserName) VALUES (?,now(),?,?)", price, cname, sname); // for now
        jdbcTemplate.update("INSERT INTO Enrollment VALUES (?,?,0,0)", cname, sname);
        jdbcTemplate.update("DELETE FROM Wishlist WHERE CName = ? and username = ?", cname, sname);
    }

    public List<Course> getWishlistCourses(String username) {
        List<String> names = jdbcTemplate.queryForList("SELECT CName FROM Wishlist WHERE username = ?", new Object[]{username}, String.class);
        List<Course> courses = new ArrayList<>();
        for(int i = 0; i < names.size(); i++) {
            List<Course> course = jdbcTemplate.query("SELECT * FROM Course WHERE Name = ?", new Object[]{names.get(i)}, new BeanPropertyRowMapper<>(Course.class));
            if(!course.isEmpty()) {
                courses.add(course.get(0));
            }
        }
        return courses;
    }

    public List<Video> getBookmarkedVideos(String username) {
        List<Pair<Integer,String>> ids = jdbcTemplate.query("SELECT VideoNo, CName FROM VideoBookmarks WHERE username = ?", new Object[]{username}, new RowMapper<Pair<Integer, String>>() {
            @Override
            public Pair<Integer, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Pair.of(Integer.valueOf(rs.getInt("VideoNo")),rs.getString("CName"));
            }
        });
        List<Video> videos = new ArrayList<>();
        for(int i = 0; i < ids.size(); i++) {
            List<Video> video = jdbcTemplate.query("SELECT * FROM Video WHERE VideoID = ? and CName = ?", new Object[]{ids.get(i).getFirst().intValue(), ids.get(i).getSecond()}, new BeanPropertyRowMapper<>(Video.class));
            if(!video.isEmpty()) {
                videos.add(video.get(0));
            }
        }
        return videos;
    }

    public List<Blog> getBookmarkedBlogs(String username) {
        List<Pair<Integer,String>> ids = jdbcTemplate.query("SELECT BlogNo, CName FROM BlogBookmarks WHERE username = ?", new Object[]{username}, new RowMapper<Pair<Integer, String>>() {
            @Override
            public Pair<Integer, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Pair.of(Integer.valueOf(rs.getInt("BlogNo")),rs.getString("CName"));
            }
        });
        List<Blog> blogs = new ArrayList<>();
        for(int i = 0; i < ids.size(); i++) {
            List<Blog> blog = jdbcTemplate.query("SELECT * FROM Blog WHERE BlogID = ? and CName = ?", new Object[]{ids.get(i).getFirst().intValue(), ids.get(i).getSecond()}, new BeanPropertyRowMapper<>(Blog.class));
            if(!blog.isEmpty()) {
                blogs.add(blog.get(0));
            }
        }
        return blogs;
    }
}
