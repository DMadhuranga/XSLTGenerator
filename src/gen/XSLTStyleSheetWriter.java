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

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * This class handles writing of the generated XSLT stylesheet.
 */
class XSLTStyleSheetWriter {
    private Document document;
    private final String filePath;
    private static DocumentBuilderFactory docFactory;

    public XSLTStyleSheetWriter(String filePath) throws ParserConfigurationException {
        this.filePath = filePath;
        if(docFactory==null){
            docFactory = DocumentBuilderFactory.newInstance();
        }
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        document = docBuilder.newDocument();
    }

    public void saveFile() throws TransformerException {
        TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(this.document);
        StreamResult result = new StreamResult(new File(this.filePath));
        transformer.transform(source, result);
    }

    public Document getDocument(){
        return this.document;
    }
}
