import java.util.ArrayList;
import org.w3c.dom.Node;

public class LeftContainer {
    private ArrayList<String> inNodes;

    public LeftContainer(Node node){
        this.inNodes = new ArrayList<>();
        populate(node);
    }

    public ArrayList<String> getInNodes() {
        return inNodes;
    }

    public void populate(Node node){
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node leftConnector = node.getChildNodes().item(i);
            if (leftConnector.getAttributes() != null && leftConnector.getNodeName().equals("leftConnectors")) {
                for(int j=0;j<leftConnector.getChildNodes().getLength();j++){
                    Node inNode = leftConnector.getChildNodes().item(j);
                    if(inNode.getAttributes()!=null && inNode.getNodeName().equals("inNode")){
                        if(getAttribute(inNode,"incomingLink")==null){
                            for(int k=0; k< inNode.getChildNodes().getLength(); k++){
                                Node incomingLink = inNode.getChildNodes().item(k);
                                if(incomingLink.getAttributes()!=null && incomingLink.getNodeName().equals("incomingLink")){
                                    if(getAttribute(incomingLink,"outNode")!=null){
                                        this.inNodes.add(getAttribute(incomingLink,"outNode"));
                                    }
                                }
                            }
                        }else{
                            this.inNodes.add(getAttribute(inNode,"incomingLink"));
                        }
                    }
                }
            }
        }
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

    public void printContainer(){
        System.out.println("-------------- Left container ----------------");
        for (String i:this.inNodes){
            System.out.println(i);
        }
        System.out.println("--------------               ------------------");
    }
}
