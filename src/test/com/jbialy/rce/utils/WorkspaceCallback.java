package com.jbialy.rce.utils;

import com.jbialy.rce.callbacks.DataCallback;
import com.jbialy.rce.collections.workspace.JobWorkspace_2;

import java.net.URI;

public class WorkspaceCallback implements DataCallback<JobWorkspace_2<URI>> {

    private JobWorkspace_2<URI> workspace = null;

    public JobWorkspace_2<URI> getWorkspace() {
        return workspace;
    }

    @Override
    public synchronized void onCall(JobWorkspace_2<URI> incpomingWorkspace) {
        this.workspace = incpomingWorkspace;
    }
}
