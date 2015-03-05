package org.dbiggs.tests;


import org.dbiggs.ClassMethodsDataProvider;
import org.dbiggs.tests.entities.DummyOperations;
import org.dbiggs.tests.entities.DummyRestClient;
import org.dbiggs.tests.entities.SkipMethodInvocation;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.testng.Assert.fail;

public class DummyOperationsPermissionTest {

    private DummyRestClient dummyRestClient = new DummyRestClient();

    private DummyOperations dummyOperations = new DummyOperations();

    /**
     * @return Operation methods in the admin operation class to run permission test.
     */
    @DataProvider
    public Object[][] getOperationMethods() throws Exception {
        return new ClassMethodsDataProvider(DummyOperations.class, dummyRestClient)
                .getOperationMethods(SkipMethodInvocation.class);
    }

    /**
     * Actually run the test against the specified operation method.
     *
     * @param methodSignature Method name
     * @param operationMethod operation method
     * @param parameters parameters to pass into the method
     */
    @Test(dataProvider = "getOperationMethods")
    public void verifyClientHasNoPermission(String methodSignature, Object operationMethod, Object parameters)
            throws IllegalAccessException, InstantiationException {
        runOperationMethod(methodSignature, (Method) operationMethod, (Object[]) parameters, dummyOperations);
    }

    /**
     * Invokes an operation method. Expect to fail.
     *
     * @param methodSignature string representation of method signature
     * @param method method entity to invoke
     * @param parameters parameters for method
     * @param operationsInstance instance of operations class to get methods from
     */
    protected void runOperationMethod(String methodSignature, Method method, Object[] parameters,
                                      Object operationsInstance) throws IllegalAccessException {
        try {
            method.invoke(operationsInstance, parameters);
            fail("Expected IllegalArgumentException but it didn't occur for method " + method.getName());
        } catch (InvocationTargetException e) {
            if (!(e.getTargetException() instanceof IllegalArgumentException)) {
                e.getTargetException().printStackTrace();
                fail(e.getTargetException() + " occurred instead of IllegalArgumentException for method " + methodSignature);
            }
        }
    }

}
