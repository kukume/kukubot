## KuKuBot

YuQ-Art + Spring-data-Jpa

- [x] 群管
- [x] 工具
- [x] qq和Telegram消息互通
- [ ] 哔哩哔哩自动签到+哔哩哔哩动态推送
- [ ] 微博超话自动签到+微博推送
- [ ] 百度贴吧自动签到
- [ ] 酷狗音乐自动领会员
- [ ] 原神签到
- [ ] oppo商城（欢太）自动签到
- [ ] 修改微信、支付宝步数


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