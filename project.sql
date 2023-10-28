create database dbms_project;
use dbms_project;

SET SQL_SAFE_UPDATES=0;

create table User(
username varchar(255) not null,
FName varchar(255) not null,
LName varchar(255),
password varchar(255) not null,
role varchar(10),
primary key(username)
);

select * from User;

delete from User where username = "educator1";

select * from Admin;

select * from Educator;

select * from Course;

delete from Course;

select * from Teacher;

delete from Teacher;

select * from PendingCourses;

select * from PendingTeachers;

select * from PendingEducators;

delete from PendingEducators;

SELECT * from PendingEducators WHERE username = "educator1";

create table Admin(
username varchar(255) not null,
FName varchar(255) not null,
LName varchar(255),
DateOfJoining date,
password varchar(255),
primary key(username),
foreign key (username) references User(username)
);

create table PendingEducators(
username varchar(255) not null,
Gender varchar(255),
FName varchar(255) not null,
LName varchar(255),
Degree varchar(255),
Year int,
University varchar(255),
About text,
password varchar(255),
AdminUserName varchar(255),
primary key(username),
foreign key (AdminUserName) references Admin(username),
foreign key (username) references User(username)
);

create table Educator(
username varchar(255) not null,
Gender varchar(255),
FName varchar(255) not null,
LName varchar(255),
Degree varchar(255),
Year int,
University varchar(255),
About text,
password varchar(255),
AdminUserName varchar(255),
primary key(username),
foreign key (AdminUserName) references Admin(username),
foreign key (username) references User(username)
);

create table EducatorEmailIDs(
EmailID varchar(255) not null,
username varchar(255) not null,
primary key(EmailID,username),
foreign key (username) references Educator(username)
);

create table EducatorFieldOExpertise(
FieldOfExpertise varchar(255) not null,
username varchar(255) not null,
primary key(FieldOfExpertise,username),
foreign key (username) references Educator(username)
);

create table Student(
username varchar(255) not null,
FName varchar(255) not null,
LName varchar(255),
password varchar(255),
Age int,
City varchar(255),
State varchar(255),
Pincode int,
Specialisation varchar(255),
Degree varchar(255),
College varchar(255),
DOB date,
primary key(username),
foreign key (username) references User(username)
);

create table StudentEmailIDs(
EmailID varchar(255) not null,
username varchar(255) not null,
primary key(EmailID,username),
foreign key (username) references Student(username)
);

create table StudentPhNos(
PhoneNo bigint not null,
username varchar(255) not null,
foreign key (username) references Student(username)
);

create table Course(
Name varchar(255) not null,
Description text,
Rating numeric(10,2),
Price numeric(20,2),
Category varchar(255),
primary key(Name)
);

create table PendingCourses(
Name varchar(255) not null,
Description text,
Rating numeric(10,2),
Price numeric(20,2),
Category varchar(255),
primary key(Name)
);

create table Video(
VideoID int not null,
CName varchar(255),
ThumbnailLink varchar(255),
Rating numeric(10,2),
Link varchar(255),
Views int,
Description text,
Duration int, /* Store as second, then format in frontend as hh:mm:ss */
Title varchar(255) not null,
UploadDate date,
primary key(VideoID,CName),
foreign key (CName) references Course(Name)
);

create table Blog(
BlogID int not null,
CName varchar(255),
Rating numeric(10,2),
CreatedDate date,
Views int,
Content longtext,
ReadTime int, /* Store as second, then format in frontend as hh:mm:ss */
Title varchar(255) not null,
primary key(BlogID,CName),
foreign key (CName) references Course(Name)
);

create table Comment(
CommentID int not null,
PostedDate date,
Body text,
CommentLikes int,
Susername varchar(255),
BlogNo int,
VideoNo int,
ParentID int,
primary key(CommentID),
foreign key (Susername) references Student(username),
foreign key (BlogNo) references Blog(BlogID),
foreign key (VideoNo) references Video(VideoID),
foreign key (ParentID) references Comment(CommentID)
);

create table Transaction(
TransactionID int not null,
TransactionAmount numeric(20,2),
TransactionDate date,
TrCourseName varchar(255),
TrStudentUserName varchar(255),
primary key(TransactionID),
foreign key (TrCourseName) references Course(Name),
foreign key (TrStudentUserName) references Student(username)
);

create table Teacher(
username varchar(255) not null,
CName varchar(255) not null,
primary key(username,CName),
foreign key (CName) references Course(Name),
foreign key (username) references Educator(username)
);

create table PendingTeachers(
username varchar(255) not null,
CName varchar(255) not null,
primary key(username,CName),
foreign key (CName) references PendingCourses(Name),
foreign key (username) references Educator(username)
);

create table Wishlist(
CName varchar(255) not null,
username varchar(255) not null,
primary key(CName,username),
foreign key (CName) references Course(Name),
foreign key (username) references Student(username)
);

create table Enrollment(
CName varchar(255) not null,
username varchar(255) not null,
Rating numeric(10,2),
Progress numeric(10,2), /* stored as a percentage */
primary key(CName,username),
foreign key (CName) references Course(Name),
foreign key (username) references Student(username)
);

create table VideoRating(
VideoNo int not null,
CName varchar(255) not null,
username varchar(255) not null,
primary key(VideoNo,CName,username),
foreign key (username) references Student(username),
foreign key (VideoNo,CName) references Video(VideoID,CName)
);

create table BlogRating(
BlogNo int not null,
CName varchar(255) not null,
username varchar(255) not null,
primary key(BlogNo,CName,username),
foreign key (username) references Student(username),
foreign key (BlogNo,CName) references Blog(BlogID,CName)
);

create table VideoBookmarks(
VideoNo int not null,
CName varchar(255) not null,
username varchar(255) not null,
primary key(VideoNo,CName,username),
foreign key (username) references Student(username),
foreign key (VideoNo,CName) references Video(VideoID,CName)
);

create table BlogBookmarks(
BlogNo int not null,
CName varchar(255) not null,
username varchar(255) not null,
primary key (BlogNo,CName,username),
foreign key (username) references Student(username),
foreign key (BlogNo,CName) references Blog(BlogID,CName)
);



















