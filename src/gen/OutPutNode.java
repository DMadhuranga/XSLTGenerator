package gen;

import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Node;

public class OutPutNode extends XMLNode{
    private String schemaDataType;
    private String level;
    private OutNode outNode;
    private String xPath;
    private OutPutNode parentNode;

    public InNode getInNode() {
        return inNode;
    }

    private InNode inNode;
    private ArrayList<OutPutNode> childNodes;
    private HashMap<String,String> properties;

    public OutPutNode(Node node,OutPutNode parentNode, String parentXPath){
        this.schemaDataType = getAttribute(node,"schemaDataType");
        this.name = getAttribute(node,"name");
        this.level = getAttribute(node,"level");
        this.properties = new HashMap<>();
        this.childNodes = new ArrayList<>();
        this.parentNode = parentNode;
        if(parentXPath.equals("")){
            this.xPath = parentXPath+this.name;
        }else{
            this.xPath = parentXPath+"/"+this.name;
        }
        populate(node);
    }

    public String getProperty(String property){
        if(this.properties.containsKey(property)){
            return this.properties.get(property);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void populate(Node node){
        for(int i=0;i<node.getChildNodes().getLength();i++){
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getAttributes() != null) {
                if(childNode.getNodeName().equals("properties")){
                    this.properties.put(childNode.getAttributes().getNamedItem("key").getTextContent(),childNode.getAttributes().getNamedItem("value").getTextContent());
                }else if (childNode.getNodeName().equals("node")) {
                    this.childNodes.add(new OutPutNode(childNode,this,this.xPath));
                }else if (childNode.getNodeName().equals("outNode")) {
                    this.outNode = new OutNode(childNode);
                }else if (childNode.getNodeName().equals("inNode")) {
                    this.inNode = new InNode(childNode);
                }
            }
        }
    }

    public ArrayList<OutPutNode> getChildNodes(){
        return this.childNodes;
    }


}
