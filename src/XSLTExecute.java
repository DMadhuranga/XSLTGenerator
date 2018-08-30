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
import javax.xml.transform.*;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import net.sf.saxon.*;

class XSLTExecute {

    public static void main(String args[]) throws Exception {
        File xsltFile = new File("/home/danushka/Downloads/output.xml");
        File inputXml = new File("/home/danushka/Downloads/input1.xml");

        Source xmlSource = new javax.xml.transform.stream.StreamSource(inputXml);
        Source xsltSource = new javax.xml.transform.stream.StreamSource(xsltFile);
        StringWriter sw = new StringWriter();

        Result result = new javax.xml.transform.stream.StreamResult(sw);

        TransformerFactory transFact = new TransformerFactoryImpl();
        Transformer trans = transFact.newTransformer(xsltSource);

        for (int i = 3; i < args.length; i++) {
            String[] parts = args[i].split("=");
            trans.setParameter(parts[0], parts[1]);
        }
        trans.setParameter("PROPERTIES","");

        trans.transform(xmlSource, result);
        File outputFile = new File("/home/danushka/Downloads/output1.xml");
        FileWriter output = new FileWriter(outputFile);
        output.write(sw.toString());
        output.close();
    }
}
