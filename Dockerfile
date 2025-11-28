# 1. Java 17の環境を用意する
FROM eclipse-temurin:17-jdk-jammy

# 2. 作業フォルダを作る
WORKDIR /app

# 3. ファイルをすべてコピーする
COPY . .

# 4. 実行権限を与えて、ビルド（アプリの作成）を行う
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# 5. アプリを起動する
CMD ["java", "-jar", "target/*.jar"]
