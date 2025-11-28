# 1. JavaとMavenが入った環境を使う
FROM maven:3-eclipse-temurin-17

# 2. 作業場所を作る
WORKDIR /app

# 3. ファイルをすべてコピーする
COPY . .

# 4. ビルド（アプリの作成）をする
# ※ここでmvnwではなく、システムに入っているmvnコマンドを使います
RUN mvn clean package -DskipTests

# 5. アプリを起動する
CMD ["sh", "-c", "java -jar target/*.jar"]
