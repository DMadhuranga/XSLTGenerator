/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package gen;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;

import static gen.XSLTGeneratorConstants.*;

/**
 * This class handles the generation of XSLT stylesheet.
 */
class XSLTCreator {

    private static ArrayList<InPutNode> inPutNodes;
    private static ArrayList<OutPutNode> outPutNodes;
    private static InPutNode currentInNode;
    private static ArrayList<OperatorNode> operatorNodes;

    public static void main(String[] args) throws SAXException, IOException,
            ParserConfigurationException,
            TransformerException {
        generateStyleSheet("/home/danushka/Downloads/output.xml",
                "/home/danushka/workspace/SampleFlowRegistry/NewConfig.datamapper");
    }

    /**
     * This method will build the data trees and generate the XSLTStyleSheet
     *
     * @param styleSheetFilePath path of the file that needed to save the generated stylesheet
     * @param schemaFilePath     path of the datamapper schema file
     */
    private static void generateStyleSheet(String styleSheetFilePath, String schemaFilePath)
            throws SAXException, IOException, ParserConfigurationException, TransformerException {
        DataMapperSchemaProcessor inputXML = new DataMapperSchemaProcessor(schemaFilePath);
        XSLTStyleSheetWriter outputXML = new XSLTStyleSheetWriter(styleSheetFilePath);
        Element rootElement = outputXML.getDocument().createElement(XSL_STYLESHEET);
        rootElement.setAttribute(XMLNS_XSL, "http://www.w3.org/1999/XSL/Transform");
        rootElement.setAttribute(XMLNS_XS, "http://www.w3.org/2001/XMLSchema");
        rootElement.setAttribute(VERSION, "2.0");
        rootElement.setAttribute(XMLNS_OWN, "http://whatever");
        outputXML.getDocument().appendChild(rootElement);
        setPrecisionFunction(rootElement, outputXML);
        Element templateElement = outputXML.getDocument().createElement(XSL_TEMPLATE);
        templateElement.setAttribute(MATCH_LOWER_CASE, SLASH);
        rootElement.appendChild(templateElement);
        createOperatorNodes(inputXML);
        createInputNode(inputXML);
        createOutputNode(inputXML, outputXML, templateElement);
        for (OutPutNode outPutNode : outPutNodes) {
            traverseOutPutNode(outPutNode, outputXML, templateElement);
        }
        outputXML.saveFile();
    }

    /**
     * This method will set up the precision function in the output file
     *
     * @param rootElement first element of the output XML file
     * @param outputXML   writeXML file responsible for writing the output file
     */
    private static void setPrecisionFunction(Element rootElement, XSLTStyleSheetWriter outputXML) {
        Element setPrecisionFunction = outputXML.getDocument().createElement(XSL_FUNCTION);
        setPrecisionFunction.setAttribute(NAME, OWN_SET_PRECISION);
        Element resultString = outputXML.getDocument().createElement(XSL_PARAM);
        resultString.setAttribute(NAME, RESULT_STRING);
        setPrecisionFunction.appendChild(resultString);
        Element noOfDigits = outputXML.getDocument().createElement(XSL_PARAM);
        noOfDigits.setAttribute(NAME, "noOfDigits");
        setPrecisionFunction.appendChild(noOfDigits);
        Element firstIf = outputXML.getDocument().createElement(XSL_IF);
        firstIf.setAttribute(TEST, "$noOfDigits=0");
        setPrecisionFunction.appendChild(firstIf);
        Element firstValue = outputXML.getDocument().createElement(XSL_VALUE_OF);
        firstValue.setAttribute(SELECT, "$resultString");
        firstIf.appendChild(firstValue);
        Element secondIf = outputXML.getDocument().createElement(XSL_IF);
        secondIf.setAttribute(TEST, "$noOfDigits!=0");
        setPrecisionFunction.appendChild(secondIf);
        Element secondValue = outputXML.getDocument().createElement(XSL_VALUE_OF);
        secondValue.setAttribute(SELECT, "own:setPrecision(concat($resultString,'0'),$noOfDigits " +
                "- 1)");
        secondIf.appendChild(secondValue);
        rootElement.appendChild(setPrecisionFunction);
    }

