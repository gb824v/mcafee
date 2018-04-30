package com.mcafee.mam.auto.infra;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a test step method. 
 * Method that has this annotation are automatically considered as tests.
 * @author danny
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestStep {
    /***
     * defines the steps precedence order. 
     * Higher order steps are invoked later.
     * @return 
     */
    int order() default -1; 
    /***
     * defines test description.
     * Will appear in reports.
     * @return 
     */
    String description() default "no description";    
    /***
     * if true - test will be skipped.
     * @return 
     */
    boolean skip() default false;
    /**
     * if true - fail in step causes fail in test. 
     * No further step will be run for the specific test.
     * @return 
     */
    boolean mandatory() default false;
}