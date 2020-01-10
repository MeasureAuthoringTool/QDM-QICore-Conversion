package mat.server;

import lombok.extern.slf4j.Slf4j;
import mat.client.shared.CQLWorkSpaceConstants;
import mat.model.cql.*;
import mat.server.service.impl.XMLMarshalUtil;
import mat.server.util.XmlProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Slf4j
public class CQLUtilityClass {
    public static final String VERSION = " version ";

    private static final String PATIENT = "Patient";
    private static final String POPULATION = "Population";
    private static StringBuilder toBeInsertedAtEnd;
    private static int size;

    public static CQLModel getCQLModelFromXML(String xmlString) {
        CQLModel cqlModel = new CQLModel();
        XmlProcessor measureXMLProcessor = new XmlProcessor(xmlString);
        String cqlLookUpXMLString = measureXMLProcessor.getXmlByTagName("cqlLookUp");

        if (StringUtils.isNotBlank(cqlLookUpXMLString)) {
            try {
                XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();
                cqlModel = (CQLModel) xmlMarshalUtil.convertXMLToObject("CQLModelMapping.xml", cqlLookUpXMLString, CQLModel.class);
            } catch (Exception e) {
                log.error("Error while getting codesystems", e);
            }
        }

        if (!cqlModel.getValueSetList().isEmpty()) {
            cqlModel.setValueSetList(filterValuesets(cqlModel.getValueSetList()));
            ArrayList<CQLQualityDataSetDTO> valueSetsList = new ArrayList<CQLQualityDataSetDTO>();
            valueSetsList.addAll(cqlModel.getValueSetList());
            cqlModel.setAllValueSetAndCodeList(valueSetsList);
        }

        if (!cqlModel.getCodeList().isEmpty()) {
            sortCQLCodeDTO(cqlModel.getCodeList());
            //Combine Codes and Value sets in allValueSetList for UI
            List<CQLQualityDataSetDTO> dtoList = convertCodesToQualityDataSetDTO(cqlModel.getCodeList());
            if (!dtoList.isEmpty()) {
                cqlModel.getAllValueSetAndCodeList().addAll(dtoList);
            }
        }
        return cqlModel;
    }

    public static String getCqlString(CQLModel cqlModel, String toBeInserted) {
        return getCqlString(cqlModel, toBeInserted, true, 2);
    }

    public static String getCqlString(CQLModel cqlModel, String toBeInserted, boolean isSpaces, int indentSize) {
        StringBuilder cqlStr = new StringBuilder();
        toBeInsertedAtEnd = new StringBuilder();
        // library Name and Using
        cqlStr.append(CQLUtilityClass.createLibraryNameSection(cqlModel));

        //includes
        cqlStr.append(CQLUtilityClass.createIncludesSection(cqlModel.getCqlIncludeLibrarys()));

        //CodeSystems
        cqlStr.append(CQLUtilityClass.createCodeSystemsSection(cqlModel.getCodeList()));

        //Valuesets
        cqlStr.append(CQLUtilityClass.createValueSetsSection(cqlModel.getValueSetList()));

        //Codes
        cqlStr.append(CQLUtilityClass.createCodesSection(cqlModel.getCodeList()));

        // parameters
        CQLUtilityClass.createParameterSection(cqlModel.getCqlParameters(), cqlStr, toBeInserted);

        // Definitions and Functions by Context
        if (!cqlModel.getDefinitionList().isEmpty() || !cqlModel.getCqlFunctions().isEmpty()) {
            getDefineAndFunctionsByContext(cqlModel.getDefinitionList(), cqlModel.getCqlFunctions(), cqlStr, toBeInserted, isSpaces, indentSize);
        } else {
            cqlStr.append("context").append(" " + PATIENT).append("\n\n");
        }


        return cqlStr.toString();

    }

    private static String createLibraryNameSection(CQLModel cqlModel) {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(cqlModel.getLibraryName())) {

            sb.append("library ").append(cqlModel.getLibraryName());
            sb.append(VERSION).append("'" + cqlModel.getVersionUsed()).append("'");
            sb.append(System.lineSeparator()).append(System.lineSeparator());

            if (StringUtils.isNotBlank(cqlModel.getLibraryComment())) {
                sb.append(createCommentString(cqlModel.getLibraryComment()));
                sb.append(System.lineSeparator()).append(System.lineSeparator());
            }

            sb.append("using ").append(cqlModel.getUsingName());
            sb.append(VERSION);
            sb.append("'").append(cqlModel.getQdmVersion()).append("'");
            sb.append("\n\n");
        }

