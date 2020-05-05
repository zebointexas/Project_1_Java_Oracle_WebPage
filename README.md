# Project_1_Java_Oracle_WebPage




In the following, I'll use the environment $CATALINA_HOME to refer the
root of your Apache Tomcat server.

0. A file named "cs5334-java.tar" has been placed under the
   URL http://newfirebird.cs.txstate.edu/~wp01/cs5334-java, namely look at the file
   http://newfirebird.cs.txstate.edu/~wp01/cs5334-java/cs5334-java.tar,
   which contains all the source code for the servlet version of the class demo.
   Just the file to the "public_html" directory within your home directory and
   do:

     % tar xpf cs5334-java.tar
   
   where "%" is your login shell prompt -- do not type it.

   to unpack the whole file. This will create a subdirectory "cs5334-java" (which may
   has been created previously) with proper subdirectories and files for the demo.

1. Software environment. Actually, you can use my .bashrc file to set up the needed environment.
   Simply do the following (when you are in your home directory):

    % cp /home/Faculty/wp01/.bashrc .

   that will copy my .bashrc file to your home directory. That file, when you login, will set
   up proper environment for your copy of the application to work. Among others, the most
   important ones are environemtn variables of $CATALINA_HOME, $JAVA_HOME, and etc.


2. In class we discussed the structure of a TomCat Java Servlet application.

   Specifically, each such application should be placed in a subdirectory of
   the directory $CATALINA_HOME/webapps.

   In order to have individual applications within each user's "public_html" directory,
   the lab has created a soft link from $CATALINA_HOME/webapps to the directory
   "public_html/cs5334-java" in your home. The name of the soft link is the same
   as your Linux login name.

   If your Linux login name is js1001 then the directory for the demo would be:

    /home/Students/js1001/public_html/cs5334-java

   The soft link $CATALINA_HOME/webapps/js1001 points to the above directory.

3. Within the directory of each application, the following subdirectories are needed:
   the "servlets" directory, the "WEB-INF" directory, and the "jsp" direcotry.

   For example, for the demo, within "public_html/cs5334-java" directory, we have
   
   (1) HTML files (and some simple JSP file) in the directory "servlets"
   (2) Java Servlet files in the directory: "WEB-INF/classes"
   (3) Most JSP files in the directory: "jsp"

   There is an important file named "web.xml" in the subdirectory "WEB-INF" that
   describes the application.

   Please read the TomCat document for more details. Please have the patience to read it!.


4. Look at the file ematchConstStruct.java and find the two entries for
   USERNAME and PASSWORD. Change them to your Oracle login name
   and password.

5. Change all lines in all java files that contain the word "/wp01/" to "/your_Linux_login_name/".
   Recall, the java files are in /home/Students/your_Linux_login_name/public_html/cs5334-java/WEB-INF/classes/

6. Similarly, change all lines in all HTML files that contain the word "/wp01/" to "/your_Linux_login_name/".
   Recall, the HTML files are in /home/Students/your_Linux_login_name/public_html/cs5334-java/servlets/

7. Compile all the java files using java compiler. For example:

      % javac home.java


8. Port number

   TomCat Java Servlet contain runs at port 8080. If your Linux login name is js1001, you should
   invoke the home page of your demonstration program using the URI:

        http://newfirebird.cs.txstate.edu:8080/your_Linux_login_name/servlets/ematch.jsp


