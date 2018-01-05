package com.omp4j.responses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 *
 * @author Brent Jacobs
 */
public class GetReportsResponse extends OMPResponse {
    
    private Elements reports;
    private Elements results;
    
    public GetReportsResponse(Document response) {
        super(response);
        this.reports = root.getChildElements("report");
    }
    
    /**
     * Returns a map of id => name pairs for each report.
     * 
     * @return 
     */
    public Map<String, String> getReportNames() {
        HashMap<String, String> map = new HashMap<>();
        Element report;
        
        for (int i = 0; i < reports.size(); i++) {
            report = reports.get(i);            
            getSyslogAtributes(report);
            map.put(getID(report), getName(report));
        }
        
        return map;
    }
    
    public ArrayList<ArrayList> getSyslogInfo(){
        ArrayList<ArrayList> resp  = new ArrayList<ArrayList>();
        Element report;
        
        for (int i = 0; i < reports.size(); i++) {
            report = reports.get(i);            
            resp.add(getSyslogAtributes(report));
        }
        return resp;
    }
    
    private String getID(Element report) {
        return report.getAttributeValue("id");
    }
    
    private String getName(Element report) {
        return report.getFirstChildElement("name").getValue();
    }
    
    private ArrayList getSyslogAtributes(Element report){
        ArrayList<String> infoSyslog = new ArrayList<String>();
        
        Elements report1 = report.getChildElements("report");
        System.out.println("FOR 1: "+report1.size());        
        for (int i = 0; i < report1.size(); i++) {
            Element report2 = report1.get(i);
            Elements resultados = report2.getChildElements("results");
            System.out.println("FOR 2: "+report1.size());
            for (int x = 0; x < resultados.size(); x++) {
                Element resultados2 = resultados.get(x);
                Elements resultados3 = resultados2.getChildElements("result");
                System.out.println("FOR 3: "+report1.size());
                for (int j = 0; j < resultados3.size(); j++){
                    Element infoResultados = resultados3.get(j);
                    Element name = infoResultados.getFirstChildElement("name");
                    Element host = infoResultados.getFirstChildElement("host");
                    Element severity = infoResultados.getFirstChildElement("severity");
                    
                    
                    
                    infoSyslog.add("|"+name.getValue()+"|"+host.getValue().trim()+"|"+severity.getValue());
                }
            }
        }
        
        
        return infoSyslog;
         

        
    }
}
