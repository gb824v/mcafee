package com.mcafee.mam.auto.infra.log.html;

public class GeneralStatistics {

    private int totalTests = 0;
    private int fails = 0;

    public GeneralStatistics() {
        
        super();
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTest) {
        this.totalTests = totalTest;
    }

    public int getFails() {
        return fails;
    }

    public void testFail() {
        this.fails++;
    }

}
