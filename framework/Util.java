package framework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@MultipartConfig
public class Util {
    public void traitementSimple(Enumeration<String> listeParameter,Parameter [] parametre,Object[] ob,HttpServletRequest request,CustomSession cust)throws Exception{
        try {
            while (listeParameter.hasMoreElements()) {
                String paramName = (String)listeParameter.nextElement();
                for (int i = 0; i < parametre.length; i++) {
                    if (parametre[i].isAnnotationPresent(Arg.class) && parametre[i].getType().isPrimitive() || parametre[i].isAnnotationPresent(Arg.class) && parametre[i].getType().getSimpleName().equalsIgnoreCase("String")) {
                            
                        Arg arg = parametre[i].getAnnotation(Arg.class);
                            String message = arg.message();
                            if (message.equals(paramName)) {
                                ob[i] = request.getParameter(message);
                            }
                    }
                    else if (!parametre[i].isAnnotationPresent(Arg.class) && parametre[i].getType().getSimpleName().equalsIgnoreCase("CustomSession")) {
                            ob[i] = cust;   
                    }
                    else if(parametre[i].isAnnotationPresent(Arg.class) && parametre[i].getType().getSimpleName().equalsIgnoreCase("Fichier")){
                            
                        Arg arg = parametre[i].getAnnotation(Arg.class);
                            String message = arg.message();
                            Part filePart = request.getPart(message);
                            ob[i]=this.convertion(filePart);
                        }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }
    public void traitementObjet(Enumeration<String> listeParameter,Parameter [] parametre,Object[] ob,HttpServletRequest request,CustomSession cust,HashMap<String,Object> objet)throws Exception{
        try {
                while (listeParameter.hasMoreElements()) {
                    String paramName = (String)listeParameter.nextElement();
                    String [] liste_paramName = paramName.split("\\.");
                    for (int j = 0; j < parametre.length; j++) {
                        Arg arg = parametre[j].getAnnotation(Arg.class);
                        String message = arg.message();
                        if(parametre[j].isAnnotationPresent(Arg.class) && !parametre[j].getType().isPrimitive() || parametre[j].isAnnotationPresent(Arg.class) && !parametre[j].getType().getSimpleName().equalsIgnoreCase("String"))
                            if (message.equals(liste_paramName[0])) {
                                if (objet.containsKey(liste_paramName[0])) {
                                    Class <?> e = objet.get(liste_paramName[0]).getClass().getDeclaredField(liste_paramName[1]).getType();
                                    Method met = objet.get(liste_paramName[0]).getClass().getDeclaredMethod("set" + liste_paramName[1].substring(0,1).toUpperCase()+liste_paramName[1].substring(1) , e);
                                    met.invoke(objet.get(liste_paramName[0]), stringToType(request.getParameter(paramName), e));
                                    ob[j]=objet.get(liste_paramName[0]);
                                }
                                else{
                                    ajout(objet,liste_paramName[0], parametre[j]);
                                    Class<?> e = objet.get(liste_paramName[0]).getClass().getDeclaredField(liste_paramName[1]).getType();
                                    Method met = objet.get(liste_paramName[0]).getClass().getDeclaredMethod("set" + liste_paramName[1].substring(0,1).toUpperCase()+liste_paramName[1].substring(1) , e);
                                    met.invoke(objet.get(liste_paramName[0]), stringToType(request.getParameter(paramName), e));
                                    ob[j]=objet.get(liste_paramName[0]);
                                }
                        }
                    }
                }
                for (String key : objet.keySet()) {
                    Object obj = objet.get(key);
                    System.out.println(key);
                    System.out.println(obj.getClass().getSimpleName());
                    this.validation(obj);
                }
                String contentType = request.getContentType();
                if (contentType != null && contentType.toLowerCase().startsWith("multipart/form-data")) {
                    this.traitementFichier(objet, ob, parametre, request);
                }
            }
            catch (Exception e) {
            throw e;
        }
    }
    public void traitementFichier(HashMap<String,Object> objet,Object[] ob,Parameter[] parameter,HttpServletRequest request)throws Exception{
        try {
            for (int i = 0; i < parameter.length; i++) {
                String para = parameter[i].getAnnotation(Arg.class).message();
                String[]liste = this.getFieldTypeFichier(objet.get(para).getClass());
                Collection <Part> filepart = request.getParts();
                for (Part part : filepart) {
                    String name = part.getName();
                    String [] name1 = name.split("\\.");
                    if(Arrays.asList(liste).contains(name1[1])){
                        if (name1.length > 1 && name1[0].equals(para) && request.getPart(name)!= null ){
                            if (objet.get(name1[0]) != null) {
                                Class<?> e = objet.get(name1[0]).getClass().getDeclaredField(name1[1]).getType();
                                Method met = objet.get(name1[0]).getClass().getDeclaredMethod("set" + name1[1].substring(0,1).toUpperCase()+name1[1].substring(1) , e);
                                met.invoke(objet.get(para),this.convertion(request.getPart(name)));
                                ob[i] = objet.get(name1[0]);
                            }
                            else{
                                ajout(objet,name1[0], parameter[i]);
                                Class<?> e = objet.get(name1[0]).getClass().getDeclaredField(name1[1]).getType();
                                Method met = objet.get(name1[0]).getClass().getDeclaredMethod("set" + name1[1].substring(0,1).toUpperCase()+name1[1].substring(1) , e);
                                met.invoke(objet.get(para), this.convertion(request.getPart(name)));
                                ob [i] = objet.get(name1[0]);
                            }        
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }
    public String [] getFieldTypeFichier(Class c)throws Exception{
        try {

            Field [] fields = c.getDeclaredFields();
            String [] liste = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getType().equals(Fichier.class)) {
                    liste[i]=fields[i].getName();
                }
            }
            return liste;
        } catch (Exception e) {
            throw e;
        }
    }
    public Fichier convertion(Part filePart) throws Exception {
        if (filePart == null) {
            throw new Exception("Le fichier est manquant ou inaccessible.");
        }
        
        try (InputStream fileContentStream = filePart.getInputStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            
            Fichier fichier = new Fichier();
            fichier.setName(filePart.getSubmittedFileName());
            
            byte[] tempBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileContentStream.read(tempBuffer)) != -1) {
                buffer.write(tempBuffer, 0, bytesRead);
            }
            
            byte[] fileContent = buffer.toByteArray();
            if (fileContent.length == 0) {
                throw new Exception("Le fichier est vide.");
            }
            
            fichier.setFileContent(fileContent);

            return fichier;
            
        } catch (IOException e) {
            throw new Exception("Erreur lors de la lecture du contenu du fichier", e);
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
   public void validation(Object object)throws Exception{
        StringBuffer Exception = new StringBuffer();
        try {
            Field [] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(NotNull.class)) {
                    Object ob = field.get(object);
                    if (field.getType().getSimpleName().equalsIgnoreCase("String") && ob != null && ((String) ob).isEmpty()) {
                        Exception.append(field.getName()+"null");
                    }
                    if (ob == null) {
                        Exception.append(field.getName()+"null");
                    }
                }
                if (field.isAnnotationPresent(Range.class)) {
                    Object ob = field.get(object);
                    Range range = field.getAnnotation(Range.class);
                    System.out.println(ob.getClass().getSimpleName());
                    if (ob.getClass().getSimpleName().compareToIgnoreCase("Integer")!=0 && ob.getClass().getSimpleName().compareToIgnoreCase("Double")!=0) {
                        Exception.append(field.getName()+"doit etre de type int ou double");
                    }
                    if (Double.parseDouble(ob.toString()) < range.min() || Double.parseDouble(ob.toString()) > range.max()) {
                        Exception.append(field.getName()+"doit etre entre " +range.min() +"et"+range.max());
                    }
                }
            }
            System.out.println(Exception.toString());
            if (Exception.length() > 0) {
                throw new Exception(Exception.toString());    
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
   }
   public void sendErrorNotFound(HttpServletRequest request,HttpServletResponse response,String exception)throws Exception{
    response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Sets 404 status code
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<html><body><h1>Erreur 404</h1><p>"+exception+"</p></body></html>");
    out.close();
   }   

}
