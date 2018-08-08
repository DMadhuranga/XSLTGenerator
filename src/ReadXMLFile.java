import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ReadXMLFile {
    private String fileName;
    private String filePath;
    private boolean fileReadable;
    File file;
    DocumentBuilderFactory documentBuilderFactory;
    DocumentBuilder documentBuilder;
    Document document;


    public ReadXMLFile(String filePath){
        this.filePath = filePath;
        readyFile();
    }

    public void readyFile(){
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

    public boolean isDocumentReadable(){
        return this.fileReadable;
    }

    public void closeFile(){
        if(isDocumentReadable()){
            //
            this.fileReadable = false;
        }
    }

    public Document getDocument(){
        if(isDocumentReadable()){
            return this.document;
        }
        return null;
    }
}
