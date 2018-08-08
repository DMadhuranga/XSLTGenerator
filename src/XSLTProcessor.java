import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

public class XSLTProcessor {

    private static String stringType = "string";
    private static String objectType = "object";
    private static String arrayType = "array";
    private static String numberType = "number";
    private static String booleanType = "boolean";
    private static String type = "type";
    private static String itemsType = "items_type";

    public static void main(String[] args){
        ReadXMLFile inputXML = new ReadXMLFile("/home/danushka/workspace/SampleFlowRegistry/NewConfig1.datamapper");
        WriteXMLFile outputXML = new WriteXMLFile("/home/danushka/Downloads/output.xml");
        Element rootElemenet = outputXML.getDocument().createElement("xsl:stylesheet");
        rootElemenet.setAttribute("xmlns:xsl","http://www.w3.org/1999/XSL/Transform");
        outputXML.getDocument().appendChild(rootElemenet);
        rootElemenet.setAttribute("version","1.0");
        Element templateElement = outputXML.getDocument().createElement("xsl:template");
        templateElement.setAttribute("match","/");
        rootElemenet.appendChild(templateElement);
        ArrayList<OperatorNode> operatorNodes = createOperatorNodes(inputXML);
        InPutNode inPutNode = createInputNode(inputXML);
        OutPutNode outPutNode = createOutputNode(inputXML,outputXML,rootElemenet,templateElement,inPutNode,operatorNodes);
        traverseOutPutNode(outPutNode,inPutNode,operatorNodes,outputXML,rootElemenet,templateElement,0);
        outputXML.saveFile();
    }

    public static OutPutNode createOutputNode(ReadXMLFile inputxmlFile, WriteXMLFile outputXMLFile,Element rootElement, Element templateElement, InPutNode inPutNode, ArrayList<OperatorNode> operatorNodes){
        Node outputNode = inputxmlFile.getDocument().getElementsByTagName("output").item(0);
        for(OperatorNode operatorNode:operatorNodes){
            if(operatorNode.getProperty("operatorType").equals("CONSTANT")){
                Element v = outputXMLFile.getDocument().createElement("xsl:param");
                v.setAttribute("name","operators."+Integer.toString(operatorNodes.indexOf(operatorNode)));
                v.setAttribute("select","'"+operatorNode.getProperty("constantValue")+"'");
                templateElement.appendChild(v);
            }
        }
        for(int i=0;i<outputNode.getChildNodes().getLength();i++){
            if(outputNode.getChildNodes().item(i).getNodeName().equals("treeNode")){
                return new OutPutNode(outputNode.getChildNodes().item(i),null,"");
            }
        }
        return null;
    }

    public static InPutNode createInputNode(ReadXMLFile inputxmlFile){
        Node inputNode = inputxmlFile.getDocument().getElementsByTagName("input").item(0);
        for(int i=0;i<inputNode.getChildNodes().getLength();i++){
            if(inputNode.getChildNodes().item(i).getNodeName().equals("treeNode")){
                return new InPutNode(inputNode.getChildNodes().item(i),null,"");
            }
        }
        return null;
    }

    public static ArrayList<OperatorNode> createOperatorNodes(ReadXMLFile inputxmlFile){
        ArrayList<OperatorNode> operatorNodes = new ArrayList<>();
        NodeList operators = inputxmlFile.getDocument().getElementsByTagName("operators");
        for(int i=0;i<operators.getLength();i++){
            operatorNodes.add(new OperatorNode(operators.item(i)));
        }
        return operatorNodes;
    }