    /**
     * This method will travers the data mapper schema and create the output tree
     *
     * @param inputXMLFile    data mapper file that need to be traversed
     * @param outputXMLFile   output XSLT stylesheet file
     * @param templateElement main template element of the output XSLT stylesheet
     */
    private static void createOutputNode(DataMapperSchemaProcessor inputXMLFile,
                                         XSLTStyleSheetWriter outputXMLFile, Element
                                                 templateElement) {
        outPutNodes = new ArrayList<>();
        Node outputNode = inputXMLFile.getDocument().getElementsByTagName(OUTPUT).item(0);
        for (OperatorNode operatorNode : operatorNodes) {
            if (operatorNode.getProperty(OPERATOR_TYPE).equals(GLOBAL_VARIABLE)) {
                Element v = outputXMLFile.getDocument().createElement(XSL_PARAM);
                v.setAttribute(NAME, "operators." + Integer.toString(operatorNodes.indexOf
                        (operatorNode)));
                v.setAttribute("global_name", operatorNode.getProperty(NAME));
                String defaultValue = operatorNode.getProperty("defaultValue");
                v.setAttribute(SELECT, "'" + defaultValue + "'");
                templateElement.appendChild(v);
            }
        }
        for (int i = 0; i < outputNode.getChildNodes().getLength(); i++) {
            if (outputNode.getChildNodes().item(i).getNodeName().equals(TREE_NODE)) {
                outPutNodes.add(new OutPutNode(outputNode.getChildNodes().item(i), EMPTY_STRING));
            }
        }
    }

    /**
     * This method will travers the data mapper schema and create the input tree
     *
     * @param inputXMLFile data mapper file that need to be traversed
     */
    private static void createInputNode(DataMapperSchemaProcessor inputXMLFile) {
        inPutNodes = new ArrayList<>();
        Node inputNode = inputXMLFile.getDocument().getElementsByTagName(INPUT).item(0);
        for (int i = 0; i < inputNode.getChildNodes().getLength(); i++) {
            if (inputNode.getChildNodes().item(i).getNodeName().equals(TREE_NODE)) {
                inPutNodes.add(new InPutNode(inputNode.getChildNodes().item(i), null,
                        EMPTY_STRING));
            }
        }
    }

    /**
     * This method will travers the data mapper schema and create the operators array
     *
     * @param inputXMLFile data mapper file that need to be traversed
     */
    private static void createOperatorNodes(DataMapperSchemaProcessor inputXMLFile) {
        operatorNodes = new ArrayList<>();
        NodeList operators = inputXMLFile.getDocument().getElementsByTagName("operators");
        for (int i = 0; i < operators.getLength(); i++) {
            operatorNodes.add(new OperatorNode(operators.item(i)));
        }
    }

    /**
     * This method will traverse through output nodes and build the stylesheet
     *
     * @param node          first element of the output XML file
     * @param outputXMLFile writeXML file responsible for writing the output file
     * @param parentElement writeXML file responsible for writing the output file
     */
    private static void traverseOutPutNode(OutPutNode node, XSLTStyleSheetWriter outputXMLFile,
                                           Element parentElement) {
        switch (node.getProperties().get(TYPE)) {
            case STRING_TYPE:
            case NUMBER_TYPE:
            case BOOLEAN_TYPE:
                if (!node.getInNode().getOutNodes().isEmpty()) {
                    Element currentElement = outputXMLFile.getDocument().createElement(node
                            .getName());
                    parentElement.appendChild(currentElement);
                    Element valueOfElement = outputXMLFile.getDocument().createElement
                            (XSL_VALUE_OF);
                    currentElement.appendChild(valueOfElement);
                    valueOfElement.setAttribute(SELECT, getValueFromMapping(node.getInNode()
                            .getOutNodes().get(0)));
                }
                break;
            case ARRAY_TYPE:
                InPutNode previousCurrentNode = currentInNode;
                InPutNode inNode = traverseArray(node, operatorNodes);
                if (inNode != null) {
                    String arrayPath = getArrayPath(inNode);
                    if (!arrayPath.equals(EMPTY_STRING)) {
                        Element forEachElement = outputXMLFile.getDocument().createElement
                                (XSL_FOR_EACH);
                        forEachElement.setAttribute(SELECT, arrayPath);
                        parentElement.appendChild(forEachElement);
                        Element currentElement = outputXMLFile.getDocument().createElement(node
                                .getName());
                        forEachElement.appendChild(currentElement);
                        if (node.getProperty(ITEMS_TYPE).equals(OBJECT_TYPE)) {
                            for (OutPutNode childNode : node.getChildNodes()) {
                                traverseOutPutNode(childNode, outputXMLFile, currentElement);
                            }
                        } else if (node.getInNode().getOutNodes().size() > 0) {
                            Element valueOfElement = outputXMLFile.getDocument().createElement
                                    (XSL_VALUE_OF);
                            currentElement.appendChild(valueOfElement);
                            valueOfElement.setAttribute(SELECT,
                                    getValueFromMapping(node.getInNode().getOutNodes().get(0)));
                        }
                    }
                } else if (!node.getInNode().getOutNodes().isEmpty()) {
                    Element currentElement = outputXMLFile.getDocument().createElement(node
                            .getName());
                    parentElement.appendChild(currentElement);
                    Element valueOfElement = outputXMLFile.getDocument().createElement
                            (XSL_VALUE_OF);
                    currentElement.appendChild(valueOfElement);
                    valueOfElement.setAttribute(SELECT,
                            getValueFromMapping(node.getInNode().getOutNodes().get(0)));
                }
                currentInNode = previousCurrentNode;
                break;
            default:
                Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                parentElement.appendChild(currentElement);
                for (OutPutNode childNode : node.getChildNodes()) {
                    traverseOutPutNode(childNode, outputXMLFile, currentElement);
                }
                break;
        }
    }

