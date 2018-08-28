package gen;

import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Node;

public class InPutNode extends XMLNode{
    private String level;
    private ArrayList<InPutNode> childNodes;
    private HashMap<String,String> properties;
    private InPutNode parentNode;
    private String xPath;

    public String getXPath() {
        return xPath;
    }

    public InPutNode(Node node, InPutNode parentNode, String xPath){
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

    private void populate(Node node){
        for(int i=0;i<node.getChildNodes().getLength();i++){
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getAttributes() != null) {
                if(childNode.getNodeName().equals("properties")){
                    this.properties.put(childNode.getAttributes().getNamedItem("key").getTextContent(),childNode.getAttributes().getNamedItem("value").getTextContent());
                }else if (childNode.getNodeName().equals("node")) {
                    this.childNodes.add(new InPutNode(childNode,this,this.xPath));
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
