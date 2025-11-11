package com.searchengine.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 结巴分词工具类
 * 负责文本预处理和中文分词
 */
@Slf4j
@Component
public class JiebaTokenizer {

    private final JiebaSegmenter jiebaSegmenter;
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\u4e00-\\u9fa5\\s]");

    public JiebaTokenizer() {
        this.jiebaSegmenter = new JiebaSegmenter();
    }

    /**
     * 清理特殊字符
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    public String cleanSpecialChars(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 移除特殊字符，保留字母、数字、中文和空白字符
        return SPECIAL_CHARS_PATTERN.matcher(text).replaceAll(" ").trim();
    }

    /**
     * 使用搜索引擎模式进行分词
     *
     * @param text 待分词文本
     * @return 分词结果列表
     */
    public List<String> tokenizeForSearch(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 清理特殊字符
            String cleanedText = cleanSpecialChars(text);

            // 使用搜索引擎模式分词
            List<SegToken> tokens = jiebaSegmenter.process(cleanedText, JiebaSegmenter.SegMode.SEARCH);

            // 提取分词结果，过滤空白和过短的词
            List<String> words = new ArrayList<>();
            for (SegToken token : tokens) {
                String word = token.word.trim();
                // 过滤条件：不为空、非空白、长度>=2、中文或英文数字混合
                if (!word.isEmpty() && word.length() >= 2) {
                    words.add(word);
                }
            }

            log.debug("分词完成：'{}' -> {}", text.substring(0, Math.min(50, text.length())), words);
            return words;

        } catch (Exception e) {
            log.error("分词失败：{}", text, e);
            return new ArrayList<>();
        }
    }

    /**
     * 计算词频
     *
     * @param words 分词结果
     * @return 词频映射
     */
    public java.util.Map<String, Integer> calculateWordFrequency(List<String> words) {
        java.util.Map<String, Integer> frequencyMap = new java.util.HashMap<>();

        for (String word : words) {
            frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
        }

        return frequencyMap;
    }

    /**
     * 批量处理文本并返回词频统计
     *
     * @param texts 文本列表
     * @return 总词频映射
     */
    public java.util.Map<String, Integer> processBatchTexts(List<String> texts) {
        java.util.Map<String, Integer> totalFrequency = new java.util.HashMap<>();

        for (String text : texts) {
            List<String> words = tokenizeForSearch(text);
            java.util.Map<String, Integer> textFrequency = calculateWordFrequency(words);

            // 合并词频
            for (java.util.Map.Entry<String, Integer> entry : textFrequency.entrySet()) {
                totalFrequency.put(entry.getKey(),
                    totalFrequency.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }

        return totalFrequency;
    }
}
