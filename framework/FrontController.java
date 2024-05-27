package controller.front;
import framework.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import java.net.URL;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class FrontController extends HttpServlet {
    // Ho an'ny sprint 1,2
    ArrayList<String> listController;
    HashMap<String,Mapping> dicoMapping = new HashMap<String,Mapping>() ;//Clé ny URL ,d mapping ny value
    String baseUrl;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter out= resp.getWriter();
        // out.println("Voici les listes des controller");
        // for (int i = 0; i < listController.size() ; i++) {
        //     out.println(listController.get(i));
        // }
        boolean existMapping = false;
        String urlTaper = req.getRequestURL().toString().split(baseUrl)[1];
        out.println("L'url taper "+urlTaper);
        for (String key : dicoMapping.keySet()) {
            if (key.compareTo(urlTaper)==0) {
                existMapping = true;
                out.println("Key "+ key);
                break;
            }
        }
        if (existMapping) {
            out.println( "Methode correpondant : "+ this.dicoMapping.get(urlTaper).getMethodName());
            out.println( "Dans la classe : "+ this.dicoMapping.get(urlTaper).getClassName());
        }
        else{
            out.println("URL introuvable");
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            String name_package = getServletConfig().getInitParameter("packageController");
            this.baseUrl = getServletConfig().getInitParameter("baseUrl");
            this.listController =  this.getCtrlInPackage(name_package);
            System.out.println("Le size du HashMap "+dicoMapping.size());
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e);
        }
        
    }
    
    /* sprint1  */
    public ArrayList<String> getCtrlInPackage( String name_package) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = name_package.replace(".", "/");
        URL packageUrl = classLoader.getResource(path);
        ArrayList<String> liste = new ArrayList<>();
        if (packageUrl!=null) {
            File packageFile = new File(packageUrl.getFile());
            if (packageFile.exists() && packageFile.isDirectory()) {
                File[] files = packageFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().endsWith(".class")) {
                        String temp_name_classe = files[i].getName().substring(0, files[i].getName().lastIndexOf("."));
                        Class temp_class = Class.forName( name_package +"."+ temp_name_classe);
                        Annote annotation = (Annote) temp_class.getAnnotation(Annote.class);
                        if (annotation.valeur().equals("Controlleur")) {
                            liste.add(temp_name_classe);
                            setDicoMapping(temp_class); // Sprint 2
                        }
                        
                    }
                }
            }
            
        }
    
        return liste; 
        
    }
    /* sprint2 */
    public void setDicoMapping(Class c){
        Method[] methodes = c.getMethods();
        for (int j = 0; j < methodes.length; j++) {
            Get annotGet = methodes[j].getAnnotation(Get.class); 
            if ( annotGet !=null ) {
                dicoMapping.put(annotGet.url(), new Mapping( c.getSimpleName() , methodes[j].getName()));
            }
        }
    }
    
}
