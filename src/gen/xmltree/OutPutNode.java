/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package gen.xmltree;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Node;

import static gen.config.XSLTGeneratorConstants.ATTRIBUTES_INITIALS;
import static gen.config.XSLTGeneratorConstants.PROPERTIES_LOWER_CASE;
import static gen.config.XSLTGeneratorConstants.KEY;
import static gen.config.XSLTGeneratorConstants.VALUE;
import static gen.config.XSLTGeneratorConstants.NODE;
import static gen.config.XSLTGeneratorConstants.IN_NODE;
import static gen.config.XSLTGeneratorConstants.NAME;
/**
 * This class represent a field in the output XML. Contains all the details of the field.
 */
public class OutPutNode extends XMLNode{
    private final String xPath;

    public InNode getInNode() {
        return inNode;
    }

    private InNode inNode;
    private final ArrayList<OutPutNode> childNodes;
    private final HashMap<String,String> properties;

    public OutPutNode(Node node, String parentXPath){
        this.name = getAttribute(node,NAME);
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
                    case PROPERTIES_LOWER_CASE:
                        this.properties.put(childNode.getAttributes().getNamedItem(KEY).getTextContent(), childNode.getAttributes().getNamedItem(VALUE).getTextContent());
                        break;
                    case NODE:
                        this.childNodes.add(new OutPutNode(childNode, this.xPath));
                        break;
                    case IN_NODE:
                        this.inNode = new InNode(childNode);
                        break;
                    default:
                        System.out.println(childNode.getNodeName());
                }
            }
        }
    }

    public ArrayList<OutPutNode> getChildNodes(){
        return this.childNodes;
    }

    public boolean isAnAttribute(){
        return this.name.startsWith(ATTRIBUTES_INITIALS);
    }

}
