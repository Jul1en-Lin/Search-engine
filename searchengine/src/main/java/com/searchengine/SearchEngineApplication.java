package com.searchengine;

import com.searchengine.service.IndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * 搜索引擎应用
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class SearchEngineApplication implements CommandLineRunner {
    private final IndexService indexService;

    public static void main(String[] args) {
        SpringApplication.run(SearchEngineApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("正在加载索引...");
        indexService.loadIndex();
        log.info("索引加载完成");
        log.info("正在加载停用词");
        indexService.loadStopWords("D:\\Project\\Search_engine\\stopwords.txt");
        log.info("停用词加载完成");
    }
}

