/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omp4j.proc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Brent Jacobs
 */
public class Proc {
    
    private String command;
    private ArrayList<String> parameters;
    
    private ArrayList<ProcListener> listeners;
    
    public Proc(String command) {
        this.command = command;
        this.parameters = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }
    
    public void addParameter(String parameter) {
        parameters.add(parameter);
    }
    
    public void addParameter(String option, String parameter) {
        parameters.add(option);
        parameters.add(parameter);
    }
    
//    public void addParameter(String format, Object... parameter) {
//        parameters.add(String.format(format, parameter));
//    }
    
    public void addListener(ProcListener listener) {
        listeners.add(listener);
    }
    
    public String exec() throws IOException, InterruptedException {
        return executeCommand(getCommand(), getParameters());
    }
    
    public String getCommand() {
        return command;
    }
    
    public List<String> getParameters() {
        return new ArrayList<>(parameters);
    }
    
    private String executeCommand(String command, List<String> parameters) throws IOException, InterruptedException {
        
        notifyListenersOfStart(command, parameters);
        
        ArrayList<String> cmd = new ArrayList<>(parameters); // clone the params
        cmd.add(0, command); // insert command at the start
        
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process proc = pb.start();
//        Process proc = Runtime.getRuntime().exec(command);
        
        BufferedReader procReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        // read output of command
        String line = null, standardOutput = "",
               errorLine = null, errorOutput = "";
        while ((line = procReader.readLine()) != null || (errorLine = errReader.readLine()) != null) {
            if (line != null) {
                notifyListenersOfLine(line);
                standardOutput += line + "\n";
            }
            
            if (errorLine != null) {
                notifyListenersOfErrorLine(errorLine);
                errorOutput += errorLine + "\n";
            }
        }
        
        proc.waitFor();
        int exitValue = proc.exitValue();
        proc.destroy();
        
        notifyListenersOfFinished(standardOutput, errorOutput, exitValue);
        return standardOutput;
    }
    
    protected void notifyListenersOfStart(String command, List<String> parameters) {
        for (ProcListener listener : listeners) {
            listener.start(command, new ArrayList<>(parameters));
        }
    }
    
    protected void notifyListenersOfLine(String line) {
        for (ProcListener listener : listeners) {
            listener.line(line);
        }
    }
    
    protected void notifyListenersOfErrorLine(String line) {
        for (ProcListener listener : listeners) {
            listener.errorLine(line);
        }
    }
    
    protected void notifyListenersOfFinished(String standardOutput, String errorOutput, int exitValue) {
        for (ProcListener listener : listeners) {
            listener.finished(standardOutput, errorOutput, exitValue);
        }
    }
    
//    public String sudoExec(String password) {
//        return executeCommand(getSudoCommand());
//    }
    
//    public String getSudoCommand(String password) {
//        // see: http://stackoverflow.com/questions/14015829/how-to-supply-sudo-with-root-password-from-java
//        // this will only work on linux
//        return "echo " + password  + " | sudo -S " + command;
//    }
    
    
    
}
