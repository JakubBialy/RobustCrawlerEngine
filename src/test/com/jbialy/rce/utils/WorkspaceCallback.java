package com.jbialy.rce.utils;

import com.jbialy.rce.callbacks.DataCallback;
import com.jbialy.rce.collections.workspace.JobWorkspace;

import java.net.URI;

public class WorkspaceCallback implements DataCallback<JobWorkspace<URI>> {

    private JobWorkspace<URI> workspace = null;

    public JobWorkspace<URI> getWorkspace() {
        return workspace;
    }

    @Override
    public synchronized void onCall(JobWorkspace<URI> incpomingWorkspace) {
        this.workspace = incpomingWorkspace;
    }
}
