package gen;

import java.util.ArrayList;
import org.w3c.dom.Node;

public class OutNode {
    private ArrayList<String> inNodes;

    public OutNode(Node node){
        this.inNodes = new ArrayList<>();
        if(getAttribute(node,"outgoingLink")!=null){
            this.inNodes.add(getAttribute(node,"outgoingLink"));
        }else {
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                Node childNode = node.getChildNodes().item(i);
                if (childNode.getAttributes() != null) {
                    if (childNode.getNodeName().equals("outgoingLink") && childNode.getAttributes().getNamedItem("inNode")!=null) {
                        this.inNodes.add(childNode.getAttributes().getNamedItem("inNode").getTextContent());
                    }
                }
            }
        }
    }

    public ArrayList<String> getInNodes() {
        return inNodes;
    }

    public String getAttribute(Node node, String property){
        if(node.getAttributes()!=null && node.getNodeName().equals("outNode")){
            try {
                return node.getAttributes().getNamedItem(property).getTextContent();
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }

    public void printOutNode(){
        System.out.println("__________ Out nodes _______________");
        for(String i : inNodes){
            System.out.println(i);
        }
        System.out.println("__________            _______________");
    }


}
