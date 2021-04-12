**基于`YuQ-Mirai`的机器人**
* [YuQ-Mirai](https://github.com/YuQWorks/YuQ-Mirai)
* [YuQ-Mirai-SuperDemo](https://github.com/YuQWorks/YuQ-SuperDemo)
* [Mirai](https://github.com/mamoe/mirai)

**最新版本已支持过滑块验证码（版本过低，环境异常等都是不能过滑块验证码的原因），但是需要gui环境，如果是把挂在无ui环境的，可以在有ui环境（比如：windows系统）的机器上登录成功后，把`device.json`复制到程序根目录或者`conf`文件夹下**

## 功能
* 自动签到（QQ、哔哩哔哩、原神、HostLoc、网易云）
* 新帖推送（微博、哔哩哔哩、Twitter、HostLoc）
* 修改步数
* 图床（图片取直链）（qq、teambition、dCloud）
* 群管功能
* 一些小工具

## 教程文章

安装教程、注意事项：[https://www.kuku.me/archives/6/](https://www.kuku.me/archives/6/)

## Docker

```shell
# 拉取镜像
docker pull kukume/kukubot
# 创建文件夹
mkdir -p kukubot/conf
# 把device.json放到kukubot目录下
# 把配置文件YuQ.properties（https://file.kuku.me/kuku-bot/YuQ.properties）设置好账号和密码等信息放到conf目录下
# 运行容器
docker run -it --name kukubot -d  \
-p 8081:8081 \
-v $(pwd)/kukubot/conf:/kukubot/conf \
-v $(pwd)/kukubot/db:/kukubot/db \
kukume/kukubot
```

## 鸣谢

> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) 是一个在各个方面都最大程度地提高开发人员的生产力的 IDE，适用于 JVM 平台语言。

特别感谢 [JetBrains](https://www.jetbrains.com/?from=kuku-bot) 为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=kuku-bot) 等 IDE 的授权  
[<img src="https://img.kuku.me/images/2021/01/31/4I4aI.png" width="200"/>](https://www.jetbrains.com/?from=kuku-bot)

## 协议
AGPL