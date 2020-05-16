package com.jbialy.rce.collections;

import com.jbialy.rce.collections.workspace.JobWorkspace2Impl;
import com.jbialy.rce.collections.workspace.JobWorkspace_2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class JobWorkspace_2Tests {
    static URI createMockURI(long id) {
        return URI.create("http://localhost/" + id);
    }

    @Test
    public void createEmptyTest() {
        JobWorkspace_2<URI> workspace = JobWorkspace2Impl.createEmpty();

        Assertions.assertEquals(0, workspace.allItemsCount());
        Assertions.assertEquals(0, workspace.todoCount());
        Assertions.assertEquals(0, workspace.inProgressCount());
        Assertions.assertEquals(0, workspace.doneCount());
        Assertions.assertEquals(0, workspace.damagedCount());
    }

    @Test
    public void addWithoutDuplicatesTest() {
        final int COUNT = 100_000;

        JobWorkspace_2<URI> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < COUNT; i++) {
            Assertions.assertTrue(workspace.add(createMockURI(i)));
        }

        Assertions.assertEquals(COUNT, workspace.allItemsCount());
        Assertions.assertEquals(COUNT, workspace.todoCount());
        Assertions.assertEquals(0, workspace.inProgressCount());
        Assertions.assertEquals(0, workspace.doneCount());
        Assertions.assertEquals(0, workspace.damagedCount());
    }

    @Test
    public void tryIllegallyMove_from_todo() {
        final int COUNT = 100_000;

        JobWorkspace_2<URI> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < COUNT; i++) {
            workspace.add(createMockURI(i));
        }

        for (int i = 0; i < COUNT; i++) { //Try illegally move to damaged
            Assertions.assertFalse(workspace.moveToDamaged(createMockURI(i)));
        }

        Assertions.assertEquals(COUNT, workspace.allItemsCount());
        Assertions.assertEquals(COUNT, workspace.todoCount());
        Assertions.assertEquals(0, workspace.inProgressCount());
        Assertions.assertEquals(0, workspace.doneCount());
        Assertions.assertEquals(0, workspace.damagedCount());

        for (int i = 0; i < COUNT; i++) {//Try illegally move to done
            Assertions.assertFalse(workspace.moveToDone(createMockURI(i)));
        }

        Assertions.assertEquals(COUNT, workspace.allItemsCount());
        Assertions.assertEquals(COUNT, workspace.todoCount());
        Assertions.assertEquals(0, workspace.inProgressCount());
        Assertions.assertEquals(0, workspace.doneCount());
        Assertions.assertEquals(0, workspace.damagedCount());
    }

    @Test
    public void addWithDuplicatesTest() {
        final int COUNT = 100_000;

        JobWorkspace_2<URI> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < COUNT; i++) {
            Assertions.assertTrue(workspace.add(createMockURI(i)));
            Assertions.assertFalse(workspace.add(createMockURI(i)));
        }
    }

    @Test
    public void addAndMoveTest() {
        final int COUNT = 100_000;

        JobWorkspace_2<URI> workspace = JobWorkspace2Impl.createEmpty();

        for (int i = 0; i < COUNT; i++) {
            Assertions.assertTrue(workspace.add(createMockURI(i)));
            Assertions.assertFalse(workspace.add(createMockURI(i))); // Try add duplicate
        }

        for (int i = 0; i < COUNT; i++) { //Move to inProgress
            Assertions.assertNotNull(workspace.moveToProcessingAndReturn());
        }

        for (int i = 0; i < COUNT; i++) { //Illegally moving to inProgress attempt => expected null
            Assertions.assertNull(workspace.moveToProcessingAndReturn());
        }

        for (int i = 0; i < COUNT; i++) { //Move from inProgress to done
            Assertions.assertTrue(workspace.moveToDone(createMockURI(i)));
        }

        for (int i = 0; i < COUNT; i++) { //Illegally moving from inProgress to done
            Assertions.assertFalse(workspace.moveToDone(createMockURI(i)));
        }

        for (int i = 0; i < COUNT; i++) { //Illegally moving from done to damaged
            Assertions.assertFalse(workspace.moveToDamaged(createMockURI(i)));
        }
    }
}
