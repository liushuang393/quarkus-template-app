@echo off
chcp 65001
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Tokyo -Dsun.jnu.encoding=UTF-8 -Duser.language=ja -Duser.country=JP -Djava.awt.headless=true
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8

echo Starting Quarkus with UTF-8 encoding...
echo JAVA_OPTS: %JAVA_OPTS%
./mvnw quarkus:dev -Pdev -Ddebug=5005
