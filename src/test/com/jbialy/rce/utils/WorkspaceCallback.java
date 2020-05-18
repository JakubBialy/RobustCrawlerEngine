package com.jbialy.rce.utils;

import com.jbialy.rce.callbacks.DataCallback;
import com.jbialy.rce.collections.workspace.Workspace;

import java.net.URI;

public class WorkspaceCallback implements DataCallback<Workspace<URI>> {

    private Workspace<URI> workspace = null;

    public Workspace<URI> getWorkspace() {
        return workspace;
    }

    @Override
    public synchronized void onCall(Workspace<URI> incpomingWorkspace) {
        this.workspace = incpomingWorkspace;
    }
}
