package gen;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

public class XSLTCreator {

    //constants
    private static final String STRING_TYPE = "string";
    private static final String OBJECT_TYPE = "object";
    private static final String ARRAY_TYPE = "array";
    private static final String NUMBER_TYPE = "number";
    private static final String BOOLEAN_TYPE = "boolean";
    private static final String TYPE = "type";
    private static final String ITEMS_TYPE = "items_type";
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String TREE_NODE= "treeNode";
    private static final String OPERATOR_TYPE = "operatorType";

    //arithmetic operators
    private static final String ADD = "ADD";
    private static final String SUBTRACT = "SUBTRACT";
    private static final String DIVIDE = "DIVIDE";
    private static final String MULTIPLY = "MULTIPLY";
    private static final String CEILING = "CEILING";
    private static final String FLOOR = "FLOOR";
    private static final String ROUND = "ROUND";
    private static final String SET_PRECISION = "SET_PRECISION";
    private static final String ABSOLUTE = "ABSOLUTE";
    private static final String MIN = "MIN";
    private static final String MAX = "MAX";


    //common operators
    private static final String CONSTANT = "CONSTANT";
    private static final String GLOBAL_VARIABLE = "GLOBAL_VARIABLE";
    private static final String PROPERTIES = "PROPERTIES";
    private static final String COMPARE = "COMPARE";

    //conditional operators
    private static final String IF_ELSE = "IF_ELSE";

    //string operators
    private static final String CONCAT = "CONCAT";
    private static final String LOWERCASE = "LOWERCASE";
    private static final String UPPERCASE = "UPPERCASE";
    private static final String STRING_LENGTH = "STRING_LENGTH";
    private static final String SPLIT = "SPLIT";
    private static final String STARTS_WITH = "STARTS_WITH";
    private static final String ENDS_WITH = "ENDS_WITH";
    private static final String SUBSTRING = "SUBSTRING";
    private static final String REPLACE = "REPLACE";
    private static final String MATCH = "MATCH";
    private static final String TRIM = "TRIM";

    //type_conversion operators
    private static final String TO_STRING = "TO_STRING";
    private static final String STRING_TO_NUMBER = "STRING_TO_NUMBER";
    private static final String STRING_TO_BOOLEAN = "STRING_TO_BOOLEAN";

    //boolean operators
    private static final String NOT = "NOT";
    private static final String AND = "AND";
    private static final String OR = "OR";

    //XML attributes
    private static final String NAME = "name";
    private static final String SELECT = "select";
    private static final String TEST = "test";

    //xsl tag names
    private static final String XSL_VALUE_OF = "xsl:value-of";
    private static final String XSL_PARAM = "xsl:param";
    private static final String XSL_IF = "xsl:if";
    private static final String XSL_FOR_EACH = "xsl:for-each";
    private static final String XSL_TEMPLATE = "xsl:template";

    //node types
    private static final String AT_NODE = "/@node";
    private static final String AT_OPERATORS = "//@operators";


    private static ArrayList<InPutNode> inPutNodes;
    private static ArrayList<OutPutNode> outPutNodes;
    private static InPutNode currentInNode;
    private static ArrayList<OperatorNode> operatorNodes;

    public static void main(String[] args){
        transform("/home/danushka/Downloads/output.xml",
                "/home/danushka/workspace/SampleFlowRegistry/NewConfig12.datamapper");
    }

    private static void transform(String outputFilePath, String styleSheetFilePath){
        ReadXMLFile inputXML = new ReadXMLFile(styleSheetFilePath);
        WriteXMLFile outputXML = new WriteXMLFile(outputFilePath);
        Element rootElement = outputXML.getDocument().createElement("xsl:stylesheet");
        rootElement.setAttribute("xmlns:xsl","http://www.w3.org/1999/XSL/Transform");
        rootElement.setAttribute("xmlns:xs","http://www.w3.org/2001/XMLSchema");
        rootElement.setAttribute("version","2.0");
        rootElement.setAttribute("xmlns:own","http://whatever");
        outputXML.getDocument().appendChild(rootElement);

        //Setting up setPrecisionFunction
        setPrecisionFunction(rootElement,outputXML);
        //End of setPrecisionFunction

        Element templateElement = outputXML.getDocument().createElement(XSL_TEMPLATE);
        templateElement.setAttribute("match","/");
        rootElement.appendChild(templateElement);
        createOperatorNodes(inputXML);
        createInputNode(inputXML);
        createOutputNode(inputXML,outputXML, templateElement);
        for (OutPutNode outPutNode:outPutNodes) {
            traverseOutPutNode(outPutNode,outputXML,templateElement);
        }
        outputXML.saveFile();
    }

