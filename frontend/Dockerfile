# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
# 安装依赖，包括 vite
RUN npm install
# 将 node_modules/.bin 添加到 PATH
ENV PATH /app/node_modules/.bin:$PATH
COPY . .
# 赋予 vite 执行权限 (如果需要)
RUN chmod +x node_modules/.bin/vite
RUN npm run build

# Run stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
