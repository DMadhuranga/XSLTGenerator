package gen;

import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Node;

public class InPutNode extends XMLNode{
    private String schemaDataType;
    private String level;
    private OutNode outNode;
    private InNode inNode;
    private ArrayList<InPutNode> childNodes;
    private HashMap<String,String> properties;
    private InPutNode parentNode;
    private String xPath;

    public String getxPath() {
        return xPath;
    }

    public InPutNode(Node node, InPutNode parentNode, String xPath){
        this.schemaDataType = getAttribute(node,"schemaDataType");
        this.name = getAttribute(node,"name");
        this.level = getAttribute(node,"level");
        this.properties = new HashMap<>();
        this.childNodes = new ArrayList<>();
        this.parentNode = parentNode;
        if(xPath.equals("")){
            this.xPath = xPath+this.name;
        }else{
            this.xPath = xPath+"/"+this.name;
        }
        populate(node);
    }

    public InPutNode getParentNode() {
        return parentNode;
    }

    public void populate(Node node){
        for(int i=0;i<node.getChildNodes().getLength();i++){
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getAttributes() != null) {
                if(childNode.getNodeName().equals("properties")){
                    this.properties.put(childNode.getAttributes().getNamedItem("key").getTextContent(),childNode.getAttributes().getNamedItem("value").getTextContent());
                }else if (childNode.getNodeName().equals("node")) {
                    this.childNodes.add(new InPutNode(childNode,this,this.xPath));
                }else if (childNode.getNodeName().equals("outNode")) {
                    this.outNode = new OutNode(childNode);
                }else if (childNode.getNodeName().equals("inNode")) {
                    this.inNode = new InNode(childNode);
                }
            }
        }
    }

    public String getProperty(String property){
        if(this.properties.containsKey(property)){
            return this.properties.get(property);
        }
        return null;
    }

    public int getLevel() {
        return Integer.parseInt(level);
    }

    public String getName() {
        return name;
    }

    public ArrayList<InPutNode> getChildNodes(){
        return this.childNodes;
    }


}