    /**
     * This method will traverse through the input tree and return the path of the relevant array
     *
     * @param node input node whose xPath is needed
     * @return path of the array element
     */
    private static String getArrayPath(InPutNode node) {
        if (currentInNode == null) {
            InPutNode tempNode = node;
            while (tempNode.getParentNode() != null) {
                if (tempNode.getParentNode().getProperty(TYPE).equals(ARRAY_TYPE)) {
                    node = tempNode.getParentNode();
                }
                tempNode = tempNode.getParentNode();
            }
            currentInNode = node;
            return node.getXPath();
        } else {
            if (node == currentInNode) {
                return EMPTY_STRING;
            }
            InPutNode tempNode = node;
            while (tempNode.getParentNode() != null && currentInNode != tempNode.getParentNode()) {
                if (tempNode.getParentNode().getProperty(TYPE).equals(ARRAY_TYPE)) {
                    node = tempNode.getParentNode();
                }
                tempNode = tempNode.getParentNode();
            }
            StringBuilder path = new StringBuilder();
            tempNode = node;
            while (tempNode.getParentNode() != null && tempNode != currentInNode) {
                if (path.toString().equals(EMPTY_STRING)) {
                    path = new StringBuilder(tempNode.getName());
                } else {
                    path.insert(0, tempNode.getName() + SLASH);
                }
                tempNode = tempNode.getParentNode();
            }
            if (!path.toString().equals(EMPTY_STRING)) {
                currentInNode = node;
            }
            return path.toString();
        }
    }

