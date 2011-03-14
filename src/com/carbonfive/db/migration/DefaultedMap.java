package com.carbonfive.db.migration;

import java.util.HashMap;

/**
 * @author huljas
 */
public class DefaultedMap extends HashMap {

    private Object defaultValue;

    public DefaultedMap(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object get(Object key) {
        Object value = super.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