    public static int traverseOutPutNode(OutPutNode node,InPutNode inPutNode,ArrayList<OperatorNode> operatorNodes, WriteXMLFile outputXMLFile, Element rootElement, Element parentElement,int currentLevel){
        //System.out.println(currentNode.getAttributes().getNamedItem("schemaDataType").getNodeValue());
        if(node.getProperties().get("type").equals("string")){
            if(node.getInNode().getOutNodes().size()>0){
                Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                parentElement.appendChild(currentElement);
                if(node.getInNode().getOutNodes().size()>0){
                    Element valueOfElement = outputXMLFile.getDocument().createElement("xsl:value-of");
                    currentElement.appendChild(valueOfElement);
                    return setStringValueFromMapping(valueOfElement,inPutNode,operatorNodes,currentLevel,node.getInNode().getOutNodes().get(0));
                }
            }
        }else if(node.getProperties().get("type").equals("number")){
            if(node.getInNode().getOutNodes().size()>0){
                Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                parentElement.appendChild(currentElement);
                if(node.getInNode().getOutNodes().size()>0){
                    Element valueOfElement = outputXMLFile.getDocument().createElement("xsl:value-of");
                    currentElement.appendChild(valueOfElement);
                    return setNumberValueFromMapping(valueOfElement,inPutNode,operatorNodes,currentLevel,node.getInNode().getOutNodes().get(0));
                }
            }
        }else if(node.getProperties().get("type").equals("array")){
            ArrayList<String> inNodes;
            if(node.getInNode().getOutNodes().size()>0){
                inNodes = new ArrayList<>();
                inNodes.add(node.getInNode().getOutNodes().get(0));
                Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                parentElement.appendChild(currentElement);
            }else{

            }
            Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
            parentElement.appendChild(currentElement);
            if(node.getInNode().getOutNodes().size()>0){
                currentElement.setAttribute("InNode",node.getInNode().getOutNodes().get(0));
            }else{
                currentElement.setAttribute("InNode","Not assigned");
            }
        }else{
            Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
            parentElement.appendChild(currentElement);
            for(OutPutNode childNode:node.getChildNodes()){
                traverseOutPutNode(childNode,inPutNode,operatorNodes,outputXMLFile,rootElement,currentElement,currentLevel);
            }
        }
        return currentLevel;
    }

    public static int setStringValueFromMapping(Element currentElement, InPutNode inPutNode, ArrayList<OperatorNode> operatorNodes, int currentLevel, String inNode){
        if(isOperator(inNode)){

        }else{
            String xPath = inPutNode.getName();
            InPutNode currentNode = inPutNode;
            String inNodeString = StringUtils.substring(inNode,20);
            while (inNodeString.contains("/@node")){
                currentNode = currentNode.getChildNodes().get(Integer.parseInt(inNodeString.substring(7,8)));
                inNodeString = StringUtils.substring(inNodeString,8);
                xPath=xPath+"/"+currentNode.getName();
            }
            if(currentNode.getProperty(type).equals(stringType)){
                int tempLevel = currentLevel;
                while (tempLevel!=0){
                    xPath="../"+xPath;
                    tempLevel--;
                }
                currentElement.setAttribute("select",xPath);
            }
        }
        return currentLevel;
    }

    public static int setNumberValueFromMapping(Element currentElement, InPutNode inPutNode, ArrayList<OperatorNode> operatorNodes, int currentLevel, String inNode){
        if(isOperator(inNode)){

        }else{
            String xPath = inPutNode.getName();
            InPutNode currentNode = inPutNode;
            String inNodeString = StringUtils.substring(inNode,20);
            while (inNodeString.contains("/@node")){
                currentNode = currentNode.getChildNodes().get(Integer.parseInt(inNodeString.substring(7,8)));
                inNodeString = StringUtils.substring(inNodeString,8);
                xPath=xPath+"/"+currentNode.getName();
            }
            if(currentNode.getProperty(type).equals(numberType)){
                int tempLevel = currentLevel;
                while (tempLevel!=0){
                    xPath="../"+xPath;
                    tempLevel--;
                }
                currentElement.setAttribute("select",xPath);
            }
        }
        return currentLevel;
    }

    public static boolean isOperator(String inNode){
        if(inNode.startsWith("//@operators")){
            return true;
        }
        return false;
    }

    public static InPutNode getInNode(InPutNode inPutNode, String inNode){
        InPutNode currentNode = inPutNode.getChildNodes().get(Integer.parseInt(inNode.substring(19,20)));
        String inNodeString = StringUtils.substring(inNode,20);
        while (inNodeString.contains("/@node")){
            currentNode = currentNode.getChildNodes().get(Integer.parseInt(inNodeString.substring(7,8)));
            inNodeString = StringUtils.substring(inNodeString,8);
        }
        return currentNode;
    }

    public static InPutNode getArrayElement(InPutNode inPutNode, String inNode){
        return null;
    }

    public static InPutNode traverseArray(OutPutNode node, InPutNode inPutNode,ArrayList<OperatorNode> operatorNodes, int currentLevel, String currentPath){
        for(OutPutNode childNode:node.getChildNodes()){
            if(childNode.getProperty(type).equals(arrayType)){
                InPutNode tempNode = traverseArray(childNode,inPutNode,operatorNodes,currentLevel,currentPath);
            }else{
                
            }
            while(!(childNode.getProperty(type).equals(stringType) || childNode.getProperty(type).equals(numberType) || childNode.getProperty(type).equals(booleanType))){
                String a;
            }
        }
        return null;
    }

}