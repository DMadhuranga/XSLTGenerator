package gen;

import org.w3c.dom.Node;

public abstract class XMLNode {

    protected String name;

    public String getAttribute(Node node, String property){
        if(node.getAttributes()!=null && (node.getNodeName().equals("node") || node.getNodeName().equals("treeNode"))){
            try {
                return node.getAttributes().getNamedItem(property).getTextContent();
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }
}
