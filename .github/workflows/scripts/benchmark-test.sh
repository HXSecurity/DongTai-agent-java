#!/bin/bash

echo "download benchmark source code"
cd ..
git clone https://github.com/exexute/BenchmarkJava.git
cd BenchmarkJava

echo "build benchmark with dongtai"
mvn clean package cargo:start -Pdeploywdongtai

echo "copy local package to temp directory"
cp ../DongTai-agent-java/release/iast-agent.jar tools/DongTai/dongtai.jar
cp ../DongTai-agent-java/release/lib/*.jar target/cargo/installs/apache-tomcat-8.5.70/apache-tomcat-8.5.70/temp

echo "run benchmark in backend"
export JAVA_TOOL_OPTIONS="-Dproject.create=true -Dproject.version=${{ github.event_name }}-${{ github.run_number }}"
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