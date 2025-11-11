package com.searchengine.index;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 倒排索引实现类
 */
@Component
public class InvertedIndex {
    private final JiebaSegmenter segmenter;
    private Map<String, List<Posting>> index;
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[^\\w\\u4e00-\\u9fa5]");

    /**
     * 停用词集合
     */
    private Set<String> stopWords;

    public InvertedIndex() {
        this.segmenter = new JiebaSegmenter();
        this.index = new ConcurrentHashMap<>();
        this.stopWords = new HashSet<>();
    }

    /**
     * 加载停用词
     * @param filePath 停用词的文件路径
     * @return 停用词的集合
     */
    public Set<String> loadStopWords(String filePath){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    stopWords.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stopWords;
    }

    /**
     * 添加文档到倒排索引
     * @param docId 文档ID
     * @param content 文档内容
     */
    public void addDocument(Long docId, String content) {
        // 清理特殊字符
        String cleanContent = SPECIAL_CHARS.matcher(content).replaceAll(" ");

        // 使用搜索引擎模式分词
        List<SegToken> tokens = segmenter.process(cleanContent, JiebaSegmenter.SegMode.SEARCH);

        // 统计词频
        Map<String, Integer> termFreq = new HashMap<>();
        for (SegToken token : tokens) {
            String term = token.word;
            termFreq.merge(term, 1, Integer::sum);
        }

        // 更新倒排索引
        termFreq.forEach((term, freq) -> {
            Posting posting = new Posting(docId, freq);
            index.computeIfAbsent(term, k -> new ArrayList<>()).add(posting);
        });
    }

    /**
     * 搜索文档
     * @param query 查询词
     * @return 按相关度排序的文档ID列表
     */
    public List<Long> search(String query) {
        // 对查询词进行分词
        List<SegToken> tokens = segmenter.process(query, JiebaSegmenter.SegMode.SEARCH);
        Set<String> terms = tokens.stream()
                .map(segToken -> segToken.word)
                .collect(Collectors.toSet());

//        // 对terms进行一个停用词过滤
//        terms.removeAll(stopWords);
//        if (terms.isEmpty()) {
//            return Collections.emptyList();
//        }

        // 获取每个词对应的文档列表
        Map<Long, Integer> docScores = new HashMap<>();
        for (String term : terms) {
            List<Posting> postings = index.getOrDefault(term, Collections.emptyList());
            for (Posting posting : postings) {
                docScores.merge(posting.getDocId(), posting.getFreq(), Integer::sum);
            }
        }

        // 按分数排序
        return docScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 批量添加文档
     * @param documents 文档列表，key为文档ID，value为文档内容
     */
    public void addDocuments(Map<Long, String> documents) {
        documents.forEach(this::addDocument);
    }

    /**
     * 获取索引大小
     * @return 索引中的词条数量
     */
    public int size() {
        return index.size();
    }

    /**
     * 清空索引
     */
    public void clear() {
        index.clear();
    }

    /**
     * 设置索引
     * @param index 索引数据
     */
    public void setIndex(Map<String, List<Posting>> index) {
        this.index = index;
    }

    /**
     * 获取索引
     * @return 索引数据
     */
    public Map<String, List<Posting>> getIndex() {
        return index;
    }

    /**
     * 文档倒排项
     */
    @Data
    public static class Posting implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Long docId;
        private final int freq;
    }
}