    /**
     * This method will return the select value according to the given inNode
     *
     * @param inNode first element of the output XML file
     * @return the name of the previous element if the element was an array element, null otherwise
     */
    private static String getValueFromMapping(String inNode) throws IndexOutOfBoundsException {
        if (isOperator(inNode)) {
            String index = inNode.split(DOT_SYMBOL)[1].split(SLASH)[0];
            OperatorNode operatorNode = operatorNodes.get(Integer.parseInt(index));
            switch (operatorNode.getProperty(OPERATOR_TYPE)) {
                case CONSTANT:
                    String type = operatorNode.getProperty(TYPE);
                    if (type != null) {
                        if (type.equals("BOOLEAN")) {
                            return "('" + operatorNode.getProperty("constantValue") + "' eq " +
                                    "'true')";
                        } else if (type.equals("NUMBER")) {
                            return "number( '" + operatorNode.getProperty("constantValue") + "' )";
                        }
                    }
                    return "'" + operatorNode.getProperty("constantValue") + "'";
                case CONCAT:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 2) {
                        return "concat(" + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + "," + getValueFromMapping(operatorNode
                                .getLeftContainer().getInNodes().get(1)) + ")";
                    }
                    break;
                case LOWERCASE:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        return "lower-case(" + getValueFromMapping(operatorNode.getLeftContainer
                                ().getInNodes().get(0)) + ")";
                    }
                    break;
                case UPPERCASE:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        return "upper-case(" + getValueFromMapping(operatorNode.getLeftContainer
                                ().getInNodes().get(0)) + ")";
                    }
                    break;
                case STRING_LENGTH:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        return "string-length(" + getValueFromMapping(operatorNode
                                .getLeftContainer().getInNodes().get(0)) + ")";
                    }
                    break;
                case SPLIT:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        return "tokenize(" + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + ",'" + operatorNode.getProperty
                                ("delimiter") + "')[" + Integer.toString(Integer.parseInt(inNode
                                .substring(inNode.lastIndexOf("@rightConnectors.") + 17, inNode
                                        .lastIndexOf("@rightConnectors.") + 18)) + 1) + "]";
                    }
                    break;
                case TO_STRING:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        return "string(" + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + ")";
                    }
                    break;
                case STRING_TO_NUMBER:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        return "number(" + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + ")";
                    }
                    break;
                case STRING_TO_BOOLEAN:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        return getValueFromMapping(operatorNode.getLeftContainer().getInNodes()
                                .get(0)) + " eq 'true'";
                    }
                    break;
                case NOT:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        return "not(" + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + ")";
                    }
                    break;
                case AND:
                    if (operatorNode.getLeftContainer().getInNodes().size() > 1) {
                        StringBuilder value = new StringBuilder(getValueFromMapping(operatorNode
                                .getLeftContainer().getInNodes().get(0)));
                        for (int i = 1; i < operatorNode.getLeftContainer().getInNodes().size();
                             i++) {
                            value = new StringBuilder(" ( " + getValueFromMapping(operatorNode
                                    .getLeftContainer().getInNodes().get(i)) + " ) and ( " +
                                    value + " )");
                        }
                        return value.toString();
                    }
                    break;
                case OR:
                    if (operatorNode.getLeftContainer().getInNodes().size() > 1) {
                        StringBuilder value = new StringBuilder(getValueFromMapping(operatorNode
                                .getLeftContainer().getInNodes().get(0)));
                        for (int i = 1; i < operatorNode.getLeftContainer().getInNodes().size();
                             i++) {
                            value.insert(0, getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(i)) + " or ");
                        }
                        return value.toString();
                    }
                    break;
                case STARTS_WITH:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 1) {
                        String value = getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0));
                        return "starts-with(" + value + " , '" + operatorNode.getAttributes().get
                                ("pattern") + "')";
                    } else if (operatorNode.getLeftContainer().getInNodes().size() == 2) {
                        String string = getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0));
                        String exp = getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(1));
                        return "starts-with(" + string + " , " + exp + ")";
                    }
                    break;
                case ENDS_WITH:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 1) {
                        String value = getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0));
                        return "ends-with(" + value + " , '" + operatorNode.getAttributes().get
                                ("pattern") + "')";
                    } else if (operatorNode.getLeftContainer().getInNodes().size() == 2) {
                        String string = getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0));
                        String exp = getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(1));
                        return "ends-with(" + string + " , " + exp + ")";
                    }
                    break;
                case SUBSTRING:
                    String startIndex = operatorNode.getAttributes().get("startIndex");
                    String endIndex = operatorNode.getAttributes().get("endIndex");
                    if (startIndex != null) {
                        startIndex = "number('" + startIndex + "')";
                    }
                    if (endIndex != null) {
                        endIndex = "number('" + endIndex + "')";
                    }
                    if (startIndex == null && endIndex == null) {
                        if (operatorNode.getLeftContainer().getInNodes().size() == 3) {
                            startIndex = getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(1));
                            endIndex = getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(2));
                        }
                    } else if (startIndex == null) {
                        if (operatorNode.getLeftContainer().getInNodes().size() > 1) {
                            startIndex = getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(1));
                        }
                    } else if (endIndex == null) {
                        if (operatorNode.getLeftContainer().getInNodes().size() > 1) {
                            endIndex = getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(1));
                        }
                    }
                    if (startIndex != null && endIndex != null) {
                        return "substring( " + getValueFromMapping(operatorNode.getLeftContainer
                                ().getInNodes().get(0)) + " , " + startIndex + " , " + startIndex
                                + " + " + endIndex + " )";
                    }
                    break;
                case REPLACE:
                    String target = operatorNode.getAttributes().get("target");
                    String replaceString = operatorNode.getAttributes().get("replaceString");
                    if (target != null) {
                        target = "'" + target + "'";
                    }
                    if (replaceString != null) {
                        replaceString = "'" + replaceString + "'";
                    }
                    if (target == null && replaceString == null) {
                        if (operatorNode.getLeftContainer().getInNodes().size() == 3) {
                            target = getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(1));
                            replaceString = getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(2));
                        }
                    } else if (target == null) {
                        if (operatorNode.getLeftContainer().getInNodes().size() > 1) {
                            target = getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(1));
                        }
                    } else if (replaceString == null) {
                        if (operatorNode.getLeftContainer().getInNodes().size() > 1) {
                            replaceString = getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(1));
                        }
                    }
                    if (target != null && replaceString != null) {
                        return "replace( " + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + " , " + target + " , " + replaceString +
                                " )";
                    }
                    break;
                case MATCH:
                    String pattern = operatorNode.getAttributes().get("pattern");
                    if (pattern == null) {
                        if (operatorNode.getLeftContainer().getInNodes().size() == 2) {
                            pattern = getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(1));
                        }
                    } else {
                        pattern = "'" + pattern.substring(1, pattern.length() - 1) + "'";
                    }
                    if (pattern != null) {
                        return "matches( " + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + " , " + pattern + " )";
                    }
                    break;
                case TRIM:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        return "replace(replace(" + getValueFromMapping(operatorNode
                                .getLeftContainer().getInNodes().get(0)) + ",'\\s+$',''),'^\\s+'," +
                                "'')";
                    }
                    break;
                case GLOBAL_VARIABLE:
                    return "$operators." + Integer.toString(operatorNodes.indexOf(operatorNode))
                            + EMPTY_STRING;
                case PROPERTIES_UPPER_CASE:
                    return EMPTY_STRING;
                case COMPARE:
                    String comparisonOperator = operatorNode.getAttributes().get
                            ("comparisonOperator");
                    if (comparisonOperator == null) {
                        if (operatorNode.getLeftContainer().getInNodes().size() == 2) {
                            return getValueFromMapping(operatorNode.getLeftContainer().getInNodes
                                    ().get(0)) + " = " + getValueFromMapping(operatorNode
                                    .getLeftContainer().getInNodes().get(1));
                        }
                    } else {
                        switch (comparisonOperator) {
                            case "!=":
                                return getValueFromMapping(operatorNode.getLeftContainer()
                                        .getInNodes().get(0)) + " != " + getValueFromMapping
                                        (operatorNode.getLeftContainer().getInNodes().get(1));
                            case "!==":
                                return "not( if((string(" + getValueFromMapping(operatorNode
                                        .getLeftContainer().getInNodes().get(0)) + ") castable as" +
                                        " xs:double) or (string(" + getValueFromMapping
                                        (operatorNode.getLeftContainer().getInNodes().get(1)) +
                                        ") castable as xs:double)) then( if((string(" +
                                        getValueFromMapping(operatorNode.getLeftContainer()
                                                .getInNodes().get(0)) + ") castable as xs:double)" +
                                        " and (string(" + getValueFromMapping(operatorNode
                                        .getLeftContainer().getInNodes().get(1)) + ") castable as" +
                                        " xs:double)) then(true()) else false() ) else if((string" +
                                        "(" + getValueFromMapping(operatorNode.getLeftContainer()
                                        .getInNodes().get(0)) + ") castable as xs:boolean) or " +
                                        "(string(" + getValueFromMapping(operatorNode
                                        .getLeftContainer().getInNodes().get(1)) + ") castable as" +
                                        " xs:boolean)) then( if((string(" + getValueFromMapping
                                        (operatorNode.getLeftContainer().getInNodes().get(0)) +
                                        ") castable as xs:boolean) and (string(" +
                                        getValueFromMapping(operatorNode.getLeftContainer()
                                                .getInNodes().get(1)) + ") castable as " +
                                        "xs:boolean)) then(true()) else false() ) else true() )";
                            case "===":
                                return "( if((string(" + getValueFromMapping(operatorNode
                                        .getLeftContainer().getInNodes().get(0)) + ") castable as" +
                                        " xs:double) or (string(" + getValueFromMapping
                                        (operatorNode.getLeftContainer().getInNodes().get(1)) +
                                        ") castable as xs:double)) then( if((string(" +
                                        getValueFromMapping(operatorNode.getLeftContainer()
                                                .getInNodes().get(0)) + ") castable as xs:double)" +
                                        " and (string(" + getValueFromMapping(operatorNode
                                        .getLeftContainer().getInNodes().get(1)) + ") castable as" +
                                        " xs:double)) then(true()) else false() ) else if((string" +
                                        "(" + getValueFromMapping(operatorNode.getLeftContainer()
                                        .getInNodes().get(0)) + ") castable as xs:boolean) or " +
                                        "(string(" + getValueFromMapping(operatorNode
                                        .getLeftContainer().getInNodes().get(1)) + ") castable as" +
                                        " xs:boolean)) then( if((string(" + getValueFromMapping
                                        (operatorNode.getLeftContainer().getInNodes().get(0)) +
                                        ") castable as xs:boolean) and (string(" +
                                        getValueFromMapping(operatorNode.getLeftContainer()
                                                .getInNodes().get(1)) + ") castable as " +
                                        "xs:boolean)) then(true()) else false() ) else true() )";
                            case ">":
                                return getValueFromMapping(operatorNode.getLeftContainer()
                                        .getInNodes().get(0)) + " > " + getValueFromMapping
                                        (operatorNode.getLeftContainer().getInNodes().get(1));
                            case ">=":
                                return getValueFromMapping(operatorNode.getLeftContainer()
                                        .getInNodes().get(0)) + " >= " + getValueFromMapping
                                        (operatorNode.getLeftContainer().getInNodes().get(1));
                            case "<":
                                return getValueFromMapping(operatorNode.getLeftContainer()
                                        .getInNodes().get(0)) + " < " + getValueFromMapping
                                        (operatorNode.getLeftContainer().getInNodes().get(1));
                            case "<=":
                                return getValueFromMapping(operatorNode.getLeftContainer()
                                        .getInNodes().get(0)) + " <= " + getValueFromMapping
                                        (operatorNode.getLeftContainer().getInNodes().get(1));
                        }
                    }
                    break;
                case ADD:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 2) {
                        return "( " + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + " + " + getValueFromMapping(operatorNode
                                .getLeftContainer().getInNodes().get(1)) + " )";
                    }
                    break;
                case SUBTRACT:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 2) {
                        return "( " + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + " - " + getValueFromMapping(operatorNode
                                .getLeftContainer().getInNodes().get(1)) + " )";
                    }
                    break;
                case DIVIDE:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 2) {
                        return "( " + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + " div " + getValueFromMapping
                                (operatorNode.getLeftContainer().getInNodes().get(1)) + " )";
                    }
                    break;
                case MULTIPLY:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 2) {
                        return "( " + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + " * " + getValueFromMapping(operatorNode
                                .getLeftContainer().getInNodes().get(1)) + " )";
                    }
                    break;
                case CEILING:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 1) {
                        return "ceiling( " + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + " )";
                    }
                    break;
                case FLOOR:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 1) {
                        return "floor( " + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + " )";
                    }
                    break;
                case ROUND:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 1) {
                        return "round( " + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + " )";
                    }
                    break;
                case ABSOLUTE:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 1) {
                        return "abs( " + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + " )";
                    }
                    break;
                case SET_PRECISION:
                    String noOfDecimals = operatorNode.getAttributes().get("numberOfDigits");
                    if (noOfDecimals == null) {
                        if (operatorNode.getLeftContainer().getInNodes().size() == 2) {
                            noOfDecimals = getValueFromMapping(operatorNode.getLeftContainer()
                                    .getInNodes().get(1));
                            return "format-number( " + getValueFromMapping(operatorNode
                                    .getLeftContainer().getInNodes().get(0)) + " ," +
                                    "own:setPrecision('#.',number(" + noOfDecimals + ")))";
                        }
                    } else if (operatorNode.getLeftContainer().getInNodes().size() == 1) {
                        return "format-number( " + getValueFromMapping(operatorNode
                                .getLeftContainer().getInNodes().get(0)) + " ,own:setPrecision('#" +
                                ".',number('" + noOfDecimals + "')))";
                    }
                    break;
                case MIN:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        StringBuilder parameters = new StringBuilder();
                        for (int i = 0; i < operatorNode.getLeftContainer().getInNodes().size();
                             i++) {
                            if (i == 0) {
                                parameters.append(getValueFromMapping(operatorNode
                                        .getLeftContainer().getInNodes().get(0)));
                            } else {
                                parameters.append(" , ").append(getValueFromMapping(operatorNode
                                        .getLeftContainer().getInNodes().get(i)));
                            }
                        }
                        return "min( (" + parameters + ") )";
                    }
                case MAX:
                    if (!operatorNode.getLeftContainer().getInNodes().isEmpty()) {
                        StringBuilder parameters = new StringBuilder();
                        for (int i = 0; i < operatorNode.getLeftContainer().getInNodes().size();
                             i++) {
                            if (i == 0) {
                                parameters.append(getValueFromMapping(operatorNode
                                        .getLeftContainer().getInNodes().get(0)));
                            } else {
                                parameters.append(" , ").append(getValueFromMapping(operatorNode
                                        .getLeftContainer().getInNodes().get(i)));
                            }
                        }
                        return "max( (" + parameters + ") )";
                    }
                case IF_ELSE:
                    if (operatorNode.getLeftContainer().getInNodes().size() == 3) {
                        return "(if(" + getValueFromMapping(operatorNode.getLeftContainer()
                                .getInNodes().get(0)) + ")then(" + getValueFromMapping
                                (operatorNode.getLeftContainer().getInNodes().get(1)) + ")else "
                                + getValueFromMapping(operatorNode.getLeftContainer().getInNodes
                                ().get(2)) + ")";
                    }
                    break;

            }
        } else {
            int currentIndex = 1;
            String[] atNodes = inNode.split(DOT_SYMBOL);
            InPutNode inPutNode = inPutNodes.get(Integer.parseInt(atNodes[currentIndex].split
                    (SLASH)[0]));
            String inNodeString = inNode.substring(inNode.indexOf(AT_NODE));
            InPutNode currentNode = inPutNode;
            StringBuilder path = new StringBuilder();
            boolean parentFound = false;
            while (inNodeString.contains(AT_NODE)) {
                currentIndex++;
                if (parentFound) {
                    if (path.toString().equals(EMPTY_STRING)) {
                        path = new StringBuilder(currentNode.getName());
                    } else {
                        path.append(SLASH).append(currentNode.getName());
                    }
                }
                if (currentInNode == currentNode) {
                    parentFound = true;
                }
                String nodeIndex = atNodes[currentIndex].split(SLASH)[0];
                currentNode = currentNode.getChildNodes().get(Integer.parseInt(nodeIndex));
                inNodeString = inNodeString.substring(AT_NODE.length() + nodeIndex.length() + 1);
            }
            if (currentInNode == null) {
                if (currentNode.getProperty(TYPE).equals(BOOLEAN_TYPE)) {
                    return currentNode.getXPath() + " eq 'true' ";
                } else if (currentNode.getProperty(TYPE).equals(NUMBER_TYPE)) {
                    return "number( " + currentNode.getXPath() + " )";
                }
                return currentNode.getXPath();
            } else if (parentFound) {
                if (path.toString().equals(EMPTY_STRING)) {
                    if (currentNode.getProperty(TYPE).equals(BOOLEAN_TYPE)) {
                        return currentNode.getName() + " eq 'true' ";
                    } else if (currentNode.getProperty(TYPE).equals(NUMBER_TYPE)) {
                        return "number( " + currentNode.getName() + " )";
                    }
                    return currentNode.getName();
                }
                if (currentNode.getProperty(TYPE).equals(BOOLEAN_TYPE)) {
                    return path + SLASH + currentNode.getName() + " eq 'true' ";
                } else if (currentNode.getProperty(TYPE).equals(NUMBER_TYPE)) {
                    return "number( " + path + SLASH + currentNode.getName() + " )";
                }
                return path + SLASH + currentNode.getName();
            } else {
                String[] cPathParts = currentInNode.getXPath().split(SLASH);
                String[] xPathParts = currentNode.getXPath().split(SLASH);
                boolean matching = true;
                if (!cPathParts[0].equals(xPathParts[0])) {
                    matching = false;
                    path = new StringBuilder(inPutNode.getName());
                } else {
                    cPathParts = removeElementAt(cPathParts, 0);
                    xPathParts = removeElementAt(xPathParts, 0);
                }
                while ((matching && cPathParts.length > 0) && (xPathParts.length > 0 &&
                        xPathParts[0].equals(cPathParts[0]))) {
                    xPathParts = removeElementAt(xPathParts, 0);
                    cPathParts = removeElementAt(cPathParts, 0);
                }
                for (String s : xPathParts) {
                    if (path.toString().equals(EMPTY_STRING)) {
                        path = new StringBuilder(s);
                    } else {
                        path.append(SLASH).append(s);
                    }
                }
                for (String s : cPathParts) {
                    if (!s.equals(EMPTY_STRING)) {
                        path.insert(0, "../");
                    }
                }
                if (currentNode.getProperty(TYPE).equals(BOOLEAN_TYPE)) {
                    return path + " eq 'true' ";
                } else if (currentNode.getProperty(TYPE).equals(NUMBER_TYPE)) {
                    return "number( " + path + " )";
                }
                return path.toString();
            }

        }

        return EMPTY_STRING;
    }

    /**
     * This method will check whether a given inNode belong to an operator
     *
     * @param inNode inNode that needed to be tested
     * @return return the result of the test
     */
    private static boolean isOperator(String inNode) {
        return inNode.startsWith(AT_OPERATORS);
    }

    /**
     * This method will traverse through the input tree and find the matching array for given
     * output node
     *
     * @param node          array type output node
     * @param operatorNodes operators array
     * @return input node that maps to the given output node
     */
    private static InPutNode traverseArray(OutPutNode node, ArrayList<OperatorNode> operatorNodes) {
        ArrayList<InPutNode> arrayNodes = new ArrayList<>();
        if (node.getProperty(TYPE).equals(ARRAY_TYPE)) {
            if (node.getProperty(ITEMS_TYPE).equals(OBJECT_TYPE)) {
                for (OutPutNode childNode : node.getChildNodes()) {
                    InPutNode returnNode = traverseArray(childNode, operatorNodes);
                    if (returnNode != null && returnNode.getProperty(TYPE).equals(ARRAY_TYPE)) {
                        arrayNodes.add(returnNode);
                    }
                }
            } else if (!node.getInNode().getOutNodes().isEmpty()) {
                return getArrayElement(node.getInNode().getOutNodes().get(0), operatorNodes);
            }
        } else if (node.getProperty(TYPE).equals(OBJECT_TYPE)) {
            for (OutPutNode childNode : node.getChildNodes()) {
                InPutNode returnNode = traverseArray(childNode, operatorNodes);
                if (returnNode != null && returnNode.getProperty(TYPE).equals(ARRAY_TYPE)) {
                    arrayNodes.add(returnNode);
                }
            }
        } else if (!node.getInNode().getOutNodes().isEmpty()) {
            return getArrayElement(node.getInNode().getOutNodes().get(0), operatorNodes);
        }
        return getHighestLevelNode(arrayNodes);
    }

    /**
     * This method evaluate given xPath and return the array type node
     *
     * @param inNode        xPath of the array type node
     * @param operatorNodes operators array
     * @return array type node matches the given xPath
     */
    private static InPutNode getArrayElement(String inNode, ArrayList<OperatorNode> operatorNodes) {
        if (isOperator(inNode)) {
            OperatorNode operatorNode = operatorNodes.get(Integer.parseInt(inNode.split
                    (DOT_SYMBOL)[1].split(SLASH)[0]));
            ArrayList<InPutNode> inNodes = new ArrayList<>();
            for (String childInNode : operatorNode.getLeftContainer().getInNodes()) {
                InPutNode returnInNode = getArrayElement(childInNode, operatorNodes);
                if (returnInNode != null && returnInNode.getProperty(TYPE).equals(ARRAY_TYPE)) {
                    inNodes.add(returnInNode);
                }
            }
            return getHighestLevelNode(inNodes);
        } else {
            String[] nodes = inNode.split(DOT_SYMBOL);
            int currentNodeIndex = 1;
            String inputIndex = nodes[currentNodeIndex].split(SLASH)[0];
            InPutNode currentNode = inPutNodes.get(Integer.parseInt(inputIndex));
            String remainingInNodeString = inNode.substring(inNode.indexOf(AT_NODE));
            while (remainingInNodeString.contains(AT_NODE)) {
                currentNodeIndex++;
                String indexOfChild = nodes[currentNodeIndex].split(SLASH)[0];
                currentNode = currentNode.getChildNodes().get(Integer.parseInt(indexOfChild));
                remainingInNodeString = remainingInNodeString.substring(indexOfChild.length() + 1
                        + AT_NODE.length());
            }
            while (!(currentNode.getProperty(TYPE).equals(ARRAY_TYPE) || currentNode
                    .getParentNode() == null)) {
                if (currentNode.getParentNode() != null) {
                    currentNode = currentNode.getParentNode();
                }
            }
            return currentNode;
        }
    }

    /**
     * This method finds the highest level array node in an array of nodes
     *
     * @param arrayNodes array type input nodes
     * @return the array type node in highest level
     */
    private static InPutNode getHighestLevelNode(ArrayList<InPutNode> arrayNodes) {
        int maxArray = 0;
        for (int i = 0; i < arrayNodes.size(); i++) {
            if (arrayNodes.get(i).getLevel() > arrayNodes.get(maxArray).getLevel()) {
                maxArray = i;
            }
        }
        if (arrayNodes.size() > maxArray) {
            return arrayNodes.get(maxArray);
        }
        return null;
    }

    /**
     * This method removes an element from a string array given the index
     *
     * @param array      string array
     * @param removedIdx index of the element
     * @return return new array
     */
    private static String[] removeElementAt(String[] array, int removedIdx) {
        String[] newArray = new String[array.length - 1];
        for (int i = 0; i < array.length; i++) {
            if (i < removedIdx) {
                newArray[i] = array[i];
            } else if (i > removedIdx) {
                newArray[i - 1] = array[i];
            }
        }
        return newArray;
    }

}