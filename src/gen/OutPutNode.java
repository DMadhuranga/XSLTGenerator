package gen;

import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Node;

public class OutPutNode extends XMLNode{
    private String xPath;

    public InNode getInNode() {
        return inNode;
    }

    private InNode inNode;
    private ArrayList<OutPutNode> childNodes;
    private HashMap<String,String> properties;

    public OutPutNode(Node node, String parentXPath){
        this.name = getAttribute(node,"name");
        this.properties = new HashMap<>();
        this.childNodes = new ArrayList<>();
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

    private void populate(Node node){
        for(int i=0;i<node.getChildNodes().getLength();i++){
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getAttributes() != null) {
                switch (childNode.getNodeName()) {
                    case "properties":
                        this.properties.put(childNode.getAttributes().getNamedItem("key").getTextContent(), childNode.getAttributes().getNamedItem("value").getTextContent());
                        break;
                    case "node":
                        this.childNodes.add(new OutPutNode(childNode, this.xPath));
                        break;
                    case "inNode":
                        this.inNode = new InNode(childNode);
                        break;
                }
            }
        }
    }

    public ArrayList<OutPutNode> getChildNodes(){
        return this.childNodes;
    }


}
