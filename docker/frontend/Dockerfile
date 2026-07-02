FROM nginx:alpine

COPY docker/frontend/dist /faramita/index/bbs
COPY docker/frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# # 部署流程
# 1. 构建前端 (在 frontend/faramita-bbs-frontend/ 目录) :
#   npm run build && cp -r dist/ ../../docker/frontend/
# 2. 构建镜像 (在项目根目录执行) :
#   docker build -f docker/frontend/Dockerfile -t faramita-bbs-frontend .
# 3. 运行容器 (确保宿主机后端已启动 :8080) :
#   docker run -d \
#       -p 80:80 \
#       --name faramita-bbs-frontend \
#       --network=host
#       faramita-bbs-frontend