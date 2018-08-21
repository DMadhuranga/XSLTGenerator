package gen;

import java.util.HashMap;
import org.w3c.dom.Node;

public class OperatorNode {
    private HashMap<String,String> attributes;
    private LeftContainer leftContainer;
    private RightContainer rightContainer;

    public OperatorNode(Node node){
        this.attributes = new HashMap<>();
        populate(node);
    }

    public void populate(Node node){
        for(int i=0;i<node.getChildNodes().getLength();i++){
            Node basicContainer = node.getChildNodes().item(i);
            if(basicContainer.getAttributes()!=null && basicContainer.getNodeName().equals("basicContainer")){
                for(int j=0; j<basicContainer.getChildNodes().getLength(); j++){
                    Node childNode = basicContainer.getChildNodes().item(j);
                    if(childNode.getAttributes()!=null){
                        if(childNode.getNodeName().equals("leftContainer")){
                            this.leftContainer = new LeftContainer(childNode);
                        }else if(childNode.getNodeName().equals("rightContainer")){
                            this.rightContainer= new RightContainer(childNode);
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

    public RightContainer getRightContainer() {
        return rightContainer;
    }

    public void printOperator(){
        System.out.println("<---------   Operator -------->");
        System.out.println("Properties:   ");
        for(String key:this.attributes.keySet()){
            System.out.println(key + " : "+this.attributes.get(key));
        }
        System.out.println("####### propreties");
        this.leftContainer.printContainer();
        this.rightContainer.printContainer();
        System.out.println("<---------            -------->");
    }

    public String getProperty(String property){
        return this.attributes.get(property);
    }
}
