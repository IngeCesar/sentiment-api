package com.cesiumflow.sentiment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentimentRequest {

    @NotBlank(message = "The 'text' field is required and cannot be empty.")
    @Size(min = 3, max = 5000, message = "Text must be between 3 and 5000 characters.")
    private String text;

}