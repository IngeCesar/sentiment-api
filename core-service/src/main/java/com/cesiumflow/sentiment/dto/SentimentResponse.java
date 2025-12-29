package com.cesiumflow.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentimentResponse {
    
    private String prediction;
    private Double probability;
    private List<String> keywords;
    private String timestamp;
    
}