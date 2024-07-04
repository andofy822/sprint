package framework;

import java.util.Enumeration;
import java.util.HashMap;
import jakarta.servlet.http.HttpSession;

public class CustomSession {
    HttpSession session;
    public CustomSession(){
        
    }
    public HttpSession get() {
        return this.session;
    }
    public void set(HttpSession session) {
        this.session = session;
    }
    public void add(String key,Object value){
        this.session.setAttribute(key,value);
    }
    public void removeAll(){
        this.session.invalidate();
    }
    public void update(String key , Object value){
        this.session.setAttribute(key,value);
    }
    public Object search(String key1){
        return session.getAttribute(key1);       
    }
    public void remove(String key){
        this.session.removeAttribute(key);
    }

    

}
