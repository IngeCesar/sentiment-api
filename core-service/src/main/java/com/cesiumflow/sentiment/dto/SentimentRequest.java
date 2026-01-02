package com.cesiumflow.sentiment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRequest {

    @NotBlank(message = "The 'text' field is required.")
    @Size(min = 3, max = 5000, message = "Text must be between 3 and 5000 characters.")
    private String text;
}