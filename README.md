**基于`YuQ-Mirai`的机器人**
* [YuQ-Mirai](https://github.com/YuQWorks/YuQ-Mirai)
* [YuQ-Mirai-SuperDemo](https://github.com/YuQWorks/YuQ-SuperDemo)
* [Mirai](https://github.com/mamoe/mirai)

**首次登陆可能需要验证验证码，需要gui环境，如果是把挂在无ui环境的，可以在有ui环境（比如：windows系统）的机器上登录成功后，把`device.json`复制到程序根目录或者`conf`文件夹下**

**环境：Oracle jdk8，其他环境均会异常，搭建教程中的压缩包已自带Oracle jdk8**

如果出现**5000ms**的异常，请尝试更换环境。

如果登不上，出现`禁止登录当前上网环境异常，请更换网络环境或在常用设备上登录或稍后再试。, tips=若频繁出现, 请尝试开启设备锁`，请尝试更换QQ号、删除device.json、更换环境重试，也可以参考[mirai论坛的帖子](https://mirai.mamoe.net/topic/223/%E5%BD%93%E5%89%8D%E4%B8%8A%E7%BD%91%E7%8E%AF%E5%A2%83%E5%BC%82%E5%B8%B8-%E7%9A%84%E4%B8%B4%E6%97%B6%E5%A4%84%E7%90%86%E6%96%B9%E6%A1%88)

## 功能
* 自动签到（QQ、哔哩哔哩、原神、HostLoc、网易云）
* 新帖推送（微博、哔哩哔哩、Twitter、HostLoc）
* 修改步数
* 群管功能
* office自助注册
* 一些小工具

## 教程文章

安装教程、注意事项：[https://www.kuku.me/archives/6/](https://www.kuku.me/archives/6/)

## Docker

```shell
# 拉取镜像
docker pull kukume/kukubot
# 创建文件夹
mkdir -p kukubot/conf
# 把device.json放到conf目录下
# 把配置文件YuQ.properties（https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/0b84f939-3d10-45d6-a453-8bbb6828742f.properties）（更名为YuQ.properties）设置好账号和密码等信息放到conf目录下
# 运行容器
docker run -it --name kukubot -d  \
-p 8081:8081 \
-v $(pwd)/kukubot/conf:/kukubot/conf \
-v $(pwd)/kukubot/db:/kukubot/db \
kukume/kukubot
```

## Windows

不会Linux的强力推荐使用Windows搭建。不接受搭建问题

1、下载 [压缩包](https://api.kuku.me/tb/pan/kuku/kuku-bot/kukubot-windows.zip) ，解压，用记事本（文本编辑器就行）打开conf/YuQ.properties更改需要登录的机器人QQ号和密码

2、打开 start.bat即可

## 鸣谢

> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) 是一个在各个方面都最大程度地提高开发人员的生产力的 IDE，适用于 JVM 平台语言。

特别感谢 [JetBrains](https://www.jetbrains.com/?from=kuku-bot) 为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=kuku-bot) 等 IDE 的授权  
[<img src="https://img.kuku.me/images/2021/01/31/4I4aI.png" width="200"/>](https://www.jetbrains.com/?from=kuku-bot)

## 协议
AGPL
