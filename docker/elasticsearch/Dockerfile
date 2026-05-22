# 使用包含 IK 分词器的自定义镜像
FROM elasticsearch:7.17.7

# 切换到插件目录
WORKDIR /usr/share/elasticsearch/plugins

# 创建目录
RUN mkdir analysis-ik

# 复制本地 IK 分词器压缩包 (请确保 elasticsearch-analysis-ik-7.17.7.zip 与 Dockerfile 在同一目录下)
COPY elasticsearch-analysis-ik-7.17.7.zip ik.zip

# 解压并清理
RUN unzip ik.zip -d analysis-ik && \
    rm ik.zip

# 切换回工作目录
WORKDIR /usr/share/elasticsearch
