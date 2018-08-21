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

    public OutNode getOutNode() {
        return outNode;
    }

    public void setOutNode(OutNode outNode) {
        this.outNode = outNode;
    }

    public InNode getInNode() {
        return inNode;
    }

    public void setInNode(InNode inNode) {
        this.inNode = inNode;
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

    public String getxPath() {
        return xPath;
    }

    public void printNode(){
        System.out.println("Name : "+this.name);
        System.out.println("SchemaDataType : "+this.schemaDataType);
        System.out.println("Level : "+this.level);
        System.out.println("Properties : ");
        for(String key:this.properties.keySet()){
            System.out.println(key+" : "+this.properties.get(key));
        }
        System.out.println("----- End of properties");
        System.out.println("gen.OutNode : ");
        if(this.outNode!=null){
            this.outNode.printOutNode();
        }
        System.out.println("----- End of outnode");
        System.out.println("gen.InNode : ");
        if(this.inNode!=null){
            this.inNode.printInNode();
        }
        System.out.println("----- End of innode");
        for(OutPutNode node: this.childNodes){
            node.printNode();
        }
    }

    public String getProperty(String property){
        if(this.properties.containsKey(property)){
            return this.properties.get(property);
        }
        return null;
    }

    public String getSchemaDataType() {
        return schemaDataType;
    }

    public void setSchemaDataType(String schemaDataType) {
        this.schemaDataType = schemaDataType;
    }

    public int getLevel() {
        return Integer.parseInt(this.level);
    }

    public String getName() {
        return name;
    }

    public void setChildNodes(ArrayList<OutPutNode> chilsNodes) {
        this.childNodes = chilsNodes;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
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
