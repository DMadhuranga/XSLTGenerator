import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class XSLTExeceute {
    public static void main(String[] args){

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(new File("/home/danushka/Downloads/output.xml"));
            Transformer transformer = factory.newTransformer(xslt);
            Source text = new StreamSource(new File("/home/danushka/Downloads/input1.xml"));
            transformer.transform(text, new StreamResult(new File("/home/danushka/Downloads/output1.xml")));
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        /*
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(new File("/home/danushka/Downloads/transform.xslt"));
            Transformer transformer = factory.newTransformer(xslt);
            Source text = new StreamSource(new File("/home/danushka/Downloads/input.xml"));
            transformer.transform(text, new StreamResult(new File("/home/danushka/Downloads/output.xml")));
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        */

    }
}
