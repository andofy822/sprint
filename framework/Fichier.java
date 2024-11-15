package framework;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
public class Fichier {
    String name;
    byte[] fileContent;

    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }
    public byte [] getFileContent(){
        return this.fileContent;
    }
    public void setFileContent(byte [] file){
        this.fileContent = file;
    }
    public Fichier(){

    }
    public Fichier(String name,byte [] file){
        this.setName(name);
        this.setFileContent(file);
    } 
}
