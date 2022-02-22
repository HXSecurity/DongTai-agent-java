#!/bin/bash

echo "download benchmark source code"
cd ..
git clone https://github.com/exexute/BenchmarkJava.git
cd BenchmarkJava

echo "build benchmark with dongtai"
mvn clean package cargo:start -Pdeploywdongtai

echo "copy local package to temp directory"
cp ../DongTai-agent-java/dongtai-agent/target/dongtai-agent.jar tools/DongTai/dongtai.jar
cp ../DongTai-agent-java/dongtai-agent/src/main/resources/bin/*.jar target/cargo/installs/apache-tomcat-8.5.70/apache-tomcat-8.5.70/temp

echo "run benchmark in backend"
export JAVA_TOOL_OPTIONS="-Ddongtai.app.create=true -Ddongtai.app.name=$1 -Ddongtai.app.version=$2"
nohup mvn package cargo:run -Pdeploywdongtai &

echo "wait for benchmark start..."
sleep 60

echo "build Crawler"
git clone https://github.com/OWASP-Benchmark/BenchmarkUtils.git
cd BenchmarkUtils
mvn install

echo "run Crawler.sh"
cd ..
bash runCrawler.sh

echo "waiting for report upload..."
sleep 3000