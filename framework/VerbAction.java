package framework;

import java.lang.reflect.Method;

public class VerbAction {
    String verb;
    Method action;
    public String getVerb() {
        return verb;
    }
    public void setVerb(String verb) {
        this.verb = verb;
    }
    public Method getAction() {
        return action;
    }
    public void setAction(Method action) {
        this.action = action;
    }
    public VerbAction(){

    }
    public VerbAction(String verb,Method action){
        this.verb= verb;
        this.action = action;
    }
    
}
