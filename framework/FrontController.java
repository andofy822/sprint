package controller.front;
import framework.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import jakarta.servlet.http.Part;
import javax.sound.sampled.AudioFileFormat.Type;

import com.google.gson.Gson;

import java.net.URL;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpSession;

@MultipartConfig
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
        String requestURI = req.getRequestURI();
        boolean existMapping = false;
        try{
            String url1 = req.getHttpServletMapping().getPattern().replace("*","");
            String urlTaper = (req.getRequestURI().replace(req.getContextPath(),"")).replace(url1,"");
            urlTaper = (urlTaper.startsWith("/")) ? urlTaper : "/" + urlTaper;
            for (String key : dicoMapping.keySet()) {
                if (key.compareTo(urlTaper)==0) {
                    existMapping = true;
                    break;
                }
            }
            if (existMapping) {
                execute(dicoMapping, urlTaper,req,resp);
            }
            else{ 
                // throw new ServletException("URL n'existe pas");
                resp.sendError(404, "URL n'existe pas");
            }
        }
        catch(Exception e){             
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
        ArrayList<VerbAction> liste = new ArrayList<>(); 
        for (int j = 0; j < methodes.length; j++) {
            Get annotGet = methodes[j].getAnnotation(Get.class);
            if ( annotGet !=null ) {
                if (dicoMapping.containsKey(annotGet.url())) {
                    Mapping map = new Mapping();
                    map = dicoMapping.get(annotGet.url());
                    map.getVerbeMethodeDouble(methodes[j]);      
                }
                if(methodes[j].isAnnotationPresent(MethodGet.class)){
                    VerbAction verb = new VerbAction("GET",methodes[j]);
                    liste.add(verb);
                }
                if(methodes[j].isAnnotationPresent(MethodPost.class)){
                    VerbAction verb = new VerbAction("POST",methodes[j]);
                    liste.add(verb);
                }
                Mapping map = new Mapping(c.getName(),liste);
                dicoMapping.put(annotGet.url(), map);
            }
        }
    }
    public String getVerb(Method method)throws Exception{
        if (method.isAnnotationPresent(MethodGet.class)) {
            return "GET";
        }
        if (method.isAnnotationPresent(MethodPost.class)) {
            return "POST";
        }
        return null;
    }
    public boolean checkApi(Method m){
        Restapi annotGet = m.getAnnotation(Restapi.class);
        if (annotGet != null) {
            return true;
        }
        return false;
    }
    
    public void execute(HashMap<String,Mapping> dicoMapping,String urlTaper,HttpServletRequest request,HttpServletResponse response)throws Exception{
        try{
            PrintWriter out= response.getWriter();
            Class c = Class.forName(dicoMapping.get(urlTaper).getClassName());
            String verbe = request.getMethod();
            System.out.println(urlTaper); 
            String methode = dicoMapping.get(urlTaper).getMethodeName(verbe);
            Method m = getMethod(c,methode);
            boolean api = checkApi(m);
            Object[] ob = new Object[m.getParameterCount()];
            Enumeration<String> parameterNames = request.getParameterNames();
            System.out.println(parameterNames.hasMoreElements());
            Parameter [] parametre = m.getParameters();
            HashMap<String,Object> objet = new HashMap<String,Object>();
            HttpSession session = request.getSession();
            CustomSession cust = new CustomSession();
            cust.set(session);
            if (!parameterNames.hasMoreElements()) {
                for (int i =0; i < parametre.length  ; i++){
                    if(!parametre[i].isAnnotationPresent(Arg.class) && parametre[i].getType().getSimpleName().equalsIgnoreCase("CustomSession")){
                        ob[i] = cust;
                    }
                }
            } 
            else{
                    new Util().traitementSimple(parameterNames, parametre, ob, request, cust);
                    Enumeration<String> parameterNames1 = request.getParameterNames();
                    new Util().traitementObjet(parameterNames1, parametre, ob, request, cust, objet);
            }
            Object f = c.newInstance();
            checkSession(f,cust);
            Object o = m.invoke(f,ob);
            Gson json = new Gson();
            if (api) {
                response.setContentType("text/json");
                if (o == null) {
                    throw new Exception("type de retour impossible");
                }
                else if (o.getClass().getTypeName().equals(ModelAndView.class.getTypeName())){
                    ModelAndView view = (ModelAndView) o ;
                    out.println(json.toJson(view.getData()));                
                }
                else{
                    out.println(json.toJson(o));
                }
            }
            else{
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
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }
    
    
    public void checkSession(Object c,CustomSession session)throws Exception{
        try {
            Field[] fields = c.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
            if (fields[i].getType().getSimpleName().equalsIgnoreCase("CustomSession")) {
                fields[i].setAccessible(true);
                fields[i].set(c,session);
            }
        }
        } catch (Exception e) {
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
