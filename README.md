# so-vits-svc-cover
这是一个使用so-vits-svc进行ai翻唱的mirai插件
## 使用
部署教程暂未完成...

由于该插件部署复杂到只能用悲剧形容,我建了一个QQ群供大家交流使用:762608337

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

# 2.(不推荐)
cd uvr5-cli
pip install -r .\requirements.txt
cd so-vits-svc
pip install -r .\requirements.txt
```