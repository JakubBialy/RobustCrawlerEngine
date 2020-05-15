package com.jbialy.rce;

import com.jbialy.rce.downloader.core.DownloadResult;

public interface ReTryPredicate<T, U> {
    boolean shouldTry(DownloadResult<T, U> httpResponse, long madeRequests);
}
