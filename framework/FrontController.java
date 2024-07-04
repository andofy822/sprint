package controller.front;
import framework.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.sound.sampled.AudioFileFormat.Type;

import java.net.URL;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpSession;


public class FrontController extends HttpServlet {
    // Ho an'ny sprint 1,2
    ArrayList<String> listController;
    HashMap<String,Mapping> dicoMapping = new HashMap<String,Mapping>() ;//Clé ny URL ,d mapping ny value
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        PrintWriter out= resp.getWriter();
        String requestURI = req.getRequestURI();
        boolean existMapping = false;
        try{
            String url1 = req.getHttpServletMapping().getPattern().replace("*","");
            String urlTaper = req.getRequestURI().replace(req.getContextPath(),"").replace(url1,"");
            urlTaper = (urlTaper.startsWith("/")) ? urlTaper : "/" + urlTaper;
            out.println("L'url taper est"+ urlTaper);
            for (String key : dicoMapping.keySet()) {
                if (key.compareTo(urlTaper)==0) {
                    existMapping = true;
                    break;
                }
            }
            if (existMapping) {
                out.println( "Methode correpondant : "+ this.dicoMapping.get(urlTaper).getMethodName());
                out.println( "Dans la classe : "+ this.dicoMapping.get(urlTaper).getClassName());
                execute(dicoMapping, urlTaper,req,resp);
            }
            else{ 
                throw new ServletException("URL n'existe pas");
            }
        }
        catch(Exception e){
            out.println("Erreur: " + e.getMessage());
            e.printStackTrace(out);             
            throw new ServletException(e);
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ServletContext sc = getServletContext();
            String name_package = sc.getInitParameter("chemin");
            this.listController =  this.getCtrlInPackage(name_package);
            System.out.println("Le size du HashMap "+dicoMapping.size());
            if(dicoMapping.size() == 0){
                throw new ServletException("package vide");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
        
    }
    public ArrayList<String> getCtrlInPackage( String name_package ) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = name_package.replace(".", "/");
        URL packageUrl = classLoader.getResource(path);
        if (packageUrl == null) {
            throw new Exception("package not exist");
        }
        ArrayList<String> liste = new ArrayList<>();
        if (packageUrl!=null) {
            File packageFile = new File(packageUrl.getFile());
            if (packageFile.exists() && packageFile.isDirectory()) {
                File[] files = packageFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile() && files[i].getName().endsWith(".class")) {
                        String temp_name_classe = files[i].getName().substring(0, files[i].getName().lastIndexOf("."));
                        Class temp_class = Class.forName( name_package +"."+ temp_name_classe);
                        Annote annotation = (Annote) temp_class.getAnnotation(Annote.class);
                        if (annotation.valeur().equals("Controlleur")) {
                            liste.add(temp_name_classe);
                            setDicoMapping(temp_class); // Sprint 2
                        }
                        
                    }
                    else if(files[i].isDirectory()){
                        String newPackage = path + "." + files[i].getName();
                        getCtrlInPackage(newPackage);
                    }
                }
            }
            
            
        }
    
        return liste; 
        
    }
    public Method getMethod(Class c,String name){
        Method[] methodes = c.getMethods();
        for (int i = 0; i < methodes.length; i++) {
            if (methodes[i].getName().equals(name)) {
                return methodes[i];
            }
        }
        return null;
    }
    public void setDicoMapping(Class c)throws Exception{
        Method[] methodes = c.getMethods();
        for (int j = 0; j < methodes.length; j++) {
            Get annotGet = methodes[j].getAnnotation(Get.class); 
            if ( annotGet !=null ) {
                if (dicoMapping.containsKey(annotGet.url())) {
                    throw new Exception("url double");
                }
                dicoMapping.put(annotGet.url(), new Mapping( c.getName() , methodes[j].getName()));
            }
        }
    }
    public void execute(HashMap<String,Mapping> dicoMapping,String urlTaper,HttpServletRequest request,HttpServletResponse response)throws Exception{
        try{
            PrintWriter out= response.getWriter();
            Class c = Class.forName(dicoMapping.get(urlTaper).getClassName());
            String methode = dicoMapping.get(urlTaper).getMethodName();
            Method m = getMethod(c,methode);
            Object[] ob = new Object[m.getParameterCount()];
            Enumeration parameterNames = request.getParameterNames();
            Parameter [] parametre = m.getParameters();
            HashMap<String,Object> objet = new HashMap<String,Object>(); 
                while (parameterNames.hasMoreElements()) {
                    String paramName = (String)parameterNames.nextElement();
                    for (int i =0; i < parametre.length  ; i++) {
                        String [] liste_paramName = paramName.split("\\.");
                        if (parametre[i].isAnnotationPresent(Arg.class) && parametre[i].getType().isPrimitive() || parametre[i].isAnnotationPresent(Arg.class) && parametre[i].getType().getSimpleName().equalsIgnoreCase("String")) {
                            Arg arg = parametre[i].getAnnotation(Arg.class);
                            String message = arg.message();
                            if (message.equals(paramName)) {
                                ob[i] = request.getParameter(message);
                            }
                        } 
                        else if(parametre[i].isAnnotationPresent(Arg.class) && !parametre[i].getType().isPrimitive() || parametre[i].isAnnotationPresent(Arg.class) && !parametre[i].getType().getSimpleName().equalsIgnoreCase("String"))
                        {
                            Arg arg = parametre[i].getAnnotation(Arg.class);
                            String message = arg.message();
                            if (message.equals(liste_paramName[0])) {
                                if (objet.containsKey(liste_paramName[0])) {
                                    Class <?> e = objet.get(liste_paramName[0]).getClass().getDeclaredField(liste_paramName[1]).getType();
                                    Method met = objet.get(liste_paramName[0]).getClass().getDeclaredMethod("set" + liste_paramName[1].substring(0,1).toUpperCase()+liste_paramName[1].substring(1) , e);
                                    met.invoke(objet.get(liste_paramName[0]), stringToType(request.getParameter(paramName), e));
                                }
                                else{
                                    ajout(objet,liste_paramName[0], parametre[i]);
                                    System.out.println(liste_paramName[0]);
                                    Class<?> e = objet.get(liste_paramName[0]).getClass().getDeclaredField(liste_paramName[1]).getType();
                                    Method met = objet.get(liste_paramName[0]).getClass().getDeclaredMethod("set" + liste_paramName[1].substring(0,1).toUpperCase()+liste_paramName[1].substring(1) , e);
                                    met.invoke(objet.get(liste_paramName[0]), stringToType(request.getParameter(paramName), e));
                                    ob[i]=objet.get(liste_paramName[0]);
                                }

                            }
                        }
                        else if(!parametre[i].isAnnotationPresent(Arg.class)){
                            throw new Exception("ETU002365 tsy misy");
                        }
                    }
                }
                
            Object o = m.invoke(c.newInstance(),ob);
            if (o == null) {
                throw new Exception("type de retour impossible");
            }
            if (o.getClass().getTypeName().equals(String.class.getTypeName())) {
                out.println((String)o);
            }
            else if (o.getClass().getTypeName().equals(ModelAndView.class.getTypeName())){
                ModelAndView view = (ModelAndView) o ;
                RequestDispatcher disp = request.getRequestDispatcher(view.getUrl());
                for (String key : view.getData().keySet()) {
                    request.setAttribute(key,view.getData().getOrDefault(key, null));
                    
                }   
                disp.forward(request,response);             
            }
            else{
                throw new Exception("type de retour impossible");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }
    public void ajout(HashMap<String,Object> objet,String parameterNames,Parameter parametre)throws Exception{

             try{
                if (parametre.isAnnotationPresent(Arg.class)) {
                    Arg arg = parametre.getAnnotation(Arg.class);
                    String message = arg.message();
                    if (message.equals(parameterNames)) {
                        Object o = parametre.getType().getConstructor().newInstance(); 
                        objet.put(parameterNames, o);
                    }
                }

             }
             catch(Exception e){
                throw e;
             }                   
        }
        public  Object stringToType(String str, Class<?> clazz) {
            if (str == null) {
                if (clazz == Integer.class || clazz == int.class) {
                    return 0;        
                }
            }
            if (clazz == Integer.class || clazz == int.class) {
                return  Integer.parseInt(str);
            } else if (clazz == Double.class) {
                return  Double.parseDouble(str);
            } else if (clazz == Boolean.class) {
                return  Boolean.parseBoolean(str);
            }
                return  str; 
        }
    
    
}
