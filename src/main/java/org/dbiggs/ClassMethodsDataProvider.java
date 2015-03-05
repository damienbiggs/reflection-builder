package org.dbiggs;

import org.dbiggs.ReflectionBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generates a test ng data provider for a class.
 */
public class ClassMethodsDataProvider {

    private Class classToGenerateMethodsFor;

    private List<Object> realParameters;

    public ClassMethodsDataProvider(Class classToGenerateMethodsFor, Object... realParameters) {
        this.classToGenerateMethodsFor = classToGenerateMethodsFor;
        this.realParameters = Arrays.asList(realParameters);
    }

    // increment property count for each value created, assures unique values
    private static int propertyCount = 1;

    /**
     * Returns an array of methods in testng dataprovider format to invoke on the class.
     *
     * @param methodIgnoreAnnotations optional annotations to check. Methods with this annotation are not added.
     * @return The array of methods
     */
    public Object[][] getOperationMethods(Class<? extends Annotation>... methodIgnoreAnnotations)
            throws IllegalAccessException, InstantiationException, IOException, InvocationTargetException,
            NoSuchMethodException {
        Method[] methods = classToGenerateMethodsFor.getDeclaredMethods();
        List<Object[]> methodsToExecute = new ArrayList<Object[]>();
        for (Method method: methods) {
            boolean shouldMethodBeSkipped = false;
            for (Class<? extends Annotation> methodIgnoreAnnotation : methodIgnoreAnnotations) {
                if (method.isAnnotationPresent(methodIgnoreAnnotation)) {
                    shouldMethodBeSkipped = true;
                    break;
                }
            }

            if (shouldMethodBeSkipped) {
                continue;
            }

            if (Modifier.isPrivate(method.getModifiers())) {
                continue;
            }

            StringBuilder methodSignature = new StringBuilder();
            methodSignature.append(method.getName()).append("(");
            Class[] parameterTypes = method.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = getValueForParameterType(parameterTypes[i]);
                methodSignature.append(String.valueOf(parameters[i]));
                if (i != parameters.length - 1) {
                    methodSignature.append(", ");
                }
            }
            methodSignature.append(")");
            methodsToExecute.add(new Object[]{methodSignature.toString(), method, parameters});
        }
        return methodsToExecute.toArray(new Object[methodsToExecute.size()][]);
    }

    /**
     * Returns a valid value for a parameter
     *
     * @param parameterType parameter class type
     * @return the valid value
     */
    private Object getValueForParameterType(Class parameterType)
            throws IllegalAccessException, InstantiationException, IOException, InvocationTargetException,
            NoSuchMethodException {
        for (Object realParameter : realParameters) {
            if (realParameter.getClass().isAssignableFrom(parameterType)) {
                return realParameter;
            }
        }
        return ReflectionBuilder.aGenerated(parameterType).build();
    }
}