9. The following tables are needed to run the demo. The tables are the same as those used
   in Pro C/C++ version:


   (a) The 'job' table:

     Name                                    Null?    Type
     --------------------------------------- -------- -------------------

     JOB_ID                                  NOT NULL VARCHAR2(10)
     JOB_TYPE                                         VARCHAR2(40)
     JOB_TITLE                                        VARCHAR2(50)
     SPECIALIZATION                                   VARCHAR2(50)
     COUNTRY_CODE                                     NUMBER(3)
     REGION_NAME                                      VARCHAR2(30)
     STATE_NAME                                       VARCHAR2(20)
     LOCATION                                         VARCHAR2(30)
     MIN_SALARY                                       NUMBER(9)
     MAX_SALARY                                       NUMBER(9)
     COMPANY_NAME                                     VARCHAR2(50)
     START_DATE                                       VARCHAR2(20)
     REFERENCE_NUM                                    VARCHAR2(10)
     CONTACT_PERSON                                   VARCHAR2(50)
     DESCRIPTION                                      VARCHAR2(4000)
     QUALIFICATION                                    VARCHAR2(2000)

   (b) The 'member' table:
     Name                                 Null?    Type
     ------------------------------------ -------- ----------------------------

     LOGIN_ID                             NOT NULL VARCHAR2(20)
     FIRST_NAME                           NOT NULL VARCHAR2(20)
     MID_I_NAME                                    VARCHAR2(20)
     LAST_NAME                            NOT NULL VARCHAR2(20)
     SPECIALIZATION                       NOT NULL VARCHAR2(50)
     EMAIL                                NOT NULL VARCHAR2(50)
     PHONE                                         VARCHAR2(20)
     FAX                                           VARCHAR2(20)
     WEB_URL                                       VARCHAR2(50)
     CURRENT_COMPANY                               VARCHAR2(30)
     CURRENT_JOB_TITLE                    NOT NULL VARCHAR2(50)
     CURRENT_JOB_LOCATION                 NOT NULL VARCHAR2(30)
     CURRENT_JOB_LOCATION_CODE            NOT NULL NUMBER(1)
     YEAR_OF_EXP                          NOT NULL NUMBER(2)
     DEGREE                               NOT NULL NUMBER(1)
     DESIRED_JOB_1                                 VARCHAR2(50)
     DESIRED_JOB_2                                 VARCHAR2(50)
     DESIRED_JOB_3                                 VARCHAR2(50)
     DESIRED_SALARY                                NUMBER(9)
     DESIRED_JOB_LOCATION                          VARCHAR2(30)
     DESIRED_JOB_LOCATION_CODE                     NUMBER(1)
     SPECIAL_TALENTS                               VARCHAR2(50)
     RESUME                                   VARCHAR2(4000)

   (c) The 'c_g' table:
     Name                                 Null?    Type
     ------------------------------------ -------- -------------------

     LOGIN_ID                             NOT NULL VARCHAR2(20)
     FIRST_NAME                           NOT NULL VARCHAR2(20)
     MID_I_NAME                                    VARCHAR2(20)
     LAST_NAME                            NOT NULL VARCHAR2(20)
     MAJOR                                NOT NULL VARCHAR2(50)
     EMAIL                                NOT NULL VARCHAR2(50)
     PHONE                                         VARCHAR2(20)
     FAX                                           VARCHAR2(20)
     WEB_URL                                       VARCHAR2(50)
     COLLEGE_NAME                         NOT NULL VARCHAR2(30)
     COLLEGE_CITY                                  VARCHAR2(30)
     COLLEGE_STATE                                 VARCHAR2(30)
     EXPECTED_DEGREE                      NOT NULL NUMBER(1)
     EXPECTED_DEGREE_DATE                 NOT NULL DATE
     EXPECTED_MIN_SALARY                           NUMBER(9)
     DESIRED_JOB_LOCATION                          VARCHAR2(30)
     DESIRED_JOB_LOCATION_CODE                     NUMBER(1)
     SPECIAL_TALENTS                               VARCHAR2(50)
     RESUME                                        VARCHAR2(4000)

   (d) The 'employer' table:

     Name                                 Null?    Type
     ------------------------------------ -------- ---------------

     EMP_ID                               NOT NULL VARCHAR2(20)
     COMPANY_NAME                         NOT NULL VARCHAR2(50)
     CONTACT_PERSON                                VARCHAR2(50)
     EMAIL                                         VARCHAR2(50)
     PHONE                                         VARCHAR2(15)
     FAX                                           VARCHAR2(15)
     PROFILE                                       VARCHAR2(4000)
     PASSWORD                                      VARCHAR2(20)

   (e) The 'job_ids' table:

     Name                                 Null?    Type
     --------------------------------------------- ----------

     JOB_ID                                        NUMBER(9)


10. Last, but not the least, if you change your application (recompiled a JAVA file or modified
    a JSP file, for example), you need to do:

    % touch web.xml

    where the web.xml file is the one inside your WEB-INF sub-directory, to make the changes effective.
