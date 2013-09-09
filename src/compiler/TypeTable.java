/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiler;

import java.util.HashMap;
import javassist.CtClass;

/**
 * Clase para el manejo y registro de las declaraciones de tipos y alias.
 * 
 * @author Carlos Arturo
 */
public class TypeTable {
    HashMap<String, CtClass> tabla;

    public TypeTable(){
        tabla = new HashMap<String, CtClass>();
    }

    public boolean exists(String t){
        return tabla.containsKey(t);
    }

    public boolean add(String name, CtClass type){  
        if(!exists(name)){
            tabla.put(name, type);
            return true;
        }else{
            return false;
        }
    }

    public CtClass get(String name){
        if(exists(name)){
            return tabla.get(name);
        }else{
            return null;
        }
    }
}
