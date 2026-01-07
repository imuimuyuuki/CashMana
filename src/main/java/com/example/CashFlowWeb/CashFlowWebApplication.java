package com.example.CashFlowWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CashFlowWebApplication {

    public static void main(String[] args) {
        // DBの初期化はここで呼んでも良いですが、Controller側で行うか、
        // あるいは @PostConstruct で行うのが一般的です。
        // ここではシンプルに SpringApplication を起動するだけにします。
        
        // もし DBManager.initializeDatabase(); がここにあった場合は残しても良いですが、
        // categoryDAO.initializeCache(); という行があれば必ず削除してください。

        SpringApplication.run(CashFlowWebApplication.class, args);
    }
}
