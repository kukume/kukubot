## KuKuBot

YuQ-Art + Spring-data-Jpa

环境要求：`jdk11`以上

- [x] 自动通过QQ登录滑块验证码
- [x] 群管
- [x] 工具
- [x] qq和Telegram消息互通
- [x] 哔哩哔哩自动签到+哔哩哔哩动态推送+哔哩哔哩开播提醒
- [x] 网易云音乐自动签到
- [x] 微博超话自动签到+微博推送
- [x] 百度贴吧自动签到
- [x] 酷狗音乐自动领会员
- [x] 原神签到
- [ ] oppo商城（欢太）自动签到
- [x] 修改微信、支付宝步数


## 配置文件
```properties
yu.scanPackages=me.kuku.yuq
yu.context.mode = single
yu.modules=me.kuku.yuq.config.jpa.JpaModule
yu.[modules=me.kuku.yuq.config.telegram.TelegramModule
# web 端口号
webServer.port=8081
# web 跨域
webServer.cors=http://localhost:9527
# qq协议
YuQ.ArtQQ.protocol=Android

# qq号
YuQ.ArtQQ.user.qq=
# 密码
YuQ.ArtQQ.user.pwd=

# 机器人主人
YuQ.ArtQQ.master=734669014

# tg机器人 token
me.kuku.botToken=
# tg机器人名称
me.kuku.botUsername=
# 你的用户id
me.kuku.creatorId=
```