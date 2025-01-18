package framework;

import java.util.HashMap;

public class ModelAndView {
    String url;
    HashMap<String,Object> data;
    HashMap <String,String> error;
    String errorUrl;
    public ModelAndView(String url){
        this.data = new HashMap<String,Object>();
        this.setUrl(url);
    }
    public void setUrl(String url){
        this.url = url;
    }
    public String getUrl(){
        return this.url;
    }
    public HashMap<String,Object> getData(){
      return this.data;  
    }
    public void AddOject(String nom , Object o){
        this.data.put(nom, o);
    }
    public HashMap <String,String> getError(){
        return this.error;
    } 
    public void setError(HashMap <String,String> error){
        this.error = error;
    }
    public String getErrorUrl() {
        return errorUrl;
    }
    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    } 
    
}
