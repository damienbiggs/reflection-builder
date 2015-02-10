/*
 * Project EUC Gateway
 * Copyright (c) 2014 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package org.dbiggs;

import javax.xml.bind.annotation.XmlTransient;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Contains every type that the runtime entity can populate
 */
public class TestRuntimeEntityToBuild {
    private Boolean booleanValue;
    private Byte byteValue;
    private Short shortValue;
    private Integer intValue;
    private Long longValue;
    private Double doubleValue;
    private Float floatValue;
    private String string1Value;
    private String string2Value;
    private UUID uuidValue;
    @XmlTransient
    private UUID dummyUuid;
    private File fileValue;
    private byte[] byteArrayValue;
    private Date dateValue;
    private Timestamp timestampValue;

    /**
     * @return Whether all the fields in this class have a value.
     */
    public boolean allValuesAreSet() throws IllegalAccessException {
        return checkAllFieldValues(true);
    }

    /**
     * @return Whether all the fields in this class are null.
     */
    public boolean allValuesAreUnset() throws IllegalAccessException {
        return checkAllFieldValues(false);
    }

    private boolean checkAllFieldValues(boolean valuesShouldBeNull) throws IllegalAccessException {
        List<Field> fields = Arrays.asList(this.getClass().getFields());
        for (Field field : fields) {
            boolean valueIsSet = checkFieldValue(this, field, valuesShouldBeNull);
            if (!valueIsSet) {
                return false;
            }
        }
        return true;
    }

    private boolean checkFieldValue(Object testEntity, Field input, boolean fieldShouldNotBeNull) throws
            IllegalAccessException {
        Object fieldValue = input.get(testEntity);
        return fieldShouldNotBeNull ? fieldValue != null : fieldValue == null;
    }
}
