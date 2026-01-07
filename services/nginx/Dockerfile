# 基于官方nginx镜像
FROM nginx:1.25-alpine

LABEL maintainer="your-team@example.com"

# 拷贝自定义nginx配置
COPY docker/nginx.conf /etc/nginx/nginx.conf

# 可选：拷贝静态资源（如有前端构建产物）
# COPY dist/ /usr/share/nginx/html/

# 可选：拷贝证书目录（生产环境）
# COPY certs/ /etc/nginx/ssl/

# 可选：拷贝自定义入口脚本（如需动态渲染配置）
# COPY docker/entrypoint.sh /entrypoint.sh
# RUN chmod +x /entrypoint.sh
# ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 80
# EXPOSE 443 # 如启用HTTPS

CMD ["nginx", "-g", "daemon off;"]

