package gen;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.util.HashMap;
import java.util.Map;

public class XSLTGenerator {
    public static void main(String[] args){
        ReadXMLFile inputXML = new ReadXMLFile("/home/danushka/workspace/SampleFlowRegistry/RequestMapper.datamapper");
        WriteXMLFile outputXML = new WriteXMLFile("/home/danushka/Downloads/output.xml");
        Element rootElemenet = outputXML.getDocument().createElement("xsl:stylesheet");
        rootElemenet.setAttribute("xmlns:xsl","http://www.w3.org/1999/XSL/Transform");
        outputXML.getDocument().appendChild(rootElemenet);
        rootElemenet.setAttribute("version","1.0");
        Element templateElement = outputXML.getDocument().createElement("xsl:template");
        templateElement.setAttribute("match","/");
        rootElemenet.appendChild(templateElement);
        //System.out.println(inputXML.getDocument().getElementsByTagName("input").item(0).getChildNodes().item(0).getNodeName());
        HashMap<String,Object> output = new HashMap<>();
        traverseOutput(inputXML,outputXML,output,rootElemenet,templateElement);
        outputXML.saveFile();
    }

    public static void traverseOutput(ReadXMLFile inputxmlFile, WriteXMLFile outputXMLFile, Map<String,Object> map, Element rootElement, Element templateElement){
        Node outputNode = inputxmlFile.getDocument().getElementsByTagName("output").item(0);
        for(int i=0; i<outputNode.getChildNodes().getLength(); i++) {
            if (outputNode.getChildNodes().item(i).getNodeName().equals("treeNode")) {
                Node treeNode = outputNode.getChildNodes().item(i);
                Element topElement = outputXMLFile.getDocument().createElement(treeNode.getAttributes().getNamedItem("name").getTextContent());
                templateElement.appendChild(topElement);
                for (int j = 0; j < treeNode.getChildNodes().getLength(); j++) {
                    Node childNode = treeNode.getChildNodes().item(j);
                    if (childNode.getAttributes() != null && childNode.getNodeName().equals("node")) {
                        traverseNode(inputxmlFile,outputXMLFile,childNode,map,rootElement,topElement);
                        //System.out.println(node.getNodeName()+"  :  "+node.getAttributes().getNamedItem("schemaDataType").getNodeValue());
                    }
                }
            }
        }
    }

    public static void traverseNode(ReadXMLFile inputxmlFile, WriteXMLFile outputXMLFile,Node currentNode, Map<String,Object> map, Element rootElement, Element parentElement){
        //System.out.println(currentNode.getAttributes().getNamedItem("schemaDataType").getNodeValue());
        Element currentElement = outputXMLFile.getDocument().createElement(currentNode.getAttributes().getNamedItem("name").getTextContent());
        parentElement.appendChild(currentElement);
        String schemaDataType = getSchemaDataType(currentNode);
        if(schemaDataType==null){
            for(int i=0;i<currentNode.getChildNodes().getLength();i++){
                Node childNode = currentNode.getChildNodes().item(i);
                if (childNode.getAttributes() != null && childNode.getNodeName().equals("node")) {
                    traverseNode(inputxmlFile,outputXMLFile,childNode,map,rootElement,currentElement);
                }
            }
        }else{
            System.out.println("");
        }
    }

    public static String getSchemaDataType(Node node){
        if(node.getAttributes()!=null && node.getNodeName().equals("node")){
            try {
                return node.getAttributes().getNamedItem("schemaDataType").getTextContent();
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }

}
