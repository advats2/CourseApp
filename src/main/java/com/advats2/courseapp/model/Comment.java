package com.advats2.courseapp.model;

import java.sql.Date;

public class Comment {
    private int CommentID;
    private Date PostedDate;
    private String Body;
    private int CommentLikes;
    private String Susername;
    private Integer BlogNo;
    private Integer VideoNo;
    private String CName;
    private Integer ParentID;

    public int getCommentID() {
        return CommentID;
    }

    public void setCommentID(int commentID) {
        CommentID = commentID;
    }

    public Date getPostedDate() {
        return PostedDate;
    }

    public void setPostedDate(Date postedDate) {
        PostedDate = postedDate;
    }

    public String getBody() {
        return Body;
    }

    public void setBody(String body) {
        Body = body;
    }

    public int getCommentLikes() {
        return CommentLikes;
    }

    public void setCommentLikes(int commentLikes) {
        CommentLikes = commentLikes;
    }

    public String getSusername() {
        return Susername;
    }

    public void setSusername(String susername) {
        Susername = susername;
    }

    public Integer getBlogNo() {
        return BlogNo;
    }

    public void setBlogNo(Integer blogNo) {
        BlogNo = blogNo;
    }

    public Integer getVideoNo() {
        return VideoNo;
    }

    public void setVideoNo(Integer videoNo) {
        VideoNo = videoNo;
    }

    public String getCName() {
        return CName;
    }

    public void setCName(String CName) {
        this.CName = CName;
    }

    public Integer getParentID() {
        return ParentID;
    }

    public void setParentID(Integer parentID) {
        ParentID = parentID;
    }
}
