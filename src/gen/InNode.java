package gen;

import java.util.ArrayList;
import org.w3c.dom.Node;

public class InNode {
    private ArrayList<String> outNodes;

    public InNode(Node node){
        this.outNodes = new ArrayList<>();
        if(getAttribute(node,"incomingLink")!=null){
            this.outNodes.add(getAttribute(node,"incomingLink"));
        }else {
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                Node childNode = node.getChildNodes().item(i);
                if (childNode.getAttributes() != null) {
                    if (childNode.getNodeName().equals("incomingLink") && childNode.getAttributes().getNamedItem("outNode")!=null) {
                        this.outNodes.add(childNode.getAttributes().getNamedItem("outNode").getTextContent());
                    }
                }
            }
        }
    }

    public ArrayList<String> getOutNodes() {
        return outNodes;
    }

    public String getAttribute(Node node, String property){
        if(node.getAttributes()!=null && node.getNodeName().equals("inNode")){
            try {
                return node.getAttributes().getNamedItem(property).getTextContent();
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }

    public void printInNode(){
        System.out.println("__________ In nodes _______________");
        for(String i : outNodes){
            System.out.println(i);
        }
        System.out.println("__________            _______________");
    }


}
