import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

public class XSLTCreator {

    private static String stringType = "string";
    private static String objectType = "object";
    private static String arrayType = "array";
    private static String numberType = "number";
    private static String booleanType = "boolean";
    private static String type = "type";
    private static String itemsType = "items_type";
    //private static int currentLevel = 0;
    private static ArrayList<InPutNode> inPutNodes;
    private static ArrayList<OutPutNode> outPutNodes;
    private static InPutNode currentInNode;

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
        createInputNode(inputXML);
        createOutputNode(inputXML,outputXML,rootElemenet,templateElement,operatorNodes);
        for (OutPutNode outPutNode:outPutNodes) {
            traverseOutPutNode(outPutNode,operatorNodes,outputXML,rootElemenet,templateElement);
        }
        outputXML.saveFile();
    }

    public static void createOutputNode(ReadXMLFile inputxmlFile, WriteXMLFile outputXMLFile,Element rootElement, Element templateElement, ArrayList<OperatorNode> operatorNodes){
        outPutNodes = new ArrayList<>();
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
                outPutNodes.add(new OutPutNode(outputNode.getChildNodes().item(i),null,""));
            }
        }
    }

    public static void createInputNode(ReadXMLFile inputxmlFile){
        inPutNodes = new ArrayList<>();
        Node inputNode = inputxmlFile.getDocument().getElementsByTagName("input").item(0);
        for(int i=0;i<inputNode.getChildNodes().getLength();i++){
            if(inputNode.getChildNodes().item(i).getNodeName().equals("treeNode")){
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

    public static void traverseOutPutNode(OutPutNode node,ArrayList<OperatorNode> operatorNodes, WriteXMLFile outputXMLFile, Element rootElement, Element parentElement){
        //System.out.println(currentNode.getAttributes().getNamedItem("schemaDataType").getNodeValue());
        if(node.getProperties().get("type").equals("string") || node.getProperties().get("type").equals("number") || node.getProperties().get("type").equals("boolean")){
            if(node.getInNode().getOutNodes().size()>0){
                Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                parentElement.appendChild(currentElement);
                if(node.getInNode().getOutNodes().size()>0){
                    Element valueOfElement = outputXMLFile.getDocument().createElement("xsl:value-of");
                    currentElement.appendChild(valueOfElement);
                    valueOfElement.setAttribute("select",getValueFromMapping(operatorNodes,node.getInNode().getOutNodes().get(0)));
                }
            }
        }else if(node.getProperties().get("type").equals("array")){
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
                    if (node.getProperty(itemsType).equals(objectType)) {
                        for (OutPutNode childNode : node.getChildNodes()) {
                            traverseOutPutNode(childNode, operatorNodes, outputXMLFile, rootElement, currentElement);
                        }
                    } else if (node.getInNode().getOutNodes().size() > 0) {
                        Element valueOfElement = outputXMLFile.getDocument().createElement("xsl:value-of");
                        currentElement.appendChild(valueOfElement);
                        valueOfElement.setAttribute("select", getValueFromMapping(operatorNodes,node.getInNode().getOutNodes().get(0)));
                    }
                }
            }else if(node.getInNode().getOutNodes().size()>0){
                Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
                parentElement.appendChild(currentElement);
                Element valueOfElement = outputXMLFile.getDocument().createElement("xsl:value-of");
                currentElement.appendChild(valueOfElement);
                valueOfElement.setAttribute("select",getValueFromMapping(operatorNodes,node.getInNode().getOutNodes().get(0)));
            }
            currentInNode = previousCurrentNode;
        }else{
            Element currentElement = outputXMLFile.getDocument().createElement(node.getName());
            parentElement.appendChild(currentElement);
            for(OutPutNode childNode:node.getChildNodes()){
                traverseOutPutNode(childNode,operatorNodes,outputXMLFile,rootElement,currentElement);
            }
        }
    }

    public static String getArrayPath(InPutNode node){
        if(currentInNode==null){
            InPutNode tempNode = node;
            while (tempNode.getParentNode()!=null){
                if(tempNode.getParentNode().getProperty(type).equals(arrayType)){
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
                if(tempNode.getParentNode().getProperty(type).equals(arrayType)){
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
        InPutNode tempNode = node;
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

    public static String getValueFromMapping(ArrayList<OperatorNode> operatorNodes,String inNode){
        if(isOperator(inNode)){

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
                return currentNode.getxPath();
            }else if(parentFound){
                if(path.equals("")){
                    return currentNode.getName();
                }
                return path+"/"+currentNode.getName();
            }else{
                String[] cPathParts = currentInNode.getxPath().split("/");
                String[] xPathParts = currentNode.getxPath().split("/");
                boolean matching = true;
                if(!cPathParts[0].equals(inPutNode.getName())){
                    matching = false;
                    path = inPutNode.getName();
                }else{
                    cPathParts = removeElementAt(cPathParts,0);
                }
                while ((matching && cPathParts.length>0) && (xPathParts.length>0 && xPathParts[0].equals(cPathParts[0]))){
                    xPathParts = removeElementAt(xPathParts,0);
                    cPathParts = removeElementAt(cPathParts,0);
                }
                for (String s:xPathParts) {
                    if(s.equals("")){
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
            InPutNode currentNode = inPutNode;
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

    /*public static InPutNode getInNode(InPutNode inPutNode, ArrayList<OperatorNode> operatorNodes, String inNode){
        if(isOperator(inNode)){
            OperatorNode operatorNode = operatorNodes.get(Integer.parseInt(inNode.substring(13,14)));
            String inNodeString = StringUtils.substring(inNode,20);
            while (inNodeString.contains("/@node")){
                currentNode = currentNode.getChildNodes().get(Integer.parseInt(inNodeString.substring(7,8)));
                inNodeString = StringUtils.substring(inNodeString,8);
            }
            return currentNode;
        }else{
            InPutNode currentNode = inPutNode.getChildNodes().get(Integer.parseInt(inNode.substring(19,20)));
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
        if(node.getProperty(type).equals(arrayType)){
            if(node.getProperty(itemsType).equals(objectType)){
                for (OutPutNode childNode:node.getChildNodes()) {
                    InPutNode returnNode = traverseArray(childNode,operatorNodes);
                    if(returnNode!=null && returnNode.getProperty(type).equals(arrayType)){
                        arrayNodes.add(returnNode);
                    }
                }
            }else if(node.getInNode().getOutNodes().size()>0){
                return getArrayElement(node.getInNode().getOutNodes().get(0),operatorNodes);
            }
        }else if(node.getProperty(type).equals(objectType)){
            for (OutPutNode childNode:node.getChildNodes()){
                InPutNode returnNode = traverseArray(childNode,operatorNodes);
                if(returnNode!=null && returnNode.getProperty(type).equals(arrayType)){
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
                if(returnInNode!=null && returnInNode.getProperty(type).equals(arrayType)){
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
            while (!(currentNode.getProperty(type).equals(arrayType) || currentNode.getParentNode()==null)){
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