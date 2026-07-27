ARG BASE_IMAGE
FROM $BASE_IMAGE
RUN echo "Built on $BASE_IMAGE"

# 说明：BASE_IMAGE 选用官方 console 镜像（studio-console:dev-*），其已包含完整的
# nginx 目录结构、service(1001) 用户与 start.sh 启动脚本。因此本镜像只需：
#   1) 用构建产物覆盖前端静态资源；
#   2) 覆盖启动脚本（若有定制）；
#   3) 将新增资源修复为 service(1001) 归属，保证 nginx 可读。
# 原 Dockerfile 中的 groupadd/useradd/mkdir/ln -s 在基于已构建 console 镜像时会
# 因"已存在"而失败，故此处全部省略（base 镜像已完成）。

WORKDIR /home/service

ADD agent-console.tar /opt/cloud/wiseagent-nginx/nginx/
ADD script/start.sh ./start.sh

RUN chmod u+x start.sh \
    && chown -R 1001:1000 /opt/cloud/wiseagent-nginx \
    && chown -R 1001:1000 /home \
    && setcap cap_net_bind_service=+eip /usr/sbin/nginx

EXPOSE 80 443

ENTRYPOINT ["/bin/bash","/home/service/start.sh"]
