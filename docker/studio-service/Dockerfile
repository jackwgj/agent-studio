ARG BASE_IMAGE
FROM $BASE_IMAGE
RUN echo "Built on $BASE_IMAGE"

# 注：此处修改需同步修改studio-service.yaml中的runAsUser、runAsGroup、fsGroup
RUN groupadd -g 1001 service && useradd -u 1001 -g service -s /bin/bash -m service

# 根路径
ARG APP_PATH=/opt/cloud/studio-service
ENV APP_PATH $APP_PATH
# 服务运行用户
ARG SERVICE_USER=service
ARG SERVICE_USER_GROUP=service

WORKDIR ${APP_PATH}

# 拷贝
COPY lib/*.jar app/
COPY script/ bin/
COPY config/ config/

# 赋权
RUN chmod -R 550 app/ \
    && chmod -R 750 bin/ config/ \
    && chown -R service:service app/ bin/ config/

# 切换到非 root 用户运行
USER service

# 暴露 Spring Boot 默认端口
EXPOSE 31113

# 使用启动脚本作为入口点，确保信号传递到 Java 进程
ENTRYPOINT ["bin/start.sh"]