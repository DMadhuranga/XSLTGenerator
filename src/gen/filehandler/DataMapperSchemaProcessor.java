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
package gen.filehandler;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;


/**
 * This class handles reading of the data mapper schema file.
 */
public class DataMapperSchemaProcessor {
    private final String filePath;
    private Document document;


    public DataMapperSchemaProcessor(String filePath) throws SAXException, IOException, ParserConfigurationException{
        this.filePath = filePath;
        readyFile();
    }

    private void readyFile() throws SAXException, IOException, ParserConfigurationException {
        File file = new File(this.filePath);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        this.document = documentBuilder.parse(file);
    }

    public Document getDocument(){
        return this.document;
    }
}
