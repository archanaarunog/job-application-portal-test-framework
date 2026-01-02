package com.archana.framework.utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Simple retry: retries once on failure (adjust maxRetryCount if needed).
 * Use by annotating test: @Test(retryAnalyzer = com.archana.framework.utils.RetryAnalyzer.class)
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int maxRetryCount = 1;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            return true;
        }
        return false;
    }
}
