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
public class GetResultsResponse extends OMPResponse {
    
    private Elements results;
    
    public GetResultsResponse(Document response) {
        super(response);
        this.results = root.getChildElements("result");
    }
    
    /**
     * Returns a map of id => name pairs for each report.
     * 
     * @return 
     */
    public Map<String, String> getReportNames() {
        HashMap<String, String> map = new HashMap<>();
        Element report;
        
        for (int i = 0; i < results.size(); i++) {
            report = results.get(i);            
            getSyslogAtributes(report);
            map.put(getID(report), getName(report));
        }
        
        return map;
    }
    
    public ArrayList<ArrayList> getSyslogInfo(){
        ArrayList<ArrayList> resp  = new ArrayList<ArrayList>();
        Element result;
        
        for (int i = 0; i < results.size(); i++) {
            result = results.get(i);            
            resp.add(getSyslogAtributes(result));
        }
        return resp;
    }
    
    private String getID(Element report) {
        return report.getAttributeValue("id");
    }
    
    private String getName(Element report) {
        return report.getFirstChildElement("name").getValue();
    }
    
    private ArrayList getSyslogAtributes(Element result){
        ArrayList<String> infoSyslog = new ArrayList<String>();
        Element name = result.getFirstChildElement("name");
        Element host = result.getFirstChildElement("host");
        Element severity = result.getFirstChildElement("severity");
        String cve = "";
        
        Elements nvts = result.getChildElements("nvt");
        for (int x = 0; x < nvts.size(); x++) {
            Element nvt = nvts.get(x);
            cve = nvt.getFirstChildElement("cve").getValue();
            
        }
        
        
                    
        infoSyslog.add("|"+name.getValue()+"|"+host.getValue().trim()+"|"+severity.getValue()+"|"+cve);
                
         
        
        
        return infoSyslog;
         

        
    }
}
