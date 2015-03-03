package org.dbiggs;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.Builder;

import javax.xml.bind.annotation.XmlTransient;

import java.beans.Transient;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds a valid instance at run time using reflection.
 * Intended as a complement to the regular builders.
 * This can be used as a quick way to build a valid entity of any type.
 * It creates a unique value for each property.
 */
public class ReflectionBuilder<T> implements Builder {

    /**
     * Static counter that increments each time a property value is generated.
     * e.g. if I have objectA and objectB which both have the property fullName
     * The first one will be set to sampleValue1 and the second to sampleValue2
     */
    protected static int propertyCount = 1;

    /**
     * Static enum counter. Each enum that is encountered is added to the map and the counter incremented.
     * Subsequent calls to generate an value of that enum type will retrieve the enum from the map first and use the
     * count
     * to select the enum value to use.
     */
    protected static Map<Class, Integer> enumCounters = new HashMap<Class, Integer>();

    private T entityToBuild;


    /**
     * Private constructor for creating a builder.
     * Builders are created via the static constructor methods.
     *
     * @param entityClass Entity class to generate
     */
    private ReflectionBuilder(Class<T> entityClass) {
        this.entityToBuild = (T) generateRandomValue(entityClass);
    }


    /**
     * Static constructor that creates a fully valid entity.
     *
     * @param entityClass Class for the entity to create
     * @return A typed builder for the entity class
     */
    @SuppressWarnings("unchecked")
    public static <T> ReflectionBuilder<T> aGenerated(Class<T> entityClass) {
        return new ReflectionBuilder<T>(entityClass);
    }

    /**
     * Reset incrementing of the property count to 1.
     * Called in the before test method in unit tests.
     * Ensures that each test executes independent of each other.
     */
    public static void resetCounter() {
        propertyCount = 1;
    }

    /**
     * @see #with(org.apache.commons.lang3.builder.Builder)
     */
    public ReflectionBuilder<T> in(Builder builder) {
        return with(builder.build());
    }

    /**
     * @see #with(Object)
     */
    public ReflectionBuilder<T> in(Object parameter) {
        return with(parameter);
    }

    /**
     * @see #with(org.apache.commons.lang3.builder.Builder)
     */
    public ReflectionBuilder<T> belongingTo(Builder builder) {
        return with(builder.build());
    }

    /**
     * @see #with(Object)
     */
    public ReflectionBuilder<T> belongingTo(Object parameter) {
        return with(parameter);
    }

    /**
     * @see #with(Object)
     */
    public ReflectionBuilder<T> ofType(Enum enumType) {
        return with(enumType);
    }

    /**
     * @see #with(String, Object)
     */
    public ReflectionBuilder<T> with(String fieldName, Builder builder) throws NoSuchFieldException {
        return with(fieldName, builder.build());
    }

    /**
     * Sets a property using the specified field name and value.
     * Since the field name can only be checked at runtime, this can be quite brittle.
     * If the value being set has matches only one field in the target class, then there is no need to specify the name.
     *
     * @param fieldName field to set value for
     * @param value value to use
     * @return The current builder
     */
    public ReflectionBuilder<T> with(String fieldName, Object value) throws NoSuchFieldException {
        Field matchingField = getFieldByNameRecursively(entityToBuild.getClass(), fieldName);
        setFieldValue(value, matchingField);
        return this;
    }

    /**
     * Sets a property from the built entity.
     * If there is more than one property of this entity type, an exception will be thrown
     *
     * @param builder builder to use
     * @return The current builder
     */
    public ReflectionBuilder<T> with(Builder builder) {
        return with(builder.build());
    }

