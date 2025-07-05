package com.justn0w.service;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RagTest {


    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private OllamaChatModel ollamaChatModel;

    @Resource
    private VectorStore vectorStore;


    @Test
    public void upload() {
        // 1 读取文件
        TikaDocumentReader reader = new TikaDocumentReader("a.txt");

        List<Document> documents = reader.get();
        List<Document> documentList = tokenTextSplitter.apply(documents);

        documents.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));
        documentList.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));

        vectorStore.accept(documentList);

        log.info("完成上传");
    }

    @Test
    public void chatTest() {
        String message = "我是AI吗？";
        String prompt = """
            Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
            If unsure, simply state that you don't know.
            Another thing you need to note is that your reply must be in Chinese!
            DOCUMENTS:
                {documents}
            """;

        SearchRequest request = SearchRequest.builder()
                .query(message)
                .topK(5)
                .filterExpression("knowledge == '知识库名称'")
                .build();



        List<Document> documents = vectorStore.similaritySearch(request);

        // [{"score":0.8017032444477081,"metadata":{"source":"a.txt","distance":0.19829676,"knowledge":"知识库名称"},"contentFormatter":{"metadataTemplate":"{key}: {value}","excludedEmbedMetadataKeys":[],"metadataSeparator":"\n","textTemplate":"{metadata_string}\n\n{content}","excludedInferenceMetadataKeys":[]},"id":"24ddb6ad-0462-45dd-b0bc-c72714538665","text":"我是justn0w，我不是AI","media":null},{"score":0.6467585563659668,"metadata":{"source":"a.txt","distance":0.35324144,"knowledge":"知识库名称"},"contentFormatter":{"metadataTemplate":"{key}: {value}","excludedEmbedMetadataKeys":[],"metadataSeparator":"\n","textTemplate":"{metadata_string}\n\n{content}","excludedInferenceMetadataKeys":[]},"id":"0a46c439-6750-442d-b647-cee081c7a721","text":"我是justn0w，我不是AI\n我出生于 1994年，来自河南省洛阳市","media":null}]
        log.info("documents:{}", JSON.toJSONString(documents));

        String documentsFromKnowledgeBase = documents.stream().map(Document::getText).collect(Collectors.joining());

        Message message1 = new SystemPromptTemplate(prompt).createMessage(Map.of("documents", documentsFromKnowledgeBase));

        List<Message> list = Arrays.asList(new UserMessage(message), message1);

        Prompt modelPrompt = new Prompt(list);
        ChatResponse callResponse = ollamaChatModel.call(modelPrompt);

        log.info("测试结果:{}", JSON.toJSONString(callResponse));


    }
}
