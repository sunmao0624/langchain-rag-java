package com.xiong.note.client;

import com.xiong.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "rag-service")
public interface RagClient {
    @PostMapping("/ai/extract-tags")
    Result<String> extractTags(@RequestBody Map<String, String> request);
}
