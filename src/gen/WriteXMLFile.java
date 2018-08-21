package gen;

import org.w3c.dom.Node;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class WriteXMLFile {
    private DocumentBuilderFactory docFactory;
    private DocumentBuilder docBuilder;
    private Document document;
    private String filePath;
    private boolean writeable;

    public WriteXMLFile(String filePath){
        this.filePath = filePath;
        docFactory = DocumentBuilderFactory.newInstance();
        try {
            docBuilder = docFactory.newDocumentBuilder();
            document = (Document) docBuilder.newDocument();
            this.writeable = true;
        }catch (Exception e){
            this.writeable = false;
        }
    }

    public boolean isWriteable() {
        return writeable;
    }

    public boolean saveFile(){
        try {
            TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(this.document);
            StreamResult result = new StreamResult(new File(this.filePath));
            transformer.transform(source, result);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public Document getDocument(){
        return this.document;
    }
}
