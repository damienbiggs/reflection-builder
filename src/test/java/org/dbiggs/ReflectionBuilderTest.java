package org.dbiggs;


import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.dbiggs.ReflectionBuilder.aGenerated;

public class ReflectionBuilderTest {

    @Test
    public void canConstructTestRuntimeEntity() throws IllegalAccessException, NoSuchFieldException {
        TestRuntimeEntityToBuild testEntity = aGenerated(TestRuntimeEntityToBuild.class)
                .with("baseStringValue", "stringTestValue").build();
        assertTrue(testEntity.allValuesAreSet(), "All values should be set");
        assertEquals(testEntity.getBaseStringValue(), "stringTestValue");
    }

    @Test
    public void canConstructMultipleInstances() throws IllegalAccessException, NoSuchFieldException {
        for (int i = 0; i < 1000; i++) {
            canConstructTestRuntimeEntity();
        }
    }

    @Test
    public void noValuesAreSetBeforehand() throws IllegalAccessException {
        TestRuntimeEntityToBuild testEntity = new TestRuntimeEntityToBuild();
        assertTrue(testEntity.allValuesAreUnset(), "All values should be unset");
    }

    /**
     * TestRuntimeEntityToBuild has two UUID fields but one is marked @Transient
     */
    @Test
    public void willIgnoreTransientAnnotatedType() throws IllegalAccessException {
        TestRuntimeEntityToBuild testEntity = aGenerated(TestRuntimeEntityToBuild.class).with(UUID.randomUUID())
                .build();
        assertTrue(testEntity.allValuesAreSet(), "All values should be set");
    }

}
