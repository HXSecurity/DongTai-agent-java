#!/bin/bash
cd ..

echo "download apache-tomcat-8.5.0..."
wget https://github.com/exexute/github_action_samples/releases/download/1.0.0/apache-tomcat-8.5.40.zip 1>/dev/null
unzip apache-tomcat-8.5.40.zip 1>/dev/null

echo "copy dongtai agent java to tomcat..."
cp DongTai-agent-java/dongtai-agent/target/dongtai-agent.jar apache-tomcat-8.5.40/iast/agent.jar
cp DongTai-agent-java/dongtai-agent/src/main/resources/bin/*.jar apache-tomcat-8.5.40/temp/
cd apache-tomcat-8.5.40

echo "init mysql"
mysql -uroot -pyuhjnbGYUI -h127.0.0.1 -e "DROP DATABASE IF EXISTS test;"
mysql -uroot -pyuhjnbGYUI -h127.0.0.1 -e "CREATE DATABASE test;"
mysql -uroot -pyuhjnbGYUI -h127.0.0.1 -e "CREATE USER 'test'@'%' IDENTIFIED BY 'test';"
mysql -uroot -pyuhjnbGYUI -h127.0.0.1 -e "GRANT ALL PRIVILEGES ON *.* TO 'test'@'%' WITH GRANT OPTION;"
mysql -uroot -pyuhjnbGYUI -h127.0.0.1 -e "FLUSH PRIVILEGES;"
mysql -uroot -pyuhjnbGYUI -h127.0.0.1 -e 'show DATABASES;'

echo "start catalina and waitting 30s..."
export JAVA_TOOL_OPTIONS="-Ddongtai.app.create=true -Ddongtai.app.name=$1 -Ddongtai.app.version=$2"
./bin/startup.sh 2>/dev/null

check_times=0
while [ $check_times -lt 30 ];
do
	echo 'checking openrasp api status'
	curl http://localhost:8080/vulns
	if [ "$?" == "0" ];then
		break
	else
		let check_times++
		echo "api status is down, retry $check_times times after 10s..."
		sleep 10
	fi
done


echo "start vulns spider..."
./spider.sh

echo "waitting for send vuls data"
sleep 120

echo "shutdown tomcat"
PID=$(ps aux|grep "org.apache.catalina.startup.Bootstrap start"|grep -v grep |awk '{print $2}')
echo "tomcat pid is: $PID"
kill $PID
ps aux