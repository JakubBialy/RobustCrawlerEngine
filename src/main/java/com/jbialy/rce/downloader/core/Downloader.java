package com.jbialy.rce.downloader.core;

import com.jbialy.rce.downloader.SafetySwitch;

public interface Downloader<D, U> {

    DownloadResult<D, U> download(U uri);

    default DownloadResult<D, U> download(U uri, SafetySwitch<DownloadResult<D, U>> requestSafetySwitch) {
        return requestSafetySwitch.testAndReturn(download(uri));
    }
}
