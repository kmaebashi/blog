rmdir /s /q C:\Tomcat10\webapps\blog\WEB-INF\classes\com\kmaebashi
xcopy /E C:\maebashi\develop\IdeaProjects\Blog\target\classes\com C:\Tomcat10\webapps\blog\WEB-INF\classes\com
#xcopy /E C:\maebashi\develop\IdeaProjects\Blog\out\production\Blog\com C:\Tomcat10\webapps\blog\WEB-INF\classes\com
copy /y C:\maebashi\develop\IdeaProjects\Blog\src\main\resources\htmltemplate\*.html C:\Tomcat10\webapps\blog\WEB-INF\htmltemplate\
copy /y C:\maebashi\develop\IdeaProjects\Blog\src\main\resources\htmltemplate\blogid\*.html C:\Tomcat10\webapps\blog\WEB-INF\htmltemplate\blogid\
copy /y C:\maebashi\develop\IdeaProjects\Blog\src\main\resources\htmltemplate\blogid\post\*.html C:\Tomcat10\webapps\blog\WEB-INF\htmltemplate\blogid\post\
copy /y C:\maebashi\develop\IdeaProjects\Blog\src\main\resources\htmltemplate\blogid\date\*.html C:\Tomcat10\webapps\blog\WEB-INF\htmltemplate\blogid\date\
copy /y C:\maebashi\develop\IdeaProjects\Blog\src\main\resources\htmltemplate\css\*.css C:\Tomcat10\webapps\blog\css\
copy /y C:\maebashi\develop\IdeaProjects\Blog\src\main\resources\htmltemplate\js\*.js C:\Tomcat10\webapps\blog\js\
rem copy /y C:\maebashi\develop\IdeaProjects\Blog\properties\application.properties_local C:\Tomcat10\webapps\ykameibo\WEB-INF\classes\application.properties
