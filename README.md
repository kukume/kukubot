**基于`YuQ-Mirai`的没有菜单的而且也不好用的机器人**
* [YuQ](https://github.com/YuQWorks/YuQ)
* [YuQ-Mirai](https://github.com/YuQWorks/YuQ-Mirai)
* [YuQ-Mirai-Demo](https://github.com/YuQWorks/YuQ-Mirai-Demo)
* [YuQ-Mirai-SuperDemo](https://github.com/YuQWorks/YuQ-SuperDemo)
* [Mirai](https://github.com/mamoe/mirai)

**一切开发旨在学习，请勿用于非法用途**

## 功能
* QQ签到
* 超级萌宠养成
* 推送最新微博到群聊（指定用户名）、私聊（我的关注），微博自动赞、评论、转发
* 修改步数（wx and alipay）
* 网易云音乐打卡和每日300首听歌量
* 群管功能
* 一些小工具

指令：[https://u.iheit.com/kuku/bot/menu.html](https://u.iheit.com/kuku/bot/menu.html)

安装教程：[https://blog.kuku.me/index.php/archives/3/](https://blog.kuku.me/index.php/archives/3/)

## 配置文件
```properties
# 运行模式，该模式会影响配置文件读取，或某些模块的行为。默认为 dev
# yu.config.runMode = dev

# 扫描包路径
yu.scanPackages=me.kuku.yuq

# 机器人登录的协议，可为 Watch （手表）、Android （安卓），默认为Ipad
YuQ.Mirai.protocol=

# 机器人名，可不配置。
# YuQ.bot.name = Yu

# 登录的 QQ 号
YuQ.Mirai.user.qq=
# 登录的 QQ 号的密码
YuQ.Mirai.user.pwd=
# 机器人主人
YuQ.Mirai.bot.master=734669014
# api
YuQ.Mirai.bot.myApi=api.kuku.me
# ai.qq.com/v1的app_id，需赋予图片鉴黄、智能闲聊、通用OCR能力
YuQ.Mirai.bot.ai.appId=
# ai.qq.com/v1的app_key，需赋予图片鉴黄、智能闲聊、通用OCR能力
YuQ.Mirai.bot.ai.appKey=
```

## 说明
* 机器人使用之前必须发送`机器人 开`才能开启机器人
* 数据库使用h2，目录`db`下
* 发送的图片保存在`images`目录下
* 步数修改使用lexin运动的接口

## 协议
**一切开发旨在学习，请勿用于非法用途**

**本项目禁止被用于进行违反中华人民共和国法律法规的行为**

------

```text
Copyright (C) 2018-2020 StarWishsama

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```