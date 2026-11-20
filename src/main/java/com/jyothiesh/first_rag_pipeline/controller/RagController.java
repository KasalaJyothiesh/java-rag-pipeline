package com.jyothiesh.first_rag_pipeline.controller;

import com.jyothiesh.first_rag_pipeline.service.RagService;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/rag")
public class RagController {
    RagService ragService;
    RagController(RagService ragService)
    {
        this.ragService=ragService;
    }
    @GetMapping("/ask")
    public String askLLM(@RequestParam String userQuestion)
    {
        return ragService.ask(userQuestion);
    }
}
