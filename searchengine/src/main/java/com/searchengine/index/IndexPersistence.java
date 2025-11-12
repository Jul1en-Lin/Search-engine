package com.searchengine.index;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 索引存储工具类
 */
@Slf4j
@Component
public class IndexPersistence {
    private static final String INDEX_FILE = "D:\\Project\\Search_engine\\inverted_index.dat";

    /**
     * 保存索引到文件
     * @param index 倒排索引
     */
    public void saveIndex(Map<String, List<InvertedIndex.Posting>> index) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(INDEX_FILE)))) {
            oos.writeObject(index);
            log.info("索引已保存到文件: {}", INDEX_FILE);
        } catch (IOException e) {
            log.error("保存索引失败", e);
            throw new RuntimeException("保存索引失败", e);
        }
    }

    /**
     * 从文件加载索引
     * @return 倒排索引
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<InvertedIndex.Posting>> loadIndex() {
        File file = new File(INDEX_FILE);
        if (!file.exists()) {
            log.info("索引文件不存在，返回空索引");
            return new ConcurrentHashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            Map<String, List<InvertedIndex.Posting>> index = (Map<String, List<InvertedIndex.Posting>>) ois.readObject();
            log.info("从文件加载索引成功，共 {} 个词条", index.size());
            return index;
        } catch (IOException | ClassNotFoundException e) {
            log.error("加载索引失败", e);
            throw new RuntimeException("加载索引失败", e);
        }
    }
}
