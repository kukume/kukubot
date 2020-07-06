**基于`YuQ-Mirai`的机器人**
* [YuQ](https://github.com/YuQWorks/YuQ)
* [YuQ-Mirai](https://github.com/YuQWorks/YuQ-Mirai)
* [YuQ-Mirai-Demo](https://github.com/YuQWorks/YuQ-Mirai-Demo)
* [Mirai](https://github.com/mamoe/mirai)

## 功能
* QQ自动签到
* 超级萌宠快速养成
* 自动修改步数
* 一些小工具

指令：[https://u.iheit.com/kuku/bot/menu.html](https://u.iheit.com/kuku/bot/menu.html)

## 安装

### Windows

1、下载安装 [jdk](http://8rr.co/8Kbk) ，一直默认下一步即可

2、下载 [压缩包](https://u.iheit.com/kuku/bot/yuq.zip) ，解压，打开`conf/YuQ.properties`按提示更改需要登录的机器人QQ号和密码

3、打开`start.bat`即可

### Linux

1、安装jdk

* 手动安装
```shell script
mkdir /usr/java
wget https://u.iheit.com/kuku/jdk/jdk-8u251-linux-x64.tar.gz
tar -zxvf jdk-8u251-linux-x64.tar.gz -C /usr/java
# 配置环境变量
cat >> /etc/profile <<EOF
export JAVA_HOME=/usr/java/jdk1.8.0_251
export CLASSPATH=\$JAVA_HOME/lib/tools.jar:\$JAVA_HOME/lib/dt.jar:\$JAVA_HOME/lib
export PATH=\$JAVA_HOME/bin:\$PATH
EOF

source /etc/profile
```
* 或者通过宝塔安装

安装 [宝塔](http://bt.cn) ，在面板程序中找到`tomcat9`安装即可。

2、下载压缩包，解压
```shell script
cd ~ && mkdir yuq && cd yuq
wget https://u.iheit.com/kuku/bot/yuq.zip
unzip yuq.zip
```
打开`conf/YuQ.properties`按提示更改需要登录的机器人QQ号和密码

3、运行
```shell script
### 安装screen
#centos
yum install screen -y
#debian/ubuntu
apt-get install screen -y
# 运行
bash start.sh
# 停止
bash stop.sh
```
如需查看运行日志：
```shell script
screen -R yuq
```

### Android
安卓使用`Termux`安装linux来运行

1、下载并安装[Termux](https://www.coolapk.com/apk/com.termux)

2、安装`Linux`，这里使用 [国光](https://www.sqlsec.com/2020/04/termuxlinux.html) 大佬的脚本
```shell script
pkg install proot git python -y
git clone https://github.com/sqlsec/termux-install-linux
cd termux-install-linux
python termux-linux-install.py
# 想安装啥，看提示就行
```
等待安装成功后，运行
```shell script
# 如安装为Debian
cd ~/Termux-Linux/Debian
./start-debian.sh
# 如安装为Centos
cd ~/Termux-Linux/CentOS
./start-centos.sh
```
这样，Linux系统就跑起来了。。

3、安装jdk
```shell script
mkdir /usr/java
wget https://u.iheit.com/kuku/jdk/jdk-8u251-linux-arm64-vfp-hflt.tar.gz
tar -zxvf jdk-8u251-linux-arm64-vfp-hflt.tar.gz -C /usr/java

cat >> /etc/profile <<EOF
export JAVA_HOME=/usr/java/jdk1.8.0_251
export CLASSPATH=\$JAVA_HOME/lib/tools.jar:\$JAVA_HOME/lib/dt.jar:\$JAVA_HOME/lib
export PATH=\$JAVA_HOME/bin:\$PATH
EOF

source /etc/profile
```

4、运行
```shell script
cd ~ && mkdir yuq && cd yuq
wget https://u.iheit.com/kuku/bot/yuq.zip
unzip yuq.zip
java -jar yuq-1.0-SNAPSHOT.jar
```
保持`Termux`后台即可

## 说明
* 数据库使用h2，目录`db`下
* 发送的图片保存在`images`目录下
* 步数修改使用乐心运动的接口，需要使用手机号注册乐心运动，并绑定数据来源
* 配置文件需要修改的地方
```properties
# 登录的 QQ 号
YuQ.Mirai.user.qq=
# 登录的 QQ 号的密码
YuQ.Mirai.user.pwd=
```
* 本程序仅供内部学习和交流使用，并倡导富强、民主、文明、和谐,倡导、平等、公正、法治,倡导爱、敬业、信、友善,积极培育和践行社会主义核心价值观。