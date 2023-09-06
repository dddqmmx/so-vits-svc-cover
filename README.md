# so-vits-svc-cover
这是一个使用so-vits-svc进行ai翻唱的mirai插件
## 使用

由于该插件部署复杂到只能用悲剧形容,我建了一个QQ群供大家交流使用:762608337
![motobe_izou](motobe_izou.png)
### 推荐环境

- JDK 17
- Anaconda conda 23.7.2

### 1.部署NeteaseCloudMusicApi
[项目地址](https://github.com/Binaryify/NeteaseCloudMusicApi)


### 2.安装anaconda
[官网](https://www.anaconda.com/)

### 3. 克隆所需项目
```shell
# 找个路径克隆 so-vits-svc
git clone https://github.com/svc-develop-team/so-vits-svc
# 找个路径克隆 uvr5-cli
https://github.com/dddqmmx/uvr5-cli.git
```
### 4. 配置python依赖
```shell
# 首先要下载pytorch,选择适合你的版本。下面的命令是示例千万不要照抄
# https://pytorch.org/
pip3 install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu117

# 安装项目python依赖

# 1.(推荐)
cd so-vits-svc-cover
pip install -r .\requirements.txt

# 2.(不推荐)
cd uvr5-cli
pip install -r .\requirements.txt
cd so-vits-svc
pip install -r .\requirements.txt
```

### 5. 将本插件拖入到plugins文件夹然后新建配置文件
在config里新建com.dddqmmx.cover文件夹,新建文件config.properties,spk_list.json
```shell
# config.properties
neteasePhone=你的网易云音乐账号的手机号
neteasePassword=你的网易云音乐密码
baseUrl163Music=你搭建好的NeteaseCloudMusicApi的地址
pythonPath=你的python.exe路径
uvr5Path=克隆的uvr5项目路径
soVitsSvc=克隆的so-vits-svc项目路径
```
```shell
# spk_list.json,例子
# parameter如何填写请看,so-vits-svc项目的README.md
# parameter千万不要填写-s参数和-t!!!!
[
  {
    "name": "nana7mi",
    "parameter": " -m \"D:/AI/Client/weights/so-vits-svc-4.1/nanami/G.pth\" -c \"D:/AI/Client/weights/so-vits-svc-4.1/nanami/config.json\" -cm \"D:/AI/Client/weights/so-vits-svc-4.1/nanami/kmeans.pt\" -cr 0.5 -dm \"D:/AI/Client/weights/so-vits-svc-4.1/nanami/diffusion.pt\" -dc \"D:/AI/Client/weights/so-vits-svc-4.1/nanami/diffusionConfig.yaml\" -s \"nanami\" -ks 100"
  },
  {
    "name": "shirakami_fubuki",
    "parameter": " -m \"D:/AI/Client/weights/so-vits-svc-4.0/holo/G.pth\" -c \"D:/AI/Client/weights/so-vits-svc-4.0/holo/config.json\" -cm \"D:/AI/Client/weights/so-vits-svc-4.0/holo/kmeans.pt\" -cr 0.5 -s \"fubuki\""
  },
  {
    "name": "houshou_marine",
    "parameter": " -m \"D:/AI/Client/weights/so-vits-svc-4.0/holo/G.pth\" -c \"D:/AI/Client/weights/so-vits-svc-4.0/holo/config.json\" -cm \"D:/AI/Client/weights/so-vits-svc-4.0/holo/kmeans.pt\" -cr 0.5 -s \"marine\""
  },
  {
    "name": "湊泔水",
    "parameter": " -m \"D:/AI/Client/weights/so-vits-svc-4.0/holo/G.pth\" -c \"D:/AI/Client/weights/so-vits-svc-4.0/holo/config.json\" -cm \"D:/AI/Client/weights/so-vits-svc-4.0/holo/kmeans.pt\" -cr 0.5 -s \"aqua\""
  }
]
