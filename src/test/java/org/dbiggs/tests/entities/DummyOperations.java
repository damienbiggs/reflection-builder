package org.dbiggs.tests.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Dummy instance for testing creating testng data provider.
 */
public class DummyOperations {

    @SkipMethodInvocation
    public List<TestRuntimeEntityToBuild> listEntities(DummyRestClient restClient) {
        // Should be skipped so it not throwing an IllegalArgumentException will be okay
        return new ArrayList<TestRuntimeEntityToBuild>();
    }

    public void createTestEntity(DummyRestClient restClient, TestRuntimeEntityToBuild runtimeEntityToBuild) {
        throw new IllegalArgumentException();
    }

    public TestRuntimeEntityToBuild getTestEntity(DummyRestClient restClient) {
        throw new IllegalArgumentException();
    }

    public void deleteTestEntity(DummyRestClient restClient) {
        throw new IllegalArgumentException();
    }
}