    private static void setPrecisionFunction(Element rootElement, WriteXMLFile outputXML){
        Element setPrecisionFunction = outputXML.getDocument().createElement("xsl:function");
        setPrecisionFunction.setAttribute(NAME,"own:setPrecision");
        Element resultString = outputXML.getDocument().createElement(XSL_PARAM);
        resultString.setAttribute(NAME,"resultString");
        setPrecisionFunction.appendChild(resultString);
        Element noOfDigits = outputXML.getDocument().createElement(XSL_PARAM);
        noOfDigits.setAttribute(NAME,"noOfDigits");
        setPrecisionFunction.appendChild(noOfDigits);
        Element firstIf = outputXML.getDocument().createElement(XSL_IF);
        firstIf.setAttribute(TEST,"$noOfDigits=0");
        setPrecisionFunction.appendChild(firstIf);
        Element firstValue = outputXML.getDocument().createElement(XSL_VALUE_OF);
        firstValue.setAttribute(SELECT,"$resultString");
        firstIf.appendChild(firstValue);
        Element secondIf = outputXML.getDocument().createElement(XSL_IF);
        secondIf.setAttribute(TEST,"$noOfDigits!=0");
        setPrecisionFunction.appendChild(secondIf);
        Element secondValue = outputXML.getDocument().createElement(XSL_VALUE_OF);
        secondValue.setAttribute(SELECT,"own:setPrecision(concat($resultString,'0'),$noOfDigits - 1)");
        secondIf.appendChild(secondValue);
        rootElement.appendChild(setPrecisionFunction);
    }

    private static void createOutputNode(ReadXMLFile inputXMLFile, WriteXMLFile outputXMLFile, Element templateElement){
        outPutNodes = new ArrayList<>();
        Node outputNode = inputXMLFile.getDocument().getElementsByTagName(OUTPUT).item(0);
        for(OperatorNode operatorNode:operatorNodes){
            if(operatorNode.getProperty(OPERATOR_TYPE).equals(GLOBAL_VARIABLE)){
                Element v = outputXMLFile.getDocument().createElement(XSL_PARAM);
                v.setAttribute(NAME,"operators."+Integer.toString(operatorNodes.indexOf(operatorNode)));
                v.setAttribute("global_name",operatorNode.getProperty(NAME));
                String defaultValue = operatorNode.getProperty("defaultValue");
                v.setAttribute(SELECT,"'"+defaultValue+"'");
                templateElement.appendChild(v);
            }
        }
        for(int i=0;i<outputNode.getChildNodes().getLength();i++){
            if(outputNode.getChildNodes().item(i).getNodeName().equals(TREE_NODE)){
                outPutNodes.add(new OutPutNode(outputNode.getChildNodes().item(i), ""));
            }
        }
    }

    private static void createInputNode(ReadXMLFile inputXMLFile){
        inPutNodes = new ArrayList<>();
        Node inputNode = inputXMLFile.getDocument().getElementsByTagName(INPUT).item(0);
        for(int i=0;i<inputNode.getChildNodes().getLength();i++){
            if(inputNode.getChildNodes().item(i).getNodeName().equals(TREE_NODE)){
                inPutNodes.add(new InPutNode(inputNode.getChildNodes().item(i),null,""));
            }
        }
    }

    private static void createOperatorNodes(ReadXMLFile inputXMLFile){
        operatorNodes = new ArrayList<>();
        NodeList operators = inputXMLFile.getDocument().getElementsByTagName("operators");
        for(int i=0;i<operators.getLength();i++){
            operatorNodes.add(new OperatorNode(operators.item(i)));
        }
    }

