package com.jbialy.rce;

public interface Downloader<D, U> {

    DownloadResult<D, U> download(U uri);

    DownloadResult<D, U> download(U uri, SafetySwitch<DownloadResult<D, U>> requestSafetySwitch);
}
