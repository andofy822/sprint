package controller.front;
import java.io.File;
import java.io.PrintWriter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import framework.*;
/**
 * FrontController
 */
public class FrontController extends HttpServlet {
    private boolean test = false;
    List<String>valiny; 
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        try {
            processRequest(request, response);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response){
        try {
            processRequest(request, response);

        } catch (Exception e) {
            System.out.println(e);
        }
        
    }
    public void processRequest(HttpServletRequest request, HttpServletResponse response)throws Exception {
        
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();
        String chemin = context.getInitParameter("chemin");
        if (!test) {
            valiny = scan(chemin);
        }
        for (String string : valiny) {
            out.println(string);
        }
    }
    public List<String> scan(String chemin)throws Exception{
        List<String> liste = new ArrayList<String>();
        try 
        {
            String cheminRepertoire = chemin.replace('.','/');
            URL urPackage = Thread.currentThread().getContextClassLoader().getResource(cheminRepertoire);
            if (urPackage != null) {
                File directory = new File(urPackage.getFile());
                File[] fichiers = directory.listFiles();
                if (fichiers != null) {
                    for (File fichier : fichiers) {
                        if (fichier.isFile() && fichier.getName().endsWith(".class")) {
                            String nomClasse = fichier.getName().substring(0, fichier.getName().length() - 6);
                            String nomCompletClasse = chemin + "." + nomClasse;
                            Class class1 = Class.forName(nomCompletClasse);
                            if (class1.isAnnotationPresent(Annote.class)) {
                                Annote annotation = (Annote) class1.getAnnotation(Annote.class);
                                if (annotation.valeur().equals("Controlleur")) {
                                    liste.add(nomClasse +".class");
                                }
                            } 
                        }
                        else if(fichier.isDirectory()){
                            List<String> li =  scan(cheminRepertoire + "." + fichier.getName());
                            liste.addAll(li);
                        }
                    }
                }
            }
            test = true;

        }
        catch(Exception e){
            throw e;
        }
        return liste;
    }
    
}