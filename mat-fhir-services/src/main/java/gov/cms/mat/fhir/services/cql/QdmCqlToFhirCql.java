/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.cql;

import com.google.common.io.Files;
import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author duanedecouteau
 */
public class QdmCqlToFhirCql {
    private String FHIR_MODEL = "using FHIR version '4.0.0'";
    private List<String> outputList = new ArrayList<String>();
    private List<String> inputList = new ArrayList<String>();
    private StringBuffer sb = new StringBuffer();
    private StringBuffer sbFinal = new StringBuffer();
    private String outputFileName = "/Users/duanedecouteau/Downloads/CMS145v2_FHIR.cql";
    private final RestTemplate restTemplate = new RestTemplate();
    private List<ConversionMapping> cList = new ArrayList<ConversionMapping>();
    private QdmQiCoreDataService qdmQicoreDataService;
    private List<CqlDomainSymbolic> csList = new ArrayList<CqlDomainSymbolic>();
    

    public QdmCqlToFhirCql() {
        loadMappings();
        readInput();        
    }
    
    public QdmCqlToFhirCql(String cqlText) {
        loadMappings();
        String[] lines = cqlText.split(System.getProperty("line.separator"));
        //pop inputList
        for (int i = 0; i < lines.length; i++) {
            inputList.add(lines[i]);
        }
    }
    
    
    private void loadMappings() {
        qdmQicoreDataService = new QdmQiCoreDataService(restTemplate);
        cList = qdmQicoreDataService.findAllFiltered();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        try {
            QdmCqlToFhirCql convert = new QdmCqlToFhirCql();
            convert.processCQLFromMain();  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public String processCQL() {
        processLines();
        processAttributes();
        return sbFinal.toString();
    }
    
    public void processCQLFromMain() {
        processLines();
        processAttributes();
        writeOutput();
    }
    
    private void readInput() {

        try {
            File input = new File("/Users/duanedecouteau/Downloads/CMS146v2_QDM.cql");
            inputList = Files.readLines(input, Charset.defaultCharset());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    private void processAttributes() {
        String firstString = sb.toString();
        String[] lines = firstString.split(System.getProperty("line.separator"));
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String lineAugmented = findAttributesIfAny(line);
            lineAugmented = lineAugmented;
            sbFinal.append(lineAugmented);
        }
    }
    
    private String findAttributesIfAny(String line) {
        String res = line;
        Iterator iter = csList.iterator();
        //check each line for symbolic and find its attribute
        while (iter.hasNext()) {
            CqlDomainSymbolic sym = (CqlDomainSymbolic)iter.next();
            String dotNotation = sym.getSymbolic()+".";
            int dotNotationStart = line.indexOf(dotNotation);
            if (dotNotationStart > -1) {
                //get fhir attribute
                String matDataType = sym.getQdmDomain();
                String subLine = line.substring(dotNotationStart);
                System.out.println(subLine);
                String attributeName = subLine.substring(2);
                System.out.println("Search for: "+matDataType+" "+attributeName+" From "+subLine);
                String fhir = getFHIRAttribute(matDataType, attributeName);
                String original = dotNotation + attributeName;
                String replacement = dotNotation + fhir;
                System.out.println("Replace: "+original+" with "+replacement);
                res = line.replaceAll(original, replacement);
                res = res + "\r\n";
            }
        }
        return res;
    }
    
    private void writeOutput() {
        outputList.forEach(listItem->sbFinal.append(listItem));
        try {
                System.out.println(sbFinal.toString());
                //delete file first
                File f = new File(outputFileName);
                try {
                    f.delete();
                } catch (Exception x) {}
                
                BufferedWriter bwr = new BufferedWriter(new FileWriter(f));
		
		//write contents of StringBuffer to a file
		bwr.write(sb.toString());
		
		//flush the stream
		bwr.flush();
		
		//close the stream
		bwr.close();            
        } catch (Exception ex) {
                ex.printStackTrace();
        }        
    }
    
    
    
    private void processLines() {
        Iterator iter = inputList.iterator();
        while(iter.hasNext()) {
            String lineMain = (String)iter.next();
            //System.out.println(lineMain);
            String res = "\r\n";
            //evaluate keyword
            String line = lineMain;
            StringTokenizer st = new StringTokenizer(line);
            try {
                String keyWord = (String)st.nextElement();
                if (keyWord.equals("library")) {
                    res = processLibraryName(lineMain);
                }
                else if (keyWord.equals("using")) {
                    res = processModelName(lineMain);
                }
                else if (keyWord.equals("define")) {
                    res = processDefines(lineMain);
                }
                else if (keyWord.equals("include")) {
                    res = processIncludes(lineMain);
                }
                else {
                    if (lineMain.indexOf("[") > -1) {
                        res = processDefineSymbolics(lineMain);
                    }
                    else {
                        res = lineMain;
                    }
                }
                res = res + "\r\n";
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            sb.append(res);
        }

    }

    // igmore codesystems and valueset code , parameter context  DEFINE "SDE "

    private String processLibraryName(String line) {
        String res = "";
        StringTokenizer st = new StringTokenizer(line);
        String lib = st.nextToken();
        String nameQDM = st.nextToken();
        String nameFHIR = nameQDM + "_FHIR";
        res = line.replace(nameQDM, nameFHIR);

        return res;        
    }
    
    private String processModelName(String line) {
        //version might change to handle it.
        String res = FHIR_MODEL;
        
        return res;        
    }
    
    private String processDefines(String line) {
        String res = line;
        
        return res;
    }
    
    private String processDefineSymbolics(String line) {
        String res = "";
        String symbolic = "";
        String dataTypeName = "";
        String fhir = "";
        
        StringTokenizer st = new StringTokenizer(line, "]");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int beginIdx = token.indexOf("[\"");
            if (beginIdx > -1) {
                //defining something
                int endIdx = token.indexOf("\":");
                dataTypeName = token.substring(beginIdx + 2, endIdx);
                System.out.println(dataTypeName);
                fhir = getFHIRResourceName(dataTypeName);
                System.out.println("Replace "+dataTypeName+" with "+fhir);
                line = line.replaceAll(dataTypeName, fhir);
                System.out.println(line);
            } else {
                //could be symbolic
                String s = token.trim();
                System.out.println("Symbolic "+dataTypeName+" "+fhir+" "+s);
                CqlDomainSymbolic sym = new CqlDomainSymbolic(dataTypeName, fhir, s);
                csList.add(sym);
            }
            
        }
        res = line;
        return res;
    }
    
    private String processIncludes(String line) {
        //need to add this when example tested
        String res = "";
        res = line;
        
        return res;
    }
    
    private String getFHIRResourceName(String matDataTypeDesc) {
        String res = "Unknown";
        for (ConversionMapping cm : cList) {
            if (cm.getMatDataTypeDescription().equals(matDataTypeDesc) && !cm.getFhirResource().isEmpty()) {
                res = cm.getFhirResource();
                break;
            }
        }
        return res;
    }
    
    private String getFHIRAttribute(String matDataType, String matAttribute) {
        String res = "Unknown";
        for (ConversionMapping cm : cList) {
            if (cm.getMatDataTypeDescription().equals(matDataType.trim()) && cm.getMatAttributeName().equals(matAttribute.trim())) {
                res = cm.getFhirElement();
                break;
            }
        }
        return res;
    }
      
}
