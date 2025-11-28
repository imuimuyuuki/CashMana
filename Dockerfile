# Mavenがあらかじめインストールされている環境を使います
FROM maven:3-eclipse-temurin-17

# 作業フォルダを作ります
WORKDIR /app

# ファイルをすべてコピーします
COPY . .

# 'mvnw' ファイルを使わず、システムに入っている 'mvn' コマンドでビルドします
RUN mvn clean package -DskipTests

# アプリを起動します
CMD ["java", "-jar", "target/*.jar"]
