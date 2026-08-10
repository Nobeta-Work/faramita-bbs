FROM nginx:alpine

COPY infra/docker/frontend/dist /para/index/bbs
COPY infra/docker/frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# # 部署流程
# 1. 构建前端 (在 frontend/web/ 目录) :
#   npm run build && cp -r dist/ ../../infra/docker/frontend/
# 2. 构建镜像 (在项目根目录执行) :
#   docker build -f infra/docker/frontend/Dockerfile -t parabbs-frontend .
# 3. 运行容器 (确保宿主机后端已启动 :8080) :
#   docker run -d \
#       -p 80:80 \
#       --name parabbs-frontend \
#       --network=host
#       parabbs-frontend