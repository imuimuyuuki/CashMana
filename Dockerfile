FROM maven:3-eclipse-temurin-17
WORKDIR /app
COPY . .

# アプリをビルドします
RUN mvn clean package -DskipTests

# ★ここが重要！ 作られたファイルの「本当の住所」をログに表示させます
RUN echo "========== FILE CHECK START =========="
RUN find target/classes -name "*.class"
RUN echo "========== FILE CHECK END ============"

# アプリを起動します
CMD ["sh", "-c", "java -jar target/*.jar"]