    /**
     * Sets a property from the passed in entity.
     * If there is more than one property of this parameter type, an exception will be thrown.
     * If no property in the entity matches the parameter type, an exception will be thrown.
     *
     * @param parameter Entity to set
     * @return The current builder
     */
    public ReflectionBuilder<T> with(Object parameter) {
        Class valueType = parameter.getClass();

        Field selectedField = null;
        Class classToCheck = entityToBuild.getClass();
        while (classToCheck != Object.class) {
            selectedField = checkClassForMatchingField(valueType, selectedField, classToCheck);
            classToCheck = classToCheck.getSuperclass();
        }
        if (selectedField == null) {
            throw new RuntimeException(String.format("No property matching type %s in class %s",
                    valueType.getSimpleName(), entityToBuild.getClass().getSimpleName()));
        }
        setFieldValue(parameter, selectedField);
        return this;
    }

    /**
     * Checks a class for a field that matches the specified field.
     * An exception will be thrown if two fields are matched for the same type.
     *
     * @param valueType the class type for the value to use
     * @param selectedField the currently selected field.
     * @param classToCheck current class to check for the specified field
     * @return The selected field, null if not found
     */
    private Field checkClassForMatchingField(final Class valueType, Field selectedField, final Class classToCheck) {
        String className = classToCheck.getSimpleName();
        for (Field field : classToCheck.getDeclaredFields()) {
            Class classToUse = getRealType(field);
            if (classToUse == valueType || classToUse.isAssignableFrom(valueType)) {
                // don't set transient fields
                if (field.getAnnotation(XmlTransient.class) != null || field.getAnnotation(Transient.class) != null) {
                    continue;
                }
                if (selectedField != null) {
                    throw new RuntimeException(String.format("Both %s and %s are of type %s, " +
                                    "with method should only be used to set fields that have a unique type in class %s",
                            selectedField.getName(), field.getName(), valueType.getSimpleName(), className));
                }
                selectedField = field;
            }
        }
        return selectedField;
    }

