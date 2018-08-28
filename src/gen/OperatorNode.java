package gen;

import java.util.HashMap;
import org.w3c.dom.Node;

public class OperatorNode {
    private HashMap<String,String> attributes;
    private LeftContainer leftContainer;

    public OperatorNode(Node node){
        this.attributes = new HashMap<>();
        populate(node);
    }

    private void populate(Node node){
        for(int i=0;i<node.getChildNodes().getLength();i++){
            Node basicContainer = node.getChildNodes().item(i);
            if(basicContainer.getAttributes()!=null && basicContainer.getNodeName().equals("basicContainer")){
                for(int j=0; j<basicContainer.getChildNodes().getLength(); j++){
                    Node childNode = basicContainer.getChildNodes().item(j);
                    if(childNode.getAttributes()!=null){
                        if(childNode.getNodeName().equals("leftContainer")){
                            this.leftContainer = new LeftContainer(childNode);
                        }
                    }
                }
            }
        }
        for(int i=0;i<node.getAttributes().getLength();i++){
            Node childNode = node.getAttributes().item(i);
            this.attributes.put(childNode.getNodeName(),childNode.getTextContent());
        }
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public LeftContainer getLeftContainer() {
        return leftContainer;
    }

    public String getProperty(String property){
        return this.attributes.get(property);
    }
}
