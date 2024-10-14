package framework;

public class VerbAction {
    String verb;
    String action;
    public String getVerb() {
        return verb;
    }
    public void setVerb(String verb) {
        this.verb = verb;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public VerbAction(){

    }
    public VerbAction(String verb,String action){
        this.verb= verb;
        this.action = action;
    }
    
}
