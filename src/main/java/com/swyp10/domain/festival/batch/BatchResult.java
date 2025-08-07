package com.swyp10.domain.festival.batch;

import lombok.Data;

@Data
public class BatchResult {
    private int successCount = 0;
    private int skipCount = 0;
    private int errorCount = 0;

    public void incrementSuccess() {
        successCount++;
    }

    public void incrementSkip() {
        skipCount++;
    }

    public void incrementError() {
        errorCount++;
    }

    public int getTotalProcessed() {
        return successCount + skipCount + errorCount;
    }

    @Override
    public String toString() {
        return String.format("Success: %d, Skip: %d, Error: %d, Total: %d",
            successCount, skipCount, errorCount, getTotalProcessed());
    }
}