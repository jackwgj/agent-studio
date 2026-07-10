#!/bin/bash
nginx_dic="/opt/cloud/wiseagent-nginx"
nginx_conf="$nginx_dic/conf"
export PATH=$PATH:${nginx_dic}/nginx/sbin

CERT_FILE="/home/secret/cert.pem"
PRI_KEY_FILE="/home/secret/pri_key.pem"
KEYPASS_FIFO="/home/service/nginx/keypassfifo"

function check_https_enabled() {
  if [ -f "$CERT_FILE" ] && [ -f "$PRI_KEY_FILE" ] && [ -f "$KEYPASS_FIFO" ]; then
    echo "[INFO]: HTTPS certificates detected, enabling HTTPS mode"
    return 0
  else
    echo "[INFO]: HTTPS certificates not found, enabling HTTP mode"
    return 1
  fi
}

function update_nginx_config_for_https() {
  echo "[INFO]: HTTPS remains unchanged"
}

function update_nginx_config_for_http() {
  sed -i "s/listen ${POD_IP}:443 ssl;/#listen ${POD_IP}:443 ssl;/g" $nginx_conf/nginx.conf
  sed -i "s/#listen ${POD_IP}:80;/listen ${POD_IP}:80;/g" $nginx_conf/nginx.conf
  sed -i 's/ssl_protocols/#ssl_protocols/g' $nginx_conf/nginx.conf
  sed -i 's/ssl_session_timeout/#ssl_session_timeout/g' $nginx_conf/nginx.conf
  sed -i 's/ssl_session_cache/#ssl_session_cache/g' $nginx_conf/nginx.conf
  sed -i 's/ssl_session_tickets/#ssl_session_tickets/g' $nginx_conf/nginx.conf
  sed -i 's/ssl_ciphers/#ssl_ciphers/g' $nginx_conf/nginx.conf
  sed -i 's/ssl_prefer_server_ciphers/#ssl_prefer_server_ciphers/g' $nginx_conf/nginx.conf
  sed -i 's/ssl_password_file/#ssl_password_file/g' $nginx_conf/nginx.conf
  sed -i 's/ssl_certificate/#ssl_certificate/g' $nginx_conf/nginx.conf
  sed -i 's/ssl_certificate_key/#ssl_certificate_key/g' $nginx_conf/nginx.conf
  sed -i 's/ssl_stapling/#ssl_stapling/g' $nginx_conf/nginx.conf
  sed -i 's/ssl_stapling_verify/#ssl_stapling_verify/g' $nginx_conf/nginx.conf
  sed -i 's/http2 on;/#http2 on;/g' $nginx_conf/nginx.conf
  sed -i 's/set $server_scheme "https";/set $server_scheme "http";/g' $nginx_conf/nginx.conf
  sed -i 's/proxy_set_header X-Original-URL https:\/\/$host/proxy_set_header X-Original-URL http:\/\/$host/g' $nginx_conf/nginx.conf
}

# 准备nginx进程启动的所有配置文件
function init_config_file() {
  cd /home/service/
  cp /home/conf/nginx.conf $nginx_conf

  touch /opt/cloud/wiseagent-nginx/logs/access.log
  chmod 640 /opt/cloud/wiseagent-nginx/logs/access.log
  touch /opt/cloud/wiseagent-nginx/logs/error.log
  chmod 640 /opt/cloud/wiseagent-nginx/logs/error.log

  export POD_IP=$(ip addr | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | awk -F '/' '{print $1}' | head -1)
  sed -i "s/pod_ip/${POD_IP}/g" $nginx_conf/nginx.conf

  if check_https_enabled; then
    update_nginx_config_for_https
  else
    update_nginx_config_for_http
  fi
}

function init_env() {
    #ip to bind
    if [ -n "${NETWORK_DEFAULT_IP}" ]; then
      export SERVER_HOST=${NETWORK_DEFAULT_IP}
    else
      export SERVER_HOST=$(ip addr | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | awk -F '/' '{print $1}' | head -1)
      export NETWORK_DEFAULT_IP=${SERVER_HOST}
    fi
    echo "SERVER_HOST:${SERVER_HOST}"

    export PROG_ROOT_PATH="/usr/sbin"
    export USER_HOME="/home/service"
    export NGINX_START_USER="service"
    export NGINX_START_GROUP="servicegroup"
}

function add_context_path(){
    # POC替换前端路径
    context_path=$(printenv "context_path")
    if [ -n "${context_path}" ]; then
      echo "start to replace console path: ${context_path}"
      cd /opt/cloud/wiseagent-nginx/nginx/dist/hws/ || { echo "enter static path failed"; exit 1; }
      # 如果template_index.html不存在，则创建副本
      if [ ! -f "template_index.html" ]; then
        cp -v index.html template_index.html || { echo "create template_index.html failed"; exit 1; }
      fi
      chmod 700 index.html
      # 用模板覆盖index.html
      cp -f template_index.html index.html || { echo "copy -f index.html failed"; exit 1; }
      # 替换文本内容（使用#作为分隔符避免路径冲突）
      sed -i "s#/openjiuwen/#${context_path}/openjiuwen/#g" index.html || { echo "replace failed"; exit 1; }
      echo "finish to replace console path"
      chmod 500 index.html

      # 替换rewriter
      sed -i "s#\#rewritercontext_path#rewrite ^${context_path}/(.*) /\$1 last;#g" /opt/cloud/wiseagent-nginx/conf/nginx.conf || { echo "replace failed"; exit 1; }
    else
      echo "skip replace console path: ENABLE_POC=$ENABLE_POC, context_path=${context_path}"
    fi
}

function start_nginx() {
  echo "[INFO]: Start to run nginx process"
    #启动Nginx进程
    nginx -g "daemon off;"

    TimeOut=30
    Num=0
    #等待Nginx启动完成
    while [ $Num -lt $TimeOut ]; do
      PID=$(ps -ef | grep "nginx" | grep "nginx: master process" | grep -v "grep" | awk '{print $2}')
      echo "[INFO]: nginx master process PID is ${PID}"
      if [ "$PID" != "" ]; then
        SUBPID=$(ps -ef | grep "nginx: worker process" | grep "$PID" | grep -v "grep" | awk '{print $2}')
        echo "[INFO]: nginx worker process PID is ${SUBPID}"
        if [ "$SUBPID" != "" ]; then
          break
        fi
      fi
      sleep 1
      Num=$(expr $Num + 1)
    done

    if [ $Num -eq $TimeOut ]; then
      echo "[ERROR]:start nginx timeout."
    fi
}


function main() {
  init_config_file

  init_env

  add_context_path

  start_nginx

 echo "[INFO]:main end"
}
main
