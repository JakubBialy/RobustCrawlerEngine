package com.jbialy.rce;

public interface ReTryPredicate<T, U> {
    boolean shouldTry(DownloadResult<T, U> httpResponse, long madeRequests);
}