    private static void traverseOutPutNode(OutPutNode node, WriteXMLFile outputXMLFile, Element parentElement){
        if(node.getProperties().get(TYPE).equals(STRING_TYPE) || node.getProperties().get(TYPE).equals(NUMBER_TYPE) ||
                node.getProperties().get(TYPE).equals(BOOLEAN_TYPE)){
            if(node.getInNode().getOutNodes().size()>0){
                Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                parentElement.appendChild(currentElement);
                Element valueOfElement = outputXMLFile.getDocument().createElement(XSL_VALUE_OF);
                currentElement.appendChild(valueOfElement);
                valueOfElement.setAttribute(SELECT,getValueFromMapping(node.getInNode().getOutNodes().get(0)));
            }
        }else if(node.getProperties().get(TYPE).equals(ARRAY_TYPE)){
            InPutNode previousCurrentNode = currentInNode;
            InPutNode inNode = traverseArray(node,operatorNodes);
            if(inNode!=null){
                String arrayPath = getArrayPath(inNode);
                if(!arrayPath.equals("")) {
                    Element forEachElement = outputXMLFile.getDocument().createElement(XSL_FOR_EACH);
                    forEachElement.setAttribute(SELECT, arrayPath);
                    parentElement.appendChild(forEachElement);
                    Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                    forEachElement.appendChild(currentElement);
                    if (node.getProperty(ITEMS_TYPE).equals(OBJECT_TYPE)) {
                        for (OutPutNode childNode : node.getChildNodes()) {
                            traverseOutPutNode(childNode, outputXMLFile, currentElement);
                        }
                    } else if (node.getInNode().getOutNodes().size() > 0) {
                        Element valueOfElement = outputXMLFile.getDocument().createElement(XSL_VALUE_OF);
                        currentElement.appendChild(valueOfElement);
                        valueOfElement.setAttribute(SELECT, getValueFromMapping(node.getInNode().getOutNodes().get(0)));
                    }
                }
            }else if(node.getInNode().getOutNodes().size()>0){
                Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                parentElement.appendChild(currentElement);
                Element valueOfElement = outputXMLFile.getDocument().createElement(XSL_VALUE_OF);
                currentElement.appendChild(valueOfElement);
                valueOfElement.setAttribute(SELECT,getValueFromMapping(node.getInNode().getOutNodes().get(0)));
            }
            currentInNode = previousCurrentNode;
        }else{
            Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
            parentElement.appendChild(currentElement);
            for(OutPutNode childNode:node.getChildNodes()){
                traverseOutPutNode(childNode,outputXMLFile,currentElement);
            }
        }
    }

    private static String getArrayPath(InPutNode node){
        if(currentInNode==null){
            InPutNode tempNode = node;
            while (tempNode.getParentNode()!=null){
                if(tempNode.getParentNode().getProperty(TYPE).equals(ARRAY_TYPE)){
                    node = tempNode.getParentNode();
                }
                tempNode = tempNode.getParentNode();
            }
            currentInNode = node;
            return node.getXPath();
        }else{
            if(node==currentInNode){
                return "";
            }
            InPutNode tempNode = node;
            while (tempNode.getParentNode()!=null && currentInNode!=tempNode.getParentNode()){
                if(tempNode.getParentNode().getProperty(TYPE).equals(ARRAY_TYPE)){
                    node = tempNode.getParentNode();
                }
                tempNode = tempNode.getParentNode();
            }
            String path = "";
            tempNode = node;
            while (tempNode.getParentNode()!=null && tempNode!=currentInNode){
                if(path.equals("")){
                    path = tempNode.getName();
                }else{
                    path =tempNode.getName()+"/"+path;
                }
                tempNode = tempNode.getParentNode();
            }
            if(!path.equals("")){
                currentInNode = node;
            }
            return path;
        }
    }

