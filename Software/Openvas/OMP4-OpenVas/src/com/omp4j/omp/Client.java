package com.omp4j.omp;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.Severity;
import com.omp4j.commands.*;
import com.omp4j.responses.*;
import com.omp4j.proc.*;
import com.omp4j.syslog.UdpSyslogMessageSender;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

/**
 *
 * @author Brent Jacobs
 */
public class Client {
    
    private final String username, password, host, port;
    
    public Client(String username, String password, String host, String port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }
    
    public Client(String username, String password) {
        this(username, password, "localhost", "9390");
    }
    
    private String execute(OMPCommand command) throws IOException, InterruptedException {
        Proc proc = createOMPProc();
        proc.addParameter("-iX", command.toXML());
        proc.addListener(createProcListener());
        return proc.exec();
    }
    
    private Document parseResponse(String response) throws ParsingException, IOException {
        Builder parser = new Builder();
        Document doc = parser.build(response, "http://www.openvas.org/omp-5-0.html");
        return doc;
    }
    
    public GetConfigsResponse getConfigs() throws IOException, InterruptedException, ParsingException {
        String response = execute(new GetConfigs());
        Document doc = parseResponse(response);
        return new GetConfigsResponse(doc);
    }
    
    public GetTargetsResponse getTargets() throws IOException, InterruptedException, ParsingException {
        String response = execute(new GetTargets());
        Document doc = parseResponse(response);
        return new GetTargetsResponse(doc);
    }
    
    public CreateTargetResponse createTarget(String name, String hosts) throws IOException, InterruptedException, ParsingException {
        String response = execute(new CreateTarget(name, hosts));
        Document doc = parseResponse(response);
        return new CreateTargetResponse(doc);
    }
    
    public CreateTaskResponse createTask(String name, String comment, String configID, String targetID) throws IOException, InterruptedException, ParsingException {
        String response = execute(new CreateTask(name, comment, configID, targetID));
        Document doc = parseResponse(response);
        return new CreateTaskResponse(doc);
    }
    
    public StartTaskResponse startTask(String taskID) throws IOException, InterruptedException, ParsingException {
        String response = execute(new StartTask(taskID));
        Document doc = parseResponse(response);
        return new StartTaskResponse(doc);
    }
    
//    public String stopTask() throws IOException, InterruptedException, ParsingException {
//        return null;
//    }
//    
//    public String pauseTask() throws IOException, InterruptedException, ParsingException {
//        return null;
//    }
    
    public GetTasksResponse getTasks(String taskID) throws IOException, InterruptedException, ParsingException {
        String response = execute(new GetTasks(taskID));
        Document doc = parseResponse(response);
        return new GetTasksResponse(doc);
    }
    
    public GetReportFormatsResponse getReportFormats() throws IOException, InterruptedException, ParsingException {
        String response = execute(new GetReportFormats());
        Document doc = parseResponse(response);
        return new GetReportFormatsResponse(doc);
    }
    
    public GetReportsResponse getReports(String reportID, String formatID) throws IOException, InterruptedException, ParsingException {
        String response = execute(new GetReports(reportID, formatID));
        Document doc = parseResponse(response);
        return new GetReportsResponse(doc);
    }
    
    public GetReportsResponse getReports() throws IOException, InterruptedException, ParsingException {
        String response = execute(new GetReports());
        Document doc = parseResponse(response);
        return new GetReportsResponse(doc);
    }
    
