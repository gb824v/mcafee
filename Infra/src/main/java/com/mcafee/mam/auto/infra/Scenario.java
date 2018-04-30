package com.mcafee.mam.auto.infra;

import flexjson.JSONDeserializer;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a scenario of tests. 
 * Scenario contains tests that are run by the @ref TestRunner
 *
 * @author danny
 */
public class Scenario {

    private List<TestClass> tests = new LinkedList<TestClass>();
    private String sut = "";
    /****
     * Loads test from stream containing JSON serialized string.
     * @param inputStream - stream to load from
     * @return Scnario file
     * @throws IOException 
     */
    public static Scenario loadFrom(InputStream inputStream) throws IOException {
        JSONDeserializer<Scenario> deserializer = new JSONDeserializer<Scenario>();
        return deserializer.deserialize(TestSUT.readFile(inputStream));
    }

    /***
     * returns the list of tests contained in the scenario.
     * @return 
     */
    public List<TestClass> getTests() {
        return tests;
    }

    /***
     * sets the tests list for this scenario.
     * @param classes 
     */
    public void setTests(List<TestClass> classes) {
        this.tests = classes;
    }

	public String getSut()
	{
		return sut;
	}

	public void setSut(String sut)
	{
		this.sut = sut;
	}
}
