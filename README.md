## 安装

### Windows

1、下载安装[jdk](http://8rr.co/8Kbk)，一直默认下一步即可

2、下载[压缩包]()，解压，打开`conf/YuQ.properties`按提示更改需要登录的机器人QQ号和密码

3、打开Start.bat即可

### Linux

1、安装jdk

1)、手动安装
```shell script
wget https://u.iheit.com/kuku/jdk/jdk-8u251-linux-x64.tar.gz
tar -zxvf jdk-8u251-linux-x64.tar.gz -C /usr/java

cat >> /etc/profile <<EOF
export JAVA_HOME=/usr/java/jdk1.8.0_251
export CLASSPATH=$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib
export PATH=$JAVA_HOME/bin:$PATH
EOF

source /etc/profile
```
2)、通过宝塔安装
安装[宝塔](http://bt.cn)，在面板程序中找到`tomcat9`安装即可。

2、下载[压缩包，解压，
```shell script
cd ~ && mkdir yuq && cd yuq
wget 
unzip .zip
```
打开`conf/YuQ.properties`按提示更改需要登录的机器人QQ号和密码

3、运行
```shell script
#centos
yum install screen -y
screen -dmS yuq java -jar yuq-1.0-SNAPSHOT.jar

#debian/ubuntu
apt-get install screen -y
screen -dmS yuq java -jar yuq-1.0-SNAPSHOT.jar
```

## 说明
* 数据库使用h2，路径`~/.kuku/db/yuq.mv.db`，备份只需备份该文件即可
* 发送的图片保存在`~/.kuku/images`
* `~`表示用户目录下，`windows`为`C:\Users\您的用户名`，`linux`非root用户为`/home/用户名/`，root用户为`/root/`
* 步数修改使用乐心运动的接口，需要使用手机号注册乐心运动，并绑定数据来源