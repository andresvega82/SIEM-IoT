/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omp4j.proc;

import java.util.List;

/**
 * The default implementation of ProcListener.
 * 
 * Simply stores the output of the executing Proc.
 * 
 * 
 */
public class ProcListener {

    protected String standardOutput, errorOutput;
    
    public ProcListener() {
        reset();
    }
    
    private void reset() {
        this.standardOutput = "";
        this.errorOutput = "";
    }

    /**
     * Proc is about to start/restart.
     * 
     * Resets any captured output from the previous run.
     * @param command
     */
    public void start(String command, List<String> parameters) {
        reset();
    }

    /**
     * Proc is executing and has captured a line of the output.
     * 
     * @param line  The next line of input
     */
    public void line(String line) {
        this.standardOutput += line + "\n";
    }
    
    /**
     * Proc is executing and has captured an error line of the output.
     * 
     * @param line  The next line of input
     */
    public void errorLine(String line) {
        this.errorOutput += line + "\n";
    }
    
    /**
     * The Proc has finished executing.
     * 
     * The complete output of the executed proc is also provided.
     * 
     * @param output  The complete output
     * @param errOutput The 
     * @param exitValue Exit value of Proc after completing
     */
    public void finished(String output, String errOutput, int exitValue) {}
    
    
    
    public String getStandardOutput() {
        return standardOutput;
    }
    
    public String getErrorOutput() {
        return errorOutput;
    }
}
