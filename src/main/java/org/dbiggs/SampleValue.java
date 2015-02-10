package org.dbiggs;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to set a sample valid value for a class property.
 * Useful so that the ReflectionBuilder constructs valid properties.
 */
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface SampleValue {
    String value();
}
