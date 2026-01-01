-- Initial Schema for CesiumFlow Sentiment Analysis
-- Using UUID for better distributed systems support

CREATE TABLE IF NOT EXISTS sentiment_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_text TEXT NOT NULL,
    prediction VARCHAR(50) NOT NULL,
    probability DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Optional: Create an index for faster searches by date
CREATE INDEX idx_sentiment_created_at ON sentiment_records(created_at);
