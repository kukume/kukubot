## KuKuBot

YuQ-Art + Spring-data-Jpa

Yuq-Art：登录qq号，通过滑块验证码之后，不需要使用手机qq去确认登录，而是通过手机验证码方式来二次验证，比某框架登录方便

- [x] ~~自动通过QQ登录滑块验证码~~ 我用的框架有BUG，打包之后就不能自动通过了。框架修复BUG？有生之年
- [x] 群管
- [x] 工具
- [x] qq和Telegram消息互通
- [ ] 哔哩哔哩自动签到+哔哩哔哩动态推送
- [x] 网易云音乐自动签到
- [ ] 微博超话自动签到+微博推送
- [ ] 百度贴吧自动签到
- [x] 酷狗音乐自动领会员
- [x] 原神签到
- [ ] oppo商城（欢太）自动签到
- [x] 修改微信、支付宝步数


## 配置文件
```properties
yu.scanPackages=me.kuku.yuq
yu.context.mode = single
yu.modules=me.kuku.yuq.JpaModule
yu.[modules=me.kuku.yuq.TelegramModule
# web 端口号
webServer.port=8081
# web 跨域
webServer.cors=http://localhost:9527
# qq协议
YuQ.Mirai.protocol=Android

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