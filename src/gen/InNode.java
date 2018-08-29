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
package gen;

import java.util.ArrayList;
import org.w3c.dom.Node;

import static gen.XSLTGeneratorConstants.INCOMING_LINK;
import static gen.XSLTGeneratorConstants.IN_NODE;
import static gen.XSLTGeneratorConstants.OUT_NODE;

/**
 * This class contains details of all the incoming links of a output node
 */
public class InNode {
    private final ArrayList<String> outNodes;

    public InNode(Node node){
        this.outNodes = new ArrayList<>();
        if(getAttribute(node,INCOMING_LINK)!=null){
            this.outNodes.add(getAttribute(node,INCOMING_LINK));
        }else {
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                Node childNode = node.getChildNodes().item(i);
                if (childNode.getAttributes() != null) {
                    if (childNode.getNodeName().equals(INCOMING_LINK) && childNode.getAttributes().getNamedItem(OUT_NODE)!=null) {
                        this.outNodes.add(childNode.getAttributes().getNamedItem(OUT_NODE).getTextContent());
                    }
                }
            }
        }
    }

    public ArrayList<String> getOutNodes() {
        return outNodes;
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
