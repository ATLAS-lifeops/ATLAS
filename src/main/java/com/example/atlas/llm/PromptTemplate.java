package com.example.atlas.llm;

public record PromptTemplate(PromptPurpose purpose, String system, String user) {
}