    private static String getValueFromMapping(String inNode){
        if(isOperator(inNode)){
            String index = inNode.substring(13,14);
            int currentIndex = 14;
            boolean moreDigits = true;
            while (moreDigits){
                try{
                    String nextDigit = inNode.substring(currentIndex,currentIndex+1);
                    if(nextDigit.equals("/")){
                        moreDigits = false;
                    }else {
                        index += nextDigit;
                        currentIndex++;
                    }
                }catch (Exception e){
                    moreDigits = false;
                }
            }
            OperatorNode operatorNode = operatorNodes.get(Integer.parseInt(index));
            switch (operatorNode.getProperty(OPERATOR_TYPE)){
                case CONSTANT:
                    String type = operatorNode.getProperty(TYPE);
                    if(type!=null){
                        if(type.equals("BOOLEAN")){
                            return "('"+operatorNode.getProperty("constantValue")+"' eq 'true')";
                        }else if(type.equals("NUMBER")){
                            return "number( '"+operatorNode.getProperty("constantValue")+"' )";
                        }
                    }
                    return "'"+operatorNode.getProperty("constantValue")+"'";
                case CONCAT:
                    if(operatorNode.getLeftContainer().getInNodes().size()==2){
                        return "concat("+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+","+
                                getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+")";
                    }
                    break;
                case LOWERCASE:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "lower-case(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().
                                get(0)) + ")";
                    }
                    break;
                case UPPERCASE:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "upper-case(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().
                                get(0)) + ")";
                    }
                    break;
                case STRING_LENGTH:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "string-length(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().
                                get(0)) + ")";
                    }
                    break;
                case SPLIT:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "tokenize(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)) +
                                ",'" +operatorNode.getProperty("delimiter")+"')["+Integer.toString(Integer.
                                parseInt(inNode.substring(inNode.lastIndexOf("@rightConnectors.")+17,inNode.
                                        lastIndexOf("@rightConnectors.")+18))+1)+"]";
                    }
                    break;
                case TO_STRING:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "string(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)) +
                                ")";
                    }
                    break;
                case STRING_TO_NUMBER:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "number(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)) +
                                ")";
                    }
                    break;
                case STRING_TO_BOOLEAN:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" eq 'true'";
                    }
                    break;
                case NOT:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "not(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)) + ")";
                    }
                    break;
                case AND:
                    if(operatorNode.getLeftContainer().getInNodes().size()>1) {
                        String value = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0));
                        for(int i=1;i<operatorNode.getLeftContainer().getInNodes().size();i++){
                            value = " ( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(i))+
                                    " ) and ( "+value+" )";
                        }
                        return value;
                    }
                    break;
                case OR:
                    if(operatorNode.getLeftContainer().getInNodes().size()>1) {
                        String value = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0));
                        for(int i=1;i<operatorNode.getLeftContainer().getInNodes().size();i++){
                            value = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(i))+
                                    " or "+value;
                        }
                        return value;
                    }
                    break;
                case STARTS_WITH:
                    if(operatorNode.getLeftContainer().getInNodes().size()==1) {
                        String value = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0));
                        return "starts-with("+value+" , '"+operatorNode.getAttributes().get("pattern")+"')";
                    }else if(operatorNode.getLeftContainer().getInNodes().size()==2) {
                        String string = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0));
                        String exp = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        return "starts-with("+string+" , "+exp+")";
                    }
                    break;
                case ENDS_WITH:
                    if(operatorNode.getLeftContainer().getInNodes().size()==1) {
                        String value = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0));
                        return "ends-with("+value+" , '"+operatorNode.getAttributes().get("pattern")+"')";
                    }else if(operatorNode.getLeftContainer().getInNodes().size()==2) {
                        String string = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0));
                        String exp = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        return "ends-with("+string+" , "+exp+")";
                    }
                    break;
                case SUBSTRING:
                    String startIndex = operatorNode.getAttributes().get("startIndex");
                    String endIndex = operatorNode.getAttributes().get("endIndex");
                    if(startIndex!=null){
                        startIndex = "number('"+startIndex+"')";
                    }
                    if(endIndex!=null){
                        endIndex = "number('"+endIndex+"')";
                    }
                    if(startIndex==null && endIndex==null){
                        if (operatorNode.getLeftContainer().getInNodes().size()==3) {
                            startIndex = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            endIndex = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(2));
                        }
                    }else if(startIndex==null) {
                        if (operatorNode.getLeftContainer().getInNodes().size()>1) {
                            startIndex = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        }
                    }else if(endIndex==null) {
                        if (operatorNode.getLeftContainer().getInNodes().size()>1) {
                            endIndex = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        }
                    }
                    if(startIndex!=null && endIndex!=null){
                        return "substring( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+
                                " , "+startIndex+" , "+startIndex+" + "+endIndex+" )";
                    }
                    break;
                case REPLACE:
                    String target = operatorNode.getAttributes().get("target");
                    String replaceString = operatorNode.getAttributes().get("replaceString");
                    if(target!=null){
                        target = "'"+target+"'";
                    }
                    if(replaceString!=null){
                        replaceString = "'"+replaceString+"'";
                    }
                    if(target==null && replaceString==null){
                        if (operatorNode.getLeftContainer().getInNodes().size()==3) {
                            target = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            replaceString = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(2));
                        }
                    }else if(target==null) {
                        if (operatorNode.getLeftContainer().getInNodes().size()>1) {
                            target = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        }
                    }else if(replaceString==null) {
                        if (operatorNode.getLeftContainer().getInNodes().size()>1) {
                            replaceString = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        }
                    }
                    if(target!=null && replaceString!=null){
                        return "replace( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+
                                " , "+target+" , "+replaceString+" )";
                    }
                    break;
                case MATCH:
                    String pattern = operatorNode.getAttributes().get("pattern");
                    if(pattern==null){
                        if (operatorNode.getLeftContainer().getInNodes().size()==2) {
                            pattern = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        }
                    }else {
                        pattern = "'"+pattern.substring(1,pattern.length()-1)+"'";
                    }
                    if(pattern!=null){
                        return "matches( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+
                                " , "+pattern+" )";
                    }
                    break;
                case TRIM:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0){
                        return "replace(replace("+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().
                                get(0))+",'\\s+$',''),'^\\s+','')";
                    }
                    break;
                case GLOBAL_VARIABLE:
                    return "$operators."+Integer.toString(operatorNodes.indexOf(operatorNode))+"";
                case PROPERTIES:
                    return "";
                case COMPARE:
                    String comparisonOperator = operatorNode.getAttributes().get("comparisonOperator");
                    if(comparisonOperator==null){
                        if(operatorNode.getLeftContainer().getInNodes().size()==2){
                            return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" = "+
                                    getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        }
                    }else {
                        switch (comparisonOperator){
                            case "!=":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" != "+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            case "!==":
                                return "not( if((string("+getValueFromMapping(operatorNode.getLeftContainer().
                                        getInNodes().get(0))+") castable as xs:double) or (string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+
                                        ") castable as xs:double)) then( if((string("+getValueFromMapping(operatorNode.
                                        getLeftContainer().getInNodes().get(0))+
                                        ") castable as xs:double) and (string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+
                                        ") castable as xs:double)) then(true()) else false() ) else if((string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+
                                        ") castable as xs:boolean) or (string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+
                                        ") castable as xs:boolean)) then( if((string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+
                                        ") castable as xs:boolean) and (string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+
                                        ") castable as xs:boolean)) then(true()) else false() ) else true() )";
                            case "===":
                                return "( if((string("+getValueFromMapping(operatorNode.getLeftContainer().getInNodes()
                                        .get(0))+") castable as xs:double) or (string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+
                                        ") castable as xs:double)) then( if((string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+
                                        ") castable as xs:double) and (string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+
                                        ") castable as xs:double)) then(true()) else false() ) else if((string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+
                                        ") castable as xs:boolean) or (string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+
                                        ") castable as xs:boolean)) then( if((string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+
                                        ") castable as xs:boolean) and (string("+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+
                                        ") castable as xs:boolean)) then(true()) else false() ) else true() )";
                            case ">":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" > "+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            case ">=":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" >= "+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            case "<":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" < "+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            case "<=":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" <= "+
                                        getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        }
                    }
                    break;
                case ADD:
                    if(operatorNode.getLeftContainer().getInNodes().size()==2){
                        return "( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" + "+
                                getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+" )";
                    }
                    break;
                case SUBTRACT:
                    if(operatorNode.getLeftContainer().getInNodes().size()==2){
                        return "( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" - "+
                                getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+" )";
                    }
                    break;
                case DIVIDE:
                    if(operatorNode.getLeftContainer().getInNodes().size()==2){
                        return "( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" div "+
                                getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+" )";
                    }
                    break;
                case MULTIPLY:
                    if(operatorNode.getLeftContainer().getInNodes().size()==2){
                        return "( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" * "+
                                getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+" )";
                    }
                    break;
                case CEILING:
                    if(operatorNode.getLeftContainer().getInNodes().size()==1){
                        return "ceiling( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+
                                " )";
                    }
                    break;
                case FLOOR:
                    if(operatorNode.getLeftContainer().getInNodes().size()==1){
                        return "floor( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" )";
                    }
                    break;
                case ROUND:
                    if(operatorNode.getLeftContainer().getInNodes().size()==1){
                        return "round( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" )";
                    }
                    break;
                case ABSOLUTE:
                    if(operatorNode.getLeftContainer().getInNodes().size()==1){
                        return "abs( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" )";
                    }
                    break;
                case SET_PRECISION:
                    String noOfDecimals = operatorNode.getAttributes().get("numberOfDigits");
                    if(noOfDecimals==null){
                        if (operatorNode.getLeftContainer().getInNodes().size()==2) {
                            noOfDecimals = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            return "format-number( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().
                                    get(0))+" ,own:setPrecision('#.',number("+noOfDecimals+")))";
                        }
                    }else if(operatorNode.getLeftContainer().getInNodes().size()==1){
                        return  "format-number( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().
                                get(0))+" ,own:setPrecision('#.',number('"+noOfDecimals+"')))";
                    }
                    break;
                case MIN:
                    if (operatorNode.getLeftContainer().getInNodes().size()>0) {
                        String parameters = "";
                        for(int i=0; i<operatorNode.getLeftContainer().getInNodes().size(); i++){
                            if(i==0){
                                parameters+=getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0));
                            }else {
                                parameters+=" , "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().
                                        get(i));
                            }
                        }
                        return "min( ("+parameters+") )";
                    }
                case MAX:
                    if (operatorNode.getLeftContainer().getInNodes().size()>0) {
                        String parameters = "";
                        for(int i=0; i<operatorNode.getLeftContainer().getInNodes().size(); i++){
                            if(i==0){
                                parameters+=getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0));
                            }else {
                                parameters+=" , "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().
                                        get(i));
                            }
                        }
                        return "max( ("+parameters+") )";
                    }
                case IF_ELSE:
                    if(operatorNode.getLeftContainer().getInNodes().size()==3){
                        return "(if("+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+
                                ")then("+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+
                                ")else "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(2))+")";
                    }
                    break;

            }
        }else{
            InPutNode inPutNode = inPutNodes.get(Integer.parseInt(inNode.substring(19,20)));
            String inNodeString = StringUtils.substring(inNode,20);
            InPutNode currentNode = inPutNode;
            String path="";
            boolean parentFound = false;
            while (inNodeString.contains(AT_NODE)){
                if(parentFound){
                    if(path.equals("")){
                        path = currentNode.getName();
                    }else{
                        path = path+"/"+currentNode.getName();
                    }
                }
                if(currentInNode==currentNode){
                    parentFound = true;
                }
                currentNode = currentNode.getChildNodes().get(Integer.parseInt(inNodeString.substring(7,8)));
                inNodeString = StringUtils.substring(inNodeString,8);
            }
            if(currentInNode==null){
                if(currentNode.getProperty(TYPE).equals(BOOLEAN_TYPE)){
                    return currentNode.getXPath()+" eq 'true' ";
                }else if(currentNode.getProperty(TYPE).equals(NUMBER_TYPE)){
                    return "number( "+currentNode.getXPath()+" )";
                }
                return currentNode.getXPath();
            }else if(parentFound){
                if(path.equals("")){
                    if(currentNode.getProperty(TYPE).equals(BOOLEAN_TYPE)){
                        return currentNode.getName()+" eq 'true' ";
                    }else if(currentNode.getProperty(TYPE).equals(NUMBER_TYPE)){
                        return "number( "+currentNode.getName()+" )";
                    }
                    return currentNode.getName();
                }
                if(currentNode.getProperty(TYPE).equals(BOOLEAN_TYPE)){
                    return path+"/"+currentNode.getName()+" eq 'true' ";
                }else if(currentNode.getProperty(TYPE).equals(NUMBER_TYPE)){
                    return "number( "+path+"/"+currentNode.getName()+" )";
                }
                return path+"/"+currentNode.getName();
            }else{
                String[] cPathParts = currentInNode.getXPath().split("/");
                String[] xPathParts = currentNode.getXPath().split("/");
                boolean matching = true;
                if(!cPathParts[0].equals(xPathParts[0])){
                    matching = false;
                    path = inPutNode.getName();
                }else{
                    cPathParts = removeElementAt(cPathParts,0);
                    xPathParts = removeElementAt(xPathParts,0);
                }
                while ((matching && cPathParts.length>0) && (xPathParts.length>0 &&
                        xPathParts[0].equals(cPathParts[0]))){
                    xPathParts = removeElementAt(xPathParts,0);
                    cPathParts = removeElementAt(cPathParts,0);
                }
                for (String s:xPathParts) {
                    if(path.equals("")){
                        path=s;
                    }else{
                        path=path+"/"+s;
                    }
                }
                for (String s:cPathParts) {
                    if(!s.equals("")){
                        path="../"+path;
                    }
                }
                if(currentNode.getProperty(TYPE).equals(BOOLEAN_TYPE)){
                    return path+" eq 'true' ";
                }else if(currentNode.getProperty(TYPE).equals(NUMBER_TYPE)){
                    return "number( "+path+" )";
                }
                return path;
            }

        }

        return "";
    }

    private static boolean isOperator(String inNode){
        return inNode.startsWith(AT_OPERATORS);
    }

    private static InPutNode traverseArray(OutPutNode node, ArrayList<OperatorNode> operatorNodes){
        ArrayList<InPutNode> arrayNodes = new ArrayList<>();
        if(node.getProperty(TYPE).equals(ARRAY_TYPE)){
            if(node.getProperty(ITEMS_TYPE).equals(OBJECT_TYPE)){
                for (OutPutNode childNode:node.getChildNodes()) {
                    InPutNode returnNode = traverseArray(childNode,operatorNodes);
                    if(returnNode!=null && returnNode.getProperty(TYPE).equals(ARRAY_TYPE)){
                        arrayNodes.add(returnNode);
                    }
                }
            }else if(node.getInNode().getOutNodes().size()>0){
                return getArrayElement(node.getInNode().getOutNodes().get(0),operatorNodes);
            }
        }else if(node.getProperty(TYPE).equals(OBJECT_TYPE)){
            for (OutPutNode childNode:node.getChildNodes()){
                InPutNode returnNode = traverseArray(childNode,operatorNodes);
                if(returnNode!=null && returnNode.getProperty(TYPE).equals(ARRAY_TYPE)){
                    arrayNodes.add(returnNode);
                }
            }
        }else if(node.getInNode().getOutNodes().size()>0){
            return getArrayElement(node.getInNode().getOutNodes().get(0),operatorNodes);
        }
        return getHighestLevelNode(arrayNodes);
    }

    private static InPutNode getArrayElement(String inNode, ArrayList<OperatorNode> operatorNodes){
        if(isOperator(inNode)) {
            OperatorNode operatorNode = operatorNodes.get(Integer.parseInt(inNode.substring(13, 14)));
            ArrayList<InPutNode> inNodes = new ArrayList<>();
            for(String childInNode : operatorNode.getLeftContainer().getInNodes()){
                InPutNode returnInNode = getArrayElement(childInNode,operatorNodes);
                if(returnInNode!=null && returnInNode.getProperty(TYPE).equals(ARRAY_TYPE)){
                    inNodes.add(returnInNode);
                }
            }
            return getHighestLevelNode(inNodes);
        }else {
            InPutNode currentNode = inPutNodes.get(Integer.parseInt(inNode.substring(19, 20)));
            String inNodeString = StringUtils.substring(inNode, 20);
            while (inNodeString.contains(AT_NODE)) {
                currentNode = currentNode.getChildNodes().get(Integer.parseInt(inNodeString.substring(7, 8)));
                inNodeString = StringUtils.substring(inNodeString, 8);
            }
            while (!(currentNode.getProperty(TYPE).equals(ARRAY_TYPE) || currentNode.getParentNode()==null)){
                if(currentNode.getParentNode()!=null){
                    currentNode = currentNode.getParentNode();
                }
            }
            return currentNode;
        }
    }

    private static InPutNode getHighestLevelNode(ArrayList<InPutNode> arrayNodes){
        int maxArray = 0;
        for (int i=0; i<arrayNodes.size(); i++) {
            if(arrayNodes.get(i).getLevel()>arrayNodes.get(maxArray).getLevel()){
                maxArray = i;
            }
        }
        if(arrayNodes.size()>maxArray){
            return arrayNodes.get(maxArray);
        }
        return null;
    }

    private static String[] removeElementAt(String[] array, int removedIdx){
        String[] newArray = new String[array.length-1];
        for(int i=0;i<array.length;i++){
            if(i<removedIdx){
                newArray[i] = array[i];
            }else if(i>removedIdx){
                newArray[i-1]=array[i];
            }
        }
        return newArray;
    }

}