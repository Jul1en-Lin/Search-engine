package com.searchengine.service;

import com.searchengine.SearchEngineApplication;
import com.searchengine.dto.GiteeRepositoryDTO;
import com.searchengine.mapper.RepositoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * RepositorySyncService测试类
 */
@SpringBootTest(classes = SearchEngineApplication.class)
public class RepositorySyncServiceTest {

    @Autowired
    private RepositorySyncService repositorySyncService;

    @Test
    public void syncUserRepositories() {
        repositorySyncService.syncUserRepositories("Cary_C");
    }
}
