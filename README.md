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
