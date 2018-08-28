import javax.xml.transform.*;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import net.sf.saxon.*;

public class XSLTExecute {
    /*public static void main(String[] args){
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(new File("/home/danushka/Downloads/output.xml"));
            Transformer transformer = factory.newTransformer(xslt);
            Source text = new StreamSource(new File("/home/danushka/Downloads/input1.xml"));
            transformer.transform(text, new StreamResult(new File("/home/danushka/Downloads/output1.xml")));
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }*/

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

        trans.transform(xmlSource, result);
        File outputFile = new File("/home/danushka/Downloads/output1.xml");
        FileWriter output = new FileWriter(outputFile);
        output.write(sw.toString());
        output.close();
    }
}