    private void setFieldValue(final Object parameter, final Field fieldToSet) {
        if (!fieldToSet.isAccessible()) {
            fieldToSet.setAccessible(true);
        }
        try {
            fieldToSet.set(entityToBuild, parameter);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return The built entity.
     */
    @Override
    public T build() {
        return entityToBuild;
    }

    /**
     * Set all the fields in the instance with a random value.
     *
     * @param instance instance whose fields are set
     */
    public Object setFieldsWithRandomValuesFor(Object instance) {
        return setFieldsWithRandomValuesFor(instance, instance.getClass());
    }

    /**
     * Recursively set all the fields within the instance starting with the specified clazz of the instance.
     * <p/>
     * An instance has a hierarchy of classes (instance's class, instance's parent class,
     * etc. all the way to the Object class).
     *
     * @param instance instance whose fields are set
     * @param clazz the class to inspect
     */
    private Object setFieldsWithRandomValuesFor(Object instance, Class<?> clazz) {
        // Set all the fields in the instance's current class
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            Class realType = getRealType(field);

            Object randomValue = generateRandomValue(realType);
            if (randomValue != null) {
                try {
                    // only set list if existing list is null
                    if (!List.class.isAssignableFrom(realType) || field.get(instance) == null) {
                        field.set(instance, randomValue);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(String.format("Failed to set value for field %s in class %s ",
                            field.getName(), clazz.getSimpleName()), e);
                }
            }
        }

        // Set all the fields in the instance's parent class
        if (clazz.getSuperclass() != Object.class) {
            setFieldsWithRandomValuesFor(instance, clazz.getSuperclass());
        }
        return instance;
    }

    /**
     * Generate a random value for the specified class type.
     *
     * @param type Class type of the value to generate
     * @return Random value.
     */
    public Object generateRandomValue(Class<?> type) {
        if (type == String.class) {
            //This value can't be too long, some database fields restrict the size of a string to a small number
            return "sampleValue" + propertyCount++;
        } else if (type == UUID.class) {
            return UUID.randomUUID();
        } else if (type == Date.class) {
            return new Date();
        } else if (type == Timestamp.class) {
            return new Timestamp(new Date().getTime());
        } else if (type == byte[].class) {
            return ("sample byte data " + propertyCount++).getBytes();
        } else if (type == File.class) {
            File tempFile;
            try {
                tempFile = File.createTempFile("tempFileForCheck", ".zip");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp file tempFileForCheck", e);
            }
            tempFile.deleteOnExit();
            return tempFile;
        } else if (type == Boolean.class || type == boolean.class) {
            return true;
        } else if (type == Byte.class || type == byte.class) {
            if (propertyCount > Byte.MAX_VALUE) {
                propertyCount = 1;
            }
            return (byte) propertyCount++;
        } else if (type == Short.class || type == short.class) {
            return (short) propertyCount++;
        } else if (type == Long.class || type == long.class) {
            return (long) propertyCount++;
        } else if (type == Integer.class || type == int.class) {
            return propertyCount++;
        } else if (type == Double.class || type == double.class) {
            return (double) propertyCount++;
        } else if (type == Float.class || type == float.class) {
            return (float) propertyCount++;
        } else if (type.isEnum()) {
            return getEnumValueToUse(type);
        } else if (List.class.isAssignableFrom(type)) {
            return new ArrayList();
        } else if (ignoreClassType(type)) {
            // leave as class initialized them
            return null;
        } else if (!type.isPrimitive() && ClassUtils.wrapperToPrimitive(type) == null) {
            return setFieldsWithRandomValuesFor(instantiateEntity(type));
        }

        throw new RuntimeException("Could not set value for type " + type.getSimpleName());
    }

    /**
     * Attempt to return a different enum value for each setting of this enum
     */
    private Object getEnumValueToUse(Class enumClass) {
        if (enumCounters.get(enumClass) == null) {
            enumCounters.put(enumClass, 0);
        }
        Object[] enumValues = enumClass.getEnumConstants();
        int enumIndexToUse = enumCounters.get(enumClass);
        // loop back around to the start of the enum if we're at the end of the enum values.
        enumCounters.put(enumClass, (enumIndexToUse + 1 < enumValues.length) ? enumIndexToUse + 1 : 0);
        return enumValues[enumIndexToUse];
    }

    private Object instantiateEntity(Class entityClass) {
        try {
            Constructor[] constructors = entityClass.getDeclaredConstructors();
            Constructor constructorToUse = null;
            for (Constructor constructor : constructors) {
                if (constructorToUse == null || constructorToUse.getParameterTypes().length > constructor
                        .getParameterTypes().length) {
                    constructorToUse = constructor;
                }
            }
            return instantiateObjectViaConstructor(constructorToUse);

        } catch (Exception e) {
            throw new RuntimeException("Error instantiating " + entityClass.getSimpleName(), e);
        }
    }

    private Object instantiateObjectViaConstructor(final Constructor constructorToUse)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Class[] paramTypes = constructorToUse.getParameterTypes();
        Annotation[][] annotations = constructorToUse.getParameterAnnotations();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Annotation[] annotationsToCheck = annotations[i];
            for (Annotation annotationToCheck : annotationsToCheck) {
                if (annotationToCheck.annotationType() == SampleValue.class) {
                    params[i] = ((SampleValue) annotationToCheck).value();
                }
            }
            if (params[i] == null) {
                params[i] = aGenerated(paramTypes[i]).build();
            }
        }
        if (!constructorToUse.isAccessible()) {
            constructorToUse.setAccessible(true);
        }
        return constructorToUse.newInstance(params);
    }

    /**
     * Return the real type used by a field.
     * If a field type is generic, it will return the real type for that field.
     *
     * @param field field to Check
     * @return The real type.
     */
    private Class getRealType(final Field field) {
        return field.getType();
    }

    /**
     * Some class types are too awkward to set valid values for.
     *
     * @return Whether this class type should be ignored.
     */
    private boolean ignoreClassType(Class type) {
        return Map.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type) || Modifier.isAbstract
                (type.getModifiers());
    }

    private Field getFieldByNameRecursively(Class clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException nsfe) {
            if (clazz == Object.class) {
                throw nsfe;
            }
            return getFieldByNameRecursively(clazz.getSuperclass(), fieldName);
        }
    }

}
