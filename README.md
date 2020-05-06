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

        http://newfirebird.cs.txstate.edu:8080/MyNetID/servlets/ematch.jsp


9. The following tables are needed to run the demo. The tables are the same as those used
   in Pro C/C++ version.


10. Last, but not the least, if you change your application (recompiled a JAVA file or modified
    a JSP file, for example), you need to do:

    % touch web.xml

    where the web.xml file is the one inside your WEB-INF sub-directory, to make the changes effective.

11. remember to remove all classes before compilation "rm -f *.class" 

12. compile "javac *。java"

13. remember to change permission: chmod 744 *.class

14. Debug with this page:  http://newfirebird.cs.txstate.edu:8080/z_x3/servlets/ematch.jsp  

15. Debug with Tomcat log: cd $CATALINA_HOME  -->  echo $CATALINA_HOME  --> /usr/local/apache-tomcat-7.0.26

16. Update DB connection credentials: ematchConstStruct.java --> it contains all the system values

17. <pre> <td><a href="/z_x3/servlets/jobSearch.html">Search</td> </pre> 

18. Full Servlet + JSP tutorial on my channel:   https://youtu.be/XL0cQ_s0hSU 




