## Tutorials

[First login tutorial](https://www.kuku.me/archives/17/) 、 [build](https://www.kuku.me/archives/6/)

## Functions
* Automatic sign-in
* New post push
* Modify steps

## Use by Docker

```shell
mkdir -p /root/kukubot

docker pull kukume/kukubot

docker run -d --name kukubot -it \
-v /root/kukubot/conf/:/var/kukubot/bin/conf/ \
-v /root/kukubot/db/:/var/kukubot/bin/db/ \
kukume/kukubot
```
It will start fail in first time, then go to /root/kukubot/conf/YuQ.properties to change the qq and password, and copy device.json to the /root/kukubot/conf/ directory

## Thanks

> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) is an IDE that maximizes developer productivity in every way, for the JVM platform language.

[<img src="https://img.kuku.me/images/2021/01/31/4I4aI.png" width="200"/>](https://www.jetbrains.com/?from=kuku-bot)

## Protocols
AGPL