        return sb.toString();
    }


    private static List<CQLQualityDataSetDTO> filterValuesets(List<CQLQualityDataSetDTO> cqlValuesets) {

        cqlValuesets.removeIf(c -> c.getDataType() != null &&
                (c.getDataType().equalsIgnoreCase("Patient characteristic Birthdate")
                        || c.getDataType().equalsIgnoreCase("Patient characteristic Expired")));

        sortCQLQualityDataSetDto(cqlValuesets);

        return cqlValuesets;
    }

    public static List<CQLQualityDataSetDTO> sortCQLQualityDataSetDto(List<CQLQualityDataSetDTO> cqlQualityDataSetDTOs) {

        cqlQualityDataSetDTOs.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        return cqlQualityDataSetDTOs;
    }


    public static List<CQLCode> sortCQLCodeDTO(List<CQLCode> cqlCodes) {

        cqlCodes.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        return cqlCodes;
    }

    private static List<CQLQualityDataSetDTO> convertCodesToQualityDataSetDTO(List<CQLCode> codeList) {
        List<CQLQualityDataSetDTO> convertedCQLDataSetList = new ArrayList<CQLQualityDataSetDTO>();
        for (CQLCode tempDataSet : codeList) {
            CQLQualityDataSetDTO convertedCQLDataSet = new CQLQualityDataSetDTO();
            convertedCQLDataSet.setName(tempDataSet.getName());
            convertedCQLDataSet.setCodeSystemName(tempDataSet.getCodeSystemName());
            convertedCQLDataSet.setCodeSystemOID(tempDataSet.getCodeSystemOID());

            convertedCQLDataSet.setCodeIdentifier(tempDataSet.getCodeIdentifier());
            convertedCQLDataSet.setId(tempDataSet.getId());
            convertedCQLDataSet.setOid(tempDataSet.getCodeOID());
            convertedCQLDataSet.setVersion(tempDataSet.getCodeSystemVersion());
            convertedCQLDataSet.setDisplayName(tempDataSet.getDisplayName());
            convertedCQLDataSet.setSuffix(tempDataSet.getSuffix());

            convertedCQLDataSet.setReadOnly(tempDataSet.isReadOnly());

            convertedCQLDataSet.setType("code");
            convertedCQLDataSetList.add(convertedCQLDataSet);


        }
        return convertedCQLDataSetList;

    }


    private static String createIncludesSection(List<CQLIncludeLibrary> includeLibList) {
        StringBuilder sb = new StringBuilder();
        if (!CollectionUtils.isEmpty(includeLibList)) {
            for (CQLIncludeLibrary includeLib : includeLibList) {
                sb.append("include ").append(includeLib.getCqlLibraryName());
                sb.append(VERSION).append("'").append(includeLib.getVersion()).append("' ");
                sb.append("called ").append(includeLib.getAliasName());
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String createCodeSystemsSection(List<CQLCode> codeSystemList) {

        StringBuilder sb = new StringBuilder();

        List<String> codeSystemAlreadyUsed = new ArrayList<>();

        if (!CollectionUtils.isEmpty(codeSystemList)) {
            for (CQLCode codes : codeSystemList) {
                if (codes.getCodeSystemOID() != null && !codes.getCodeSystemOID().isEmpty() && !"null".equals(codes.getCodeSystemOID())) {
                    String codeSysStr = codes.getCodeSystemName();
                    String codeSysVersion = "";

                    if (codes.isIsCodeSystemVersionIncluded()) {
                        codeSysStr = codeSysStr + ":" + codes.getCodeSystemVersion().replaceAll(" ", "%20");
                        codeSysVersion = "version 'urn:hl7:version:" + codes.getCodeSystemVersion() + "'";
                    }

                    if (!codeSystemAlreadyUsed.contains(codeSysStr)) {
                        sb.append("codesystem \"").append(codeSysStr).append('"').append(": ");
                        sb.append("'urn:oid:").append(codes.getCodeSystemOID()).append("' ");
                        sb.append(codeSysVersion);
                        sb.append("\n");

                        codeSystemAlreadyUsed.add(codeSysStr);
                    }
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private static String createValueSetsSection(List<CQLQualityDataSetDTO> valueSetList) {
        StringBuilder sb = new StringBuilder();

        List<String> valueSetAlreadyUsed = new ArrayList<>();

        if (!CollectionUtils.isEmpty(valueSetList)) {

            for (CQLQualityDataSetDTO valueset : valueSetList) {

                if (!valueSetAlreadyUsed.contains(valueset.getName())) {

                    String version = valueset.getVersion().replaceAll(" ", "%20");
                    sb.append("valueset ").append('"').append(valueset.getName()).append('"');
                    sb.append(": 'urn:oid:").append(valueset.getOid()).append("' ");
                    //Check if QDM has expansion identifier or not.
                    if (StringUtils.isNotBlank(version) && !version.equals("1.0")) {
                        sb.append("version 'urn:hl7:version:").append(version).append("' ");
                    }
                    sb.append("\n");
                    valueSetAlreadyUsed.add(valueset.getName());
                }

            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private static String createCodesSection(List<CQLCode> codeList) {

        StringBuilder sb = new StringBuilder();

        List<String> codesAlreadyUsed = new ArrayList<String>();

        if (!CollectionUtils.isEmpty(codeList)) {

            for (CQLCode codes : codeList) {

                String codesStr = '"' + codes.getDisplayName() + '"' + ": " + "'" + codes.getCodeOID() + "'";
                String codeSysStr = codes.getCodeSystemName();
                if (codes.isIsCodeSystemVersionIncluded()) {
                    codeSysStr = codeSysStr + ":" + codes.getCodeSystemVersion().replaceAll(" ", "%20");
                }

                if (!codesAlreadyUsed.contains(codesStr)) {
                    sb.append("code ").append(codesStr).append(" ").append("from ");
                    sb.append('"').append(codeSysStr).append('"').append(" ");
                    sb.append("display " + "'" + escapeSingleQuote(codes) + "'");
                    sb.append("\n");
                    codesAlreadyUsed.add(codesStr);
                }

            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private static StringBuilder createParameterSection(List<CQLParameter> paramList, StringBuilder cqlStr, String toBeInserted) {
        if (!CollectionUtils.isEmpty(paramList)) {

            for (CQLParameter parameter : paramList) {

                String param = "parameter " + "\"" + parameter.getName() + "\"";

                if (StringUtils.isNotBlank(parameter.getCommentString())) {
                    cqlStr.append(createCommentString(parameter.getCommentString()));
                    cqlStr.append(System.lineSeparator());
                }

                cqlStr.append(param + " " + parameter.getLogic());
                cqlStr.append("\n");

                // if the the param we just appended is the current one, then
                // find the size of the file at that time.
                // This will give us the end line of the parameter we are trying to insert.
                if (param.equalsIgnoreCase(toBeInserted)) {
                    size = getEndLine(cqlStr.toString());
                }

            }

            cqlStr.append("\n");
        }

        return cqlStr;
    }


    private static StringBuilder getDefineAndFunctionsByContext(
            List<CQLDefinition> defineList, List<CQLFunctions> functionsList,
            StringBuilder cqlStr, String toBeInserted, boolean isSpaces, int indentSize) {

        List<CQLDefinition> contextPatDefineList = new ArrayList<CQLDefinition>();
        List<CQLDefinition> contextPopDefineList = new ArrayList<CQLDefinition>();
        List<CQLFunctions> contextPatFuncList = new ArrayList<CQLFunctions>();
        List<CQLFunctions> contextPopFuncList = new ArrayList<CQLFunctions>();

        if (defineList != null) {
            for (int i = 0; i < defineList.size(); i++) {
                if (defineList.get(i).getContext().equalsIgnoreCase(PATIENT)) {
                    contextPatDefineList.add(defineList.get(i));
                } else {
                    contextPopDefineList.add(defineList.get(i));
                }
            }
        }
        if (functionsList != null) {
            for (int i = 0; i < functionsList.size(); i++) {
                if (functionsList.get(i).getContext().equalsIgnoreCase(PATIENT)) {
                    contextPatFuncList.add(functionsList.get(i));
                } else {
                    contextPopFuncList.add(functionsList.get(i));
                }
            }
        }

        if ((!contextPatDefineList.isEmpty()) || (!contextPatFuncList.isEmpty())) {

            getDefineAndFunctionsByContext(contextPatDefineList, contextPatFuncList, PATIENT, cqlStr, toBeInserted, isSpaces, indentSize);
        }

        if ((!contextPopDefineList.isEmpty()) || (!contextPopFuncList.isEmpty())) {

            getDefineAndFunctionsByContext(contextPopDefineList, contextPopFuncList, POPULATION, cqlStr, toBeInserted, isSpaces, indentSize);
        }

        return cqlStr;

    }

    private static StringBuilder getDefineAndFunctionsByContext(
            List<CQLDefinition> definitionList,
            List<CQLFunctions> functionsList, String context,
            StringBuilder cqlStr, String toBeInserted, boolean isSpaces, int indentSize) {


        cqlStr = cqlStr.append("context").append(" " + context).append("\n\n");
        for (CQLDefinition definition : definitionList) {

            if (StringUtils.isNotBlank(definition.getCommentString())) {
                cqlStr.append(createCommentString(definition.getCommentString()));
                cqlStr.append(System.lineSeparator());
            }

            String def = "define " + "\"" + definition.getName() + "\"";

            cqlStr = cqlStr.append(def + ":\n");
            cqlStr = cqlStr.append(getWhiteSpaceString(isSpaces, indentSize) + definition.getLogic().replaceAll("\\n", "\n" + getWhiteSpaceString(isSpaces, indentSize)));
            cqlStr = cqlStr.append("\n\n");

            // if the the def we just appended is the current one, then
            // find the size of the file at that time. ;-
            // This will give us the end line of the definition we are trying to insert.
            if (def.equalsIgnoreCase(toBeInserted)) {
                size = getEndLine(cqlStr.toString());
            }

        }

        for (CQLFunctions function : functionsList) {

            if (StringUtils.isNotBlank(function.getCommentString())) {
                cqlStr.append(createCommentString(function.getCommentString()));
                cqlStr.append(System.lineSeparator());
            }

            String func = "define function " + "\"" + function.getName() + "\"";


            cqlStr = cqlStr.append(func + "(");
            if (function.getArgumentList() != null && !function.getArgumentList().isEmpty()) {
                for (CQLFunctionArgument argument : function.getArgumentList()) {
                    StringBuilder argumentType = new StringBuilder();
                    if (argument.getArgumentType().equalsIgnoreCase("QDM Datatype")) {
                        argumentType = argumentType.append("\"").append(argument.getQdmDataType());
                        if (argument.getAttributeName() != null) {
                            argumentType = argumentType.append(".").append(argument.getAttributeName());
                        }
                        argumentType = argumentType.append("\"");
                    } else if (argument.getArgumentType().equalsIgnoreCase(
                            CQLWorkSpaceConstants.CQL_OTHER_DATA_TYPE)) {
                        argumentType = argumentType.append(argument.getOtherType());
                    } else {
                        argumentType = argumentType.append(argument.getArgumentType());
                    }
                    cqlStr = cqlStr.append(argument.getArgumentName() + " " + argumentType + ", ");
                }
                cqlStr.deleteCharAt(cqlStr.length() - 2);
            }

            cqlStr = cqlStr.append("):\n" + getWhiteSpaceString(isSpaces, indentSize) + function.getLogic().replaceAll("\\n", "\n" + getWhiteSpaceString(isSpaces, indentSize)));
            cqlStr = cqlStr.append("\n\n");

            // if the the func we just appended is the current one, then
            // find the size of the file at that time.
            // This will give us the end line of the function we are trying to insert.
            if (func.equalsIgnoreCase(toBeInserted)) {
                size = getEndLine(cqlStr.toString());
            }
        }

        return cqlStr;
    }


    public static String createCommentString(String comment) {
        StringBuilder sb = new StringBuilder();
        sb.append("/*").append(comment).append("*/");
        return sb.toString();
    }


    private static int getEndLine(String cqlString) {

        Scanner scanner = new Scanner(cqlString);

        int endLine = -1;
        while (scanner.hasNextLine()) {
            endLine++;
            scanner.nextLine();
        }

        scanner.close();
        return endLine;
    }

    public static String getWhiteSpaceString(boolean isSpaces, int indentSize) {
        String whiteSpaceString = "";
        for (int i = 0; i < indentSize; i++) {
            if (isSpaces) {
                whiteSpaceString += " ";
            } else {
                whiteSpaceString += "\t";
            }
        }

        return whiteSpaceString;
    }

    /**
     * Method will add multiple escape(backslash) character's.Eevaluate 4 \ to 2 \ and So final will have 2 \.
     *
     * @param codes
     * @return
     */
    private static String escapeSingleQuote(CQLCode codes) {
        return codes.getName().replaceAll("'", "\\\\'");
    }

}
