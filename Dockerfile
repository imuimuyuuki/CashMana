FROM maven:3-eclipse-temurin-17
WORKDIR /app
COPY . .

RUN echo "========== FILE LIST START =========="
# src フォルダの中身を全部表示させる
RUN find . -maxdepth 5 -not -path '*/.*'
RUN echo "========== FILE LIST END ============"

# ここでわざと止めます
RUN exit 1
