package com.facebook.presto.jdbc.utils;

import org.testng.Assert;

import static org.testng.Assert.fail;

public class Assertions
{
    public static <T, U extends T> void assertInstanceOf(T actual, Class<U> expectedType, String message) {
        Assert.assertNotNull(actual, "actual is null");
        Assert.assertNotNull(expectedType, "expectedType is null");
        if(!expectedType.isInstance(actual)) {
            fail("%sexpected:<%s> to be an instance of <%s>", new Object[]{toMessageString(message), actual, expectedType.getName()});
        }
    }

    private static String toMessageString(String message) {
        return message == null?"":message + " ";
    }

    public static void fail(String format, Object... args) {
        String message = String.format(format, args);
        Assert.fail(message);
    }
}
