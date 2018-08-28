package gen;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ReadXMLFile {
    private String fileName;
    private String filePath;
    private boolean fileReadable;
    private File file;
    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Document document;


    public ReadXMLFile(String filePath){
        this.filePath = filePath;
        readyFile();
    }

    private void readyFile(){
        this.file = new File(this.filePath);
        this.documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        try {
            this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
            this.document = documentBuilder.parse(file);
            this.fileReadable = true;
        }catch (Exception e){
            this.fileReadable = false;
        }
    }

    private boolean isDocumentReadable(){
        return this.fileReadable;
    }

    public Document getDocument(){
        if(isDocumentReadable()){
            return this.document;
        }
        return null;
    }
}
