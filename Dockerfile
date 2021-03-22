FROM jachinte/oracle-jdk-1.6.0_23

# 下载maven
RUN curl https://mirrors.bfsu.edu.cn/apache/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz -o /opt/apache-maven-3.2.5-bin.tar.gz && cd /opt && tar -zxvf apache-maven-3.2.5-bin.tar.gz && ln -s /opt/apache-maven-3.2.5/bin/mvn /usr/local/bin/mvn
COPY pom.xml /opt/java-agent/pom.xml
COPY iast-agent /opt/java-agent/iast-agent
COPY iast-core /opt/java-agent/iast-core
COPY iast-inject /opt/java-agent/iast-inject
COPY docker/settings.xml /root/.m2/settings.xml
WORKDIR /opt/java-agent
# 编译
CMD mvn clean package -Dmaven.test.skip=true
