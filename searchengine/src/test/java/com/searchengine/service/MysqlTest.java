package com.searchengine.service;

import com.searchengine.SearchEngineApplication;
import com.searchengine.entity.Repository;
import com.searchengine.mapper.RepositoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SearchEngineApplication.class)
public class MysqlTest {
    @Autowired
    private RepositoryMapper repositoryMapper;

    @Test
    public void insert() {
        Repository repository = new Repository();
        repository.setFullName("bite/java");
        repository.setRepositoryId(10000L);
        repository.setHtmlUrl("www.bite.com");
        repository.setReadme("hello bite!");
        repositoryMapper.insert(repository);
    }
}
