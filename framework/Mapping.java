package framework;
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
    public String getMethodeName(String method){
        for (VerbAction verbeAction : listeMethode ) {
            if (verbeAction.getVerb().equals(method)) {
                return verbeAction.getAction();
            }
        }
        return null;
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