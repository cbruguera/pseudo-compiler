/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase que maneja las operaciones del tipo enumerado
 * 
 * @author Carlos Arturo
 */
public class Enumerado {
    int count = 1;
    int ivalue;
    long lvalue;
    //String name;

    HashMap<String,Integer> table = new HashMap<String,Integer>();

    Enumerado(String...ids){
        for(String id : ids){
            table.put(id, Integer.valueOf(count));
            count++;
        }
    }

    Enumerado(){

    }

    public void setKey(String k, int v){
        table.put(k,Integer.valueOf(v));
    }

    public int getIvalue(){
        return ivalue;
    }

    public long getJvalue(){
        return lvalue;
    }

    public void setValue(String key) throws Exception{
        Integer valp;

        if((valp = table.get(key)) != null){
            ivalue = valp;
        }else{
            throw new Exception("Valor invalido \"" + key + "\" para variable de tipo enumerado");
        }
    }




}
