package framework;
import java.lang.reflect.Method;
import java.util.ArrayList;
public class Mapping {
    String className;
    ArrayList<VerbAction> listeMethode; 
    public Mapping(String className, ArrayList<VerbAction> listeActions) {
        this.className = className;
        this.listeMethode = listeActions;
    }
    public Mapping() {
    
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public ArrayList<VerbAction> getListeMethode() {
        return listeMethode;
    }
    public String getMethode(Method method)throws Exception{
        for (VerbAction verbeAction : listeMethode ) {
            if (verbeAction.getAction().getName().equals(method.getName())) {
                return verbeAction.getAction().getName();
            }
        }
        throw new Exception("methode non correspondant");
    }
    public String getVerb(Method method)throws Exception{
        for (VerbAction verbeAction : listeMethode ) {
            if (verbeAction.getAction().getName().equals(method.getName())) {
                return verbeAction.getVerb();
            }
        }
        throw new Exception("methode non correspondant");
    }
    public Method getMethodeName(String verb)throws Exception{
        try {
            for (VerbAction verbeAction : listeMethode ) {
                if (verbeAction.getVerb().compareToIgnoreCase(verb)==0) {
                    return verbeAction.getAction();
                }
            } 
           throw new Exception("methode non corespondant par la methode" + verb);
        } catch (Exception e) {
            throw e;
        }

    }
    public String getVerbeMethodeDouble(Method method)throws Exception{
        try {
            String verbe = "";
            if (method.isAnnotationPresent(MethodGet.class)) {
                verbe = "GET";
            }
            else if (method.isAnnotationPresent(MethodPost.class)) {
                verbe = "POST";
                System.out.println("nandalo tato");
            }
            else{
                verbe="GET";
            }
            for (VerbAction verbeAction : listeMethode ) {
                if (verbeAction.getVerb().equals(verbe)) {
                    throw new Exception("url double");
                }
            }
            return verbe;
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
    }
    public void setMethodName(ArrayList<VerbAction> listeActions) {
        this.listeMethode = listeActions;
    }
    public boolean verification(VerbAction verb){
        for (VerbAction verbeAction : listeMethode ) {
            if (verbeAction.getVerb().equals(verb.getVerb())) {
                if (verbeAction.getAction().equals(verb.getAction())) {
                    return true;
                }
            }
        }
        return false;
    }
}