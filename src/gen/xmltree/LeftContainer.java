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
import org.w3c.dom.Node;

import static gen.config.XSLTGeneratorConstants.LEFT_CONNECTORS;
import static gen.config.XSLTGeneratorConstants.IN_NODE;
import static gen.config.XSLTGeneratorConstants.OUT_NODE;
import static gen.config.XSLTGeneratorConstants.INCOMING_LINK;

public class LeftContainer {
    private final ArrayList<String> inNodes;

    public LeftContainer(Node node){
        this.inNodes = new ArrayList<>();
        populate(node);
    }

    public ArrayList<String> getInNodes() {
        return inNodes;
    }

    private void populate(Node node){
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node leftConnector = node.getChildNodes().item(i);
            if (leftConnector.getAttributes() != null && leftConnector.getNodeName().equals(LEFT_CONNECTORS)) {
                for(int j=0;j<leftConnector.getChildNodes().getLength();j++){
                    Node inNode = leftConnector.getChildNodes().item(j);
                    if(inNode.getAttributes()!=null && inNode.getNodeName().equals(IN_NODE)){
                        if(getAttribute(inNode,INCOMING_LINK)==null){
                            for(int k=0; k< inNode.getChildNodes().getLength(); k++){
                                Node incomingLink = inNode.getChildNodes().item(k);
                                if(incomingLink.getAttributes()!=null && incomingLink.getNodeName().equals(INCOMING_LINK)){
                                    if(getAttribute(incomingLink,OUT_NODE)!=null){
                                        this.inNodes.add(getAttribute(incomingLink,OUT_NODE));
                                    }
                                }
                            }
                        }else{
                            this.inNodes.add(getAttribute(inNode,INCOMING_LINK));
                        }
                    }
                }
            }
        }
    }

    private String getAttribute(Node node, String property){
        if(node.getAttributes()!=null && node.getNodeName().equals(IN_NODE)){
            try {
                return node.getAttributes().getNamedItem(property).getTextContent();
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }

}
