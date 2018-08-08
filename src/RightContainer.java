import java.util.ArrayList;
import org.w3c.dom.Node;

public class RightContainer {
    private ArrayList<String> outNodes;

    public RightContainer(Node node){
        this.outNodes = new ArrayList<>();
        populate(node);
    }

    public void populate(Node node){
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node rightConnector = node.getChildNodes().item(i);
            if (rightConnector.getAttributes() != null && rightConnector.getNodeName().equals("rightConnectors")) {
                for(int j=0;j<rightConnector.getChildNodes().getLength();j++){
                    Node outNode = rightConnector.getChildNodes().item(j);
                    if(outNode.getAttributes()!=null && outNode.getNodeName().equals("outNode")){
                        if(getAttribute(outNode,"outgoingLink")==null){
                            for(int k=0; k< outNode.getChildNodes().getLength(); k++){
                                Node outgoingLink = outNode.getChildNodes().item(k);
                                if(outgoingLink.getAttributes()!=null && outgoingLink.getNodeName().equals("outgoingLink")){
                                    for(int z=0; z<outgoingLink.getAttributes().getLength(); z++){
                                        if(outgoingLink.getAttributes().item(z).getNodeName().equals("inNode")){
                                            this.outNodes.add(outgoingLink.getAttributes().item(z).getTextContent());
                                        }
                                    }
                                }
                            }
                        }else{
                            this.outNodes.add(getAttribute(outNode,"outgoingLink"));
                        }
                    }
                }
            }
        }
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

    public void printContainer(){
        System.out.println("-------------- Right container ----------------");
        for (String i:this.outNodes){
            System.out.println(i);
        }
        System.out.println("--------------               ------------------");
    }
}
