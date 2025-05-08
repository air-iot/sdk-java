@echo off

SET "JAVA_HOME=F:/Program Files/Java/jdk-1.8"
set "PATH=%JAVA_HOME%\bin;%PATH%"

java -version

mvn -P release -s F:\\apache-maven-3.9.5\\conf\\settings_aliyun.xml clean -Dmaven.test.skip=true deploy