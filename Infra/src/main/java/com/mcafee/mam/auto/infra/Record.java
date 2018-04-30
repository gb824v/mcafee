package com.mcafee.mam.auto.infra;

import com.mcafee.mam.auto.infra.TestException;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents one record of a query response
 * @author Guy
 */
public class Record {

    private Map<String, String> map = new HashMap<String, String>();

    /**
     * constructs an empty record
     */
    public Record() {
    }

    /**
     * adds record value, name must be unique per record.
     * @param name - value name
     * @param value - value value
     * @throws TestException - if record already contains key.
     */
    public void add(String name, String value) throws TestException {
        if (this.map.containsKey(name)) {
            throw new TestException("Cannot assign different values to the same key");
        }
        this.map.put(name, value);
    }

    /**
     * gets value for name
     * @param name - name to look for
     * @return - value
     */
    public String get(String name) {
        return this.map.get(name);
    }
    /**
     * gets value for name
     * @param name - name to look for
     * @return - value
     */
    public String getKey() {
        return this.map.keySet().toString();
    }
    
}
