package com.searchengine.service;

import com.searchengine.SearchEngineApplication;
import com.searchengine.index.InvertedIndex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SearchEngineApplication.class)
public class IndexServiceTest {

    @Autowired
    private IndexService service;

    @Test
    public void buildIndex() {
        service.buildIndex();
    }

    @Test
    public void loadIndex() {
        service.loadIndex();
    }

    @Test
    public void search() {
        service.loadIndex();
        service.search("项目介绍").forEach(repositoryEntity -> {
            System.out.print(repositoryEntity.getId()+","+repositoryEntity.getFullName());
            System.out.println();
        });
    }
}