    public GetResultsResponse getResults() throws IOException, InterruptedException, ParsingException {
        String response = execute(new GetResults());
        Document doc = parseResponse(response);
        return new GetResultsResponse(doc);
    }
    /**
     * @param args the command line arguments
    **/
    
    
    public static void main(String... args) throws Throwable {
        String taskKey = "xxxxxxx";
        String taskValue = "xxxxxxx";
        
        // Consulta a OpenVas con credenciales        
        Client client = new Client("admin", "vega1967","localhost","9390");
        GetTargetsResponse targets = client.getTargets();
        Map<String, String> targetIDsByHost = targets.getTargetIDsByHost();
        for( Map.Entry<String,String> entry: targetIDsByHost.entrySet()){
            taskKey = entry.getKey();
            taskValue = entry.getValue();
            
            System.out.println("Key = "+taskKey+" Value = "+taskValue);
        }
        
       
        
        GetResultsResponse results = client.getResults();
       
        ArrayList<ArrayList> syslogInfo = results.getSyslogInfo();
        
        
        // Creación paquete Syslog
        
        UdpSyslogMessageSender messageSender = new UdpSyslogMessageSender();
        messageSender.setDefaultMessageHostname(""); // some syslog cloud services may use this field to transmit a secret key
        messageSender.setDefaultAppName("OpenVas");
        messageSender.setDefaultFacility(Facility.USER);
        messageSender.setDefaultSeverity(Severity.ALERT);
        messageSender.setSyslogServerHostname("10.2.78.8");
        //messageSender.setSyslogServerHostname("192.168.0.2");
        messageSender.setSyslogServerPort(514);
        messageSender.setMessageFormat(MessageFormat.RFC_3164);
        //Definición campos del mensaje
        try {
        // send a Syslog message
        for (int i = 0; i < syslogInfo.size(); i++) {
            ArrayList<String> envio = new ArrayList<String>();
            envio = syslogInfo.get(i);
            for (int j = 0; j < envio.size(); j++){
                System.out.println("ENVIO "+envio.get(j));
                messageSender.sendMessage(envio.get(j));
            } 
        }
        
        } catch (IOException ex) {
        Logger.getLogger(ex.getMessage());
        }
        
        /**
        try{
        String host = "192.168.0.22",
        scanType = "Full and fast";
        Client omp = new Client("admin", "andrewfevi96","192.168.0.22","9390");
        System.out.println("===  Get ConfigID for Full and Fast ===================================".substring(0, 65));
        GetConfigsResponse cr = omp.getConfigs();
        String configID = cr.getConfigIDsByName().get(scanType);
        System.out.println("===  Get targets ======================================================".substring(0, 65));
        GetTargetsResponse tr = omp.getTargets();
        String targetID = tr.getTargetIDsByHost().get(host);
        if (targetID == null) {
        System.out.println("===  Create Target ================================================".substring(0, 65));
        CreateTargetResponse ctr = omp.createTarget(host, host);
        targetID = ctr.getTargetID();
        }
        System.out.println("===  Create Task ======================================================".substring(0, 65));
        CreateTaskResponse ctkr = omp.createTask("Task for " + host, "", configID, targetID);
        String taskID = ctkr.getTaskID();
        System.out.println("===  Start task ======================================================".substring(0, 65));
        StartTaskResponse str = omp.startTask(taskID);
        String reportID = str.getReportID();
        System.out.println("===  Check Report Status ======================================================".substring(0, 65));
        String reportStatus = null;
        while (reportStatus == null || !reportStatus.equalsIgnoreCase("Done")) {
        // sleep for 1 second
        try {
        Thread.sleep(1000);
        } catch(InterruptedException ex) {
        Thread.currentThread().interrupt();
        }
        System.out.print("** checking status: ");
        // check for status change
        GetTasksResponse gtr = omp.getTasks(taskID);
        reportStatus = gtr.getTaskStatuses().get(taskID);
        System.out.println(reportStatus);
        }
        System.out.println("===  Get Report Formats ======================================================".substring(0, 65));
        GetReportFormatsResponse reportFormats = omp.getReportFormats();
        String reportFormatID = reportFormats.getReportFormatIDsByName().get("XML");
        System.out.println("===  Get Report ======================================================".substring(0, 65));
        GetReportsResponse reports = omp.getReports(reportID, reportFormatID);
        String report = reports.toXML();
        System.out.println("===  Print Report ======================================================".substring(0, 65));
        System.out.println(report);
        } catch (IOException ex) {
        System.out.println("Encountered an IOException:");
        System.out.println(ex);
        ex.printStackTrace();
        } catch (InterruptedException ex) {
        System.out.println("Encountered an InterruptedException:");
        System.out.println(ex);
        } catch (ParsingException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
         **/





        
    }
    
    
    public void testCommands() throws IOException, InterruptedException {
        Proc cmd;
        Proc proc;
        
        System.out.println("===  new GetConfigs() ================================================".substring(0, 65));
        proc = createOMPProc();
        proc.addParameter("-iX", new GetConfigs().toXML());
        proc.addListener(createProcListener());
        proc.exec();
        System.out.println();System.out.println();System.out.println();
        
        
    }
    private String ompCommand() {
        return "omp";
    }
    
    public Proc createOMPProc() {
        Proc proc = new Proc(ompCommand());
        proc.addParameter("-u", username);
        proc.addParameter("-w", password);
        proc.addParameter("-h", host);
        proc.addParameter("-p", port);
        return proc;
    }
    
    public ProcListener createProcListener() {
        return new ProcListener() {
            @Override
            public void start(String command, List<String> parameters) {
                String cmd = command;
                for (String param : parameters) {
                    cmd += " " + param;
                }
                
                System.out.println("listener: start");
                System.out.println("command: " );
                System.out.println(cmd);
            }
            
            @Override
            public void line(String line) {
                    
            }
            
            @Override
            public void errorLine(String error) {
                System.out.println("error: " + error);
            }
            
            @Override
            public void finished(String output, String error, int exitValue) {
                System.out.println("listener: finished");
                System.out.println("has output? " + !(output == null || output.equals("")));
                System.out.println("has errors? " + !(error == null || error.equals("")));
                System.out.println("exitValue: " + exitValue);
            }
        };
    }
    
}
