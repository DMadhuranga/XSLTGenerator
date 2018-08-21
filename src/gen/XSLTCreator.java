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

    //arithmatic
    private static final String ADD = "ADD";
    private static final String SUBTRACT = "SUBTRACT";
    private static final String DIVIDE = "DIVIDE";
    private static final String MULTIPLY = "MULTIPLY";
    private static final String CEILING = "CEILING";
    private static final String FLOOR = "FLOOR";
    private static final String ROUND = "ROUND";
    private static final String SET_PRECISION = "SET_PRECISION";
    private static final String ABSOLUTE = "ABSOLUTE";


    //common
    private static final String CONSTANT = "CONSTANT";
    private static final String GLOBAL_VARIABLE = "GLOBAL_VARIABLE";
    private static final String PROPERTIES = "PROPERTIES";
    private static final String COMPARE = "COMPARE";

    //conditional
    private static final String IF_ELSE = "IF_ELSE";

    //string
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

    //type_conversion
    private static final String TO_STRING = "TO_STRING";
    private static final String STRING_TO_NUMBER = "STRING_TO_NUMBER";
    private static final String STRING_TO_BOOLEAN = "STRING_TO_BOOLEAN";

    //boolean
    private static final String NOT = "NOT";
    private static final String AND = "AND";
    private static final String OR = "OR";

    //private static int currentLevel = 0;
    private static ArrayList<InPutNode> inPutNodes;
    private static ArrayList<OutPutNode> outPutNodes;
    private static InPutNode currentInNode;
    private static ArrayList<OperatorNode> operatorNodes;

    public static void main(String[] args){
        ReadXMLFile inputXML = new ReadXMLFile("/home/danushka/workspace/SampleFlowRegistry/NewConfig12.datamapper");
        WriteXMLFile outputXML = new WriteXMLFile("/home/danushka/Downloads/output.xml");
        Element rootElemenet = outputXML.getDocument().createElement("xsl:stylesheet");
        rootElemenet.setAttribute("xmlns:xsl","http://www.w3.org/1999/XSL/Transform");
        outputXML.getDocument().appendChild(rootElemenet);
        rootElemenet.setAttribute("version","1.0");
        Element templateElement = outputXML.getDocument().createElement("xsl:template");
        templateElement.setAttribute("match","/");
        rootElemenet.appendChild(templateElement);
        operatorNodes = createOperatorNodes(inputXML);
        createInputNode(inputXML);
        createOutputNode(inputXML,outputXML,rootElemenet,templateElement);
        for (OutPutNode outPutNode:outPutNodes) {
            traverseOutPutNode(outPutNode,outputXML,rootElemenet,templateElement);
        }
        outputXML.saveFile();
    }

    public static void createOutputNode(ReadXMLFile inputxmlFile, WriteXMLFile outputXMLFile,Element rootElement, Element templateElement){
        outPutNodes = new ArrayList<>();
        Node outputNode = inputxmlFile.getDocument().getElementsByTagName(OUTPUT).item(0);
        for(OperatorNode operatorNode:operatorNodes){
            if(operatorNode.getProperty(OPERATOR_TYPE).equals(GLOBAL_VARIABLE)){
                Element v = outputXMLFile.getDocument().createElement("xsl:param");
                v.setAttribute("name","operators."+Integer.toString(operatorNodes.indexOf(operatorNode)));
                v.setAttribute("global_name",operatorNode.getProperty("name"));
                String defaultValue = operatorNode.getProperty("defaultValue");
                v.setAttribute("select","'"+defaultValue+"'");
                templateElement.appendChild(v);
            }
        }
        for(int i=0;i<outputNode.getChildNodes().getLength();i++){
            if(outputNode.getChildNodes().item(i).getNodeName().equals(TREE_NODE)){
                outPutNodes.add(new OutPutNode(outputNode.getChildNodes().item(i),null,""));
            }
        }
    }

    public static void createInputNode(ReadXMLFile inputxmlFile){
        inPutNodes = new ArrayList<>();
        Node inputNode = inputxmlFile.getDocument().getElementsByTagName(INPUT).item(0);
        for(int i=0;i<inputNode.getChildNodes().getLength();i++){
            if(inputNode.getChildNodes().item(i).getNodeName().equals(TREE_NODE)){
                inPutNodes.add(new InPutNode(inputNode.getChildNodes().item(i),null,""));
            }
        }
    }

    public static ArrayList<OperatorNode> createOperatorNodes(ReadXMLFile inputxmlFile){
        ArrayList<OperatorNode> operatorNodes = new ArrayList<>();
        NodeList operators = inputxmlFile.getDocument().getElementsByTagName("operators");
        for(int i=0;i<operators.getLength();i++){
            operatorNodes.add(new OperatorNode(operators.item(i)));
        }
        return operatorNodes;
    }

    public static OperatorNode getIfElseOperator(String operatorNodePath){
        if (isOperator(operatorNodePath)){
            OperatorNode currentOperatorNode = getOperatorNode(operatorNodePath);
            for(String path:currentOperatorNode.getLeftContainer().getInNodes()){
                if(getIfElseOperator(path)!=null){
                    return getIfElseOperator(path);
                }
            }
            if(currentOperatorNode.getProperty(OPERATOR_TYPE).equals(IF_ELSE)){
                return currentOperatorNode;
            }
        }
        return null;
    }

    public static OperatorNode getIfElsePrevOperator(String operatorNodePath){
        if (isOperator(operatorNodePath)){
            OperatorNode currentOperatorNode = getOperatorNode(operatorNodePath);
            for(String path:currentOperatorNode.getLeftContainer().getInNodes()){
                if(getIfElseOperator(path)!=null){
                    return currentOperatorNode;
                }
            }
            return currentOperatorNode;
        }
        return null;
    }

    public static void handleIfElseOperator(OutPutNode node,OperatorNode operatorNode, WriteXMLFile outputXMLFile, Element parentElement){
        Element chooseElement = outputXMLFile.getDocument().createElement("xsl:choose");
        parentElement.appendChild(chooseElement);
        Element whenElement = outputXMLFile.getDocument().createElement("xsl:when");
        whenElement.setAttribute("test",getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)));
        Element whenValueElement = outputXMLFile.getDocument().createElement("xsl:value-of");
        whenValueElement.setAttribute("select",getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1)));
        whenElement.appendChild(whenValueElement);
        chooseElement.appendChild(whenElement);
        Element otherWise = outputXMLFile.getDocument().createElement("xsl:otherwise");
        Element chooseValueElement = outputXMLFile.getDocument().createElement("xsl:value-of");
        chooseValueElement.setAttribute("select",getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(2)));
        otherWise.appendChild(chooseValueElement);
        chooseElement.appendChild(otherWise);
    }

    public static String getOperatorValueIfElse(){

        return "";
    }

    public static void traverseOutPutNode(OutPutNode node,WriteXMLFile outputXMLFile, Element rootElement, Element parentElement){
        //System.out.println(currentNode.getAttributes().getNamedItem("schemaDataType").getNodeValue());
        if(node.getProperties().get(TYPE).equals(STRING_TYPE) || node.getProperties().get(TYPE).equals(NUMBER_TYPE) || node.getProperties().get(TYPE).equals(BOOLEAN_TYPE)){
            if(node.getInNode().getOutNodes().size()>0){
                Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                parentElement.appendChild(currentElement);
                if(node.getInNode().getOutNodes().size()>0){
                    OperatorNode ifElseOperatorNode = getIfElseOperator(node.getInNode().getOutNodes().get(0));
                    if(ifElseOperatorNode==null){
                        Element valueOfElement = outputXMLFile.getDocument().createElement("xsl:value-of");
                        currentElement.appendChild(valueOfElement);
                        valueOfElement.setAttribute("select",getValueFromMapping(node.getInNode().getOutNodes().get(0)));
                    }else{
                        handleIfElseOperator(node,ifElseOperatorNode,outputXMLFile,currentElement);
                    }
                }
            }
        }else if(node.getProperties().get(TYPE).equals(ARRAY_TYPE)){
            InPutNode previousCurrentNode = currentInNode;
            InPutNode inNode = traverseArray(node,operatorNodes);
            if(inNode!=null){
                String arrayPath = getArrayPath(inNode);
                if(!arrayPath.equals("")) {
                    Element forEachElement = outputXMLFile.getDocument().createElement("xsl:for-each");
                    forEachElement.setAttribute("select", arrayPath);
                    parentElement.appendChild(forEachElement);
                    Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                    forEachElement.appendChild(currentElement);
                    if (node.getProperty(ITEMS_TYPE).equals(OBJECT_TYPE)) {
                        for (OutPutNode childNode : node.getChildNodes()) {
                            traverseOutPutNode(childNode, outputXMLFile, rootElement, currentElement);
                        }
                    } else if (node.getInNode().getOutNodes().size() > 0) {
                        Element valueOfElement = outputXMLFile.getDocument().createElement("xsl:value-of");
                        currentElement.appendChild(valueOfElement);
                        valueOfElement.setAttribute("select", getValueFromMapping(node.getInNode().getOutNodes().get(0)));
                    }
                }
            }else if(node.getInNode().getOutNodes().size()>0){
                Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                parentElement.appendChild(currentElement);
                Element valueOfElement = outputXMLFile.getDocument().createElement("xsl:value-of");
                currentElement.appendChild(valueOfElement);
                valueOfElement.setAttribute("select",getValueFromMapping(node.getInNode().getOutNodes().get(0)));
            }
            currentInNode = previousCurrentNode;
        }else{
            Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
            parentElement.appendChild(currentElement);
            for(OutPutNode childNode:node.getChildNodes()){
                traverseOutPutNode(childNode,outputXMLFile,rootElement,currentElement);
            }
        }
    }

    public static String getArrayPath(InPutNode node){
        if(currentInNode==null){
            InPutNode tempNode = node;
            while (tempNode.getParentNode()!=null){
                if(tempNode.getParentNode().getProperty(TYPE).equals(ARRAY_TYPE)){
                    node = tempNode.getParentNode();
                }
                tempNode = tempNode.getParentNode();
            }
            currentInNode = node;
            return node.getxPath();
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
        /*
        String[] cPathArray = currentPath.split("/");
        String[] xPathArray = node.getxPath().split("/");
        String precessorPath = "";
        String path = "";
        int cnt = 0;
        boolean matching= true;
        while (matching && (cPathArray.length>cnt && xPathArray.length>cnt)){
            if(xPathArray[cnt].equals(cPathArray[cnt])){
                cnt++;
            }else{
                matching=false;
            }
        }
        int back = xPathArray.length-1;
        gen.InPutNode tempNode = node;
        while (back>cnt && tempNode.getParentNode()!=null){
            if(tempNode.getParentNode().getProperty(type).equals(arrayType)){
                node = tempNode.getParentNode();
            }
            tempNode = tempNode.getParentNode();
            back--;
        }
        xPathArray = node.getxPath().split("/");
        cnt = 0;
        matching= true;
        while (matching && (cPathArray.length>cnt && xPathArray.length>cnt)){
            if(xPathArray[cnt].equals(cPathArray[cnt])){
                precessorPath+=cPathArray[cnt]+"/";
                xPathArray = removeElementAt(xPathArray,cnt);
                cPathArray = removeElementAt(cPathArray,cnt);
            }else{
                matching=false;
            }
        }
        for (String s:cPathArray) {
            if(!s.equals("")){
                path="../"+path;
            }
        }
        for (String s:xPathArray) {
            if(path.length()==0 || path.charAt(path.length()-1)=='/'){
                path = path+s;
            }else {
                path = path + "/" +s;
            }
        }
        if(!path.equals("")){
            currentPath = precessorPath+path;
        }
        return path;
        */
    }

    public static String getValueFromMapping(String inNode){
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
                        index = index+nextDigit;
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
                        return "concat("+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+","+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+")";
                    }
                    break;
                case LOWERCASE:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "lower-case(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)) + ")";
                    }
                    break;
                case UPPERCASE:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "upper-case(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)) + ")";
                    }
                    break;
                case STRING_LENGTH:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "string-length(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)) + ")";
                    }
                    break;
                case SPLIT:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "tokenize(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)) + ",'" +operatorNode.getProperty("delimiter")+"')["+Integer.toString(Integer.parseInt(inNode.substring(inNode.lastIndexOf("@rightConnectors.")+17,inNode.lastIndexOf("@rightConnectors.")+18))+1)+"]";
                    }
                    break;
                case TO_STRING:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "string(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)) + ")";
                    }
                    break;
                case STRING_TO_NUMBER:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0) {
                        return "number(" + getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0)) + ")";
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
                            value = " ( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(i))+" ) and ( "+value+" )";
                        }
                        return value;
                    }
                    break;
                case OR:
                    if(operatorNode.getLeftContainer().getInNodes().size()>1) {
                        String value = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0));
                        for(int i=1;i<operatorNode.getLeftContainer().getInNodes().size();i++){
                            value = getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(i))+" or "+value;
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
                        return "substring( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" , "+startIndex+" , "+startIndex+" + "+endIndex+" )";
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
                        return "replace( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" , "+target+" , "+replaceString+" )";
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
                        return "matches( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" , "+pattern+" )";
                    }
                    break;
                case TRIM:
                    if(operatorNode.getLeftContainer().getInNodes().size()>0){
                        return "replace(replace("+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+",'\\s+$',''),'^\\s+','')";
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
                            return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" = "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        }
                    }else {
                        switch (comparisonOperator){
                            case "!=":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" != "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            case "!==":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" != "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            case "===":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" = "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            case ">":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" > "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            case ">=":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" >= "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            case "<":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" < "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                            case "<=":
                                return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" <= "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1));
                        }
                    }
                    break;
                case ADD:
                    if(operatorNode.getLeftContainer().getInNodes().size()==2){
                        return "( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" + "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+" )";
                    }
                    break;
                case SUBTRACT:
                    if(operatorNode.getLeftContainer().getInNodes().size()==2){
                        return "( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" - "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+" )";
                    }
                    break;
                case DIVIDE:
                    if(operatorNode.getLeftContainer().getInNodes().size()==2){
                        return "( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" div "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+" )";
                    }
                    break;
                case MULTIPLY:
                    if(operatorNode.getLeftContainer().getInNodes().size()==2){
                        return "( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" * "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(1))+" )";
                    }
                    break;
                case CEILING:
                    if(operatorNode.getLeftContainer().getInNodes().size()==1){
                        return "ceiling( "+getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" )";
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
                            return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" * math:power( number('10'), "+noOfDecimals+" )";
                        }
                    }else if(operatorNode.getLeftContainer().getInNodes().size()==1){
                        return getValueFromMapping(operatorNode.getLeftContainer().getInNodes().get(0))+" * math:power( number('10'), number('"+noOfDecimals+"') )";
                    }
                    break;
            }
        }else{
            InPutNode inPutNode = inPutNodes.get(Integer.parseInt(inNode.substring(19,20)));
            String inNodeString = StringUtils.substring(inNode,20);
            InPutNode currentNode = inPutNode;
            String path="";
            boolean parentFound = false;
            while (inNodeString.contains("/@node")){
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
                    return currentNode.getxPath()+" eq 'true' ";
                }else if(currentNode.getProperty(TYPE).equals(NUMBER_TYPE)){
                    return "number( "+currentNode.getxPath()+" )";
                }
                return currentNode.getxPath();
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
                String[] cPathParts = currentInNode.getxPath().split("/");
                String[] xPathParts = currentNode.getxPath().split("/");
                boolean matching = true;
                if(!cPathParts[0].equals(xPathParts[0])){
                    matching = false;
                    path = inPutNode.getName();
                }else{
                    cPathParts = removeElementAt(cPathParts,0);
                    xPathParts = removeElementAt(xPathParts,0);
                }
                while ((matching && cPathParts.length>0) && (xPathParts.length>0 && xPathParts[0].equals(cPathParts[0]))){
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
            /*String xPath = "";
            String[] cPathParts = getCurrentPath().split("/");
            boolean matching = true;
            if(!cPathParts[0].equals(inPutNode.getName())){
                matching = false;
                xPath = inPutNode.getName();
            }else{
                cPathParts = removeElementAt(cPathParts,0);
            }
            gen.InPutNode currentNode = inPutNode;
            String inNodeString = StringUtils.substring(inNode,20);
            while (inNodeString.contains("/@node")){
                currentNode = currentNode.getChildNodes().get(Integer.parseInt(inNodeString.substring(7,8)));
                inNodeString = StringUtils.substring(inNodeString,8);
                if((matching && cPathParts.length>0) && cPathParts[0].equals(currentNode.getName())){
                    cPathParts = removeElementAt(cPathParts,0);
                }else{
                    matching = false;
                    if(xPath.equals("")){
                        xPath=xPath+currentNode.getName();
                    }else {
                        xPath=xPath+"/"+currentNode.getName();
                    }
                }
            }
            for (String s:cPathParts) {
                if(!s.equals("")){
                    xPath="../"+xPath;
                }
            }
            return xPath;*/
        }

        return "";
    }

    public static boolean isOperator(String inNode){
        if(inNode.startsWith("//@operators")){
            return true;
        }
        return false;
    }

    public static OperatorNode getOperatorNode(String operatorNode){
        String index = operatorNode.substring(13,14);
        int currentIndex = 14;
        boolean moreDigits = true;
        while (moreDigits){
            try{
                String nextDigit = operatorNode.substring(currentIndex,currentIndex+1);
                if(nextDigit.equals("/")){
                    moreDigits = false;
                }else {
                    index = index+nextDigit;
                    currentIndex++;
                }
            }catch (Exception e){
                moreDigits = false;
            }
        }
        return operatorNodes.get(Integer.parseInt(index));
    }

    /*public static gen.InPutNode getInNode(gen.InPutNode inPutNode, ArrayList<gen.OperatorNode> operatorNodes, String inNode){
        if(isOperator(inNode)){
            gen.OperatorNode operatorNode = operatorNodes.get(Integer.parseInt(inNode.substring(13,14)));
            String inNodeString = StringUtils.substring(inNode,20);
            while (inNodeString.contains("/@node")){
                currentNode = currentNode.getChildNodes().get(Integer.parseInt(inNodeString.substring(7,8)));
                inNodeString = StringUtils.substring(inNodeString,8);
            }
            return currentNode;
        }else{
            gen.InPutNode currentNode = inPutNode.getChildNodes().get(Integer.parseInt(inNode.substring(19,20)));
            String inNodeString = StringUtils.substring(inNode,20);
            while (inNodeString.contains("/@node")){
                currentNode = currentNode.getChildNodes().get(Integer.parseInt(inNodeString.substring(7,8)));
                inNodeString = StringUtils.substring(inNodeString,8);
            }
            return currentNode;
        }
    }*/

    public static InPutNode traverseArray(OutPutNode node,ArrayList<OperatorNode> operatorNodes){
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

    public static InPutNode getArrayElement(String inNode, ArrayList<OperatorNode> operatorNodes){
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
            while (inNodeString.contains("/@node")) {
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

    public static InPutNode getHighestLevelNode(ArrayList<InPutNode> arrayNodes){
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

    public static String[] removeElementAt(String[] array, int removedIdx){
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