FROM maven:3-eclipse-temurin-17
WORKDIR /app
COPY . .

# アプリをビルド（作成）する
RUN mvn clean package -DskipTests

# アプリを起動する
CMD ["sh", "-c", "java -jar target/*.jar"]
