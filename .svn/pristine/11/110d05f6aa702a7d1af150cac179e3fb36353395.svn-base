package com.megmeet.megmeetreport.sql;

import java.util.HashMap;

public class Parameters extends HashMap<Object, Object> {

    public Parameters add(Object key, Object value)
    {
        this.put(key, value);
        return this;
    }

    public Parameters clearAll()
    {
        super.clear();
        return this;
    }

    @Override
    public Object get(Object key) {
        if (super.containsKey(key)) {
            return super.get(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object key, T def)
    {
        if (key == null) return def;

        if (super.containsKey(key)) {
            return (T)super.get(key);
        }
        return def;
    }
}