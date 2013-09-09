/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiler;

import javassist.CtClass;
import javassist.bytecode.Descriptor;

import static parser.Pseudo.getToken;

/**
 * Clase para el manejo de las definiciones y operaciones del tipo de datos 
 * "Subrango" en tiempo de ejecución.
 * 
 * @author Carlos Arturo
 */
public class Subrango{ // ??
    long limitInf = Integer.MIN_VALUE;
    long limitSup = Integer.MAX_VALUE;
    double dlimitInf = Double.MIN_VALUE;
    double dlimitSup = Double.MAX_VALUE;
    
    CtClass type;
    
    double dvalue;
    long lvalue;
    int ivalue;
    char cvalue;
    float fvalue;

    //getVal, setVal y todas las funciones del peo
    public Subrango(char i, char s){
        if(i < s){
            limitInf = i;
            limitSup = s;
        }else{ 
            limitSup = i;
            limitInf = s;
        }
    }

    public Subrango(float i, float s){
        if(i < s){
            dlimitInf = i;
            dlimitSup = s;
        }else{ 
            dlimitSup = i;
            dlimitInf = s;
        }
    }

    public Subrango(double i, double s){
        if(i < s){
            dlimitInf = i;
            dlimitSup = s;
        }else{ 
            dlimitSup = i;
            dlimitInf = s;
        }
    }

    public Subrango(int i, int s){
        if(i < s){
            limitInf = i;
            limitSup = s;
        }else{  
            limitSup = i;
            limitInf = s;
        }
    }

    public Subrango(long i, long s){
        if(i < s){
            limitInf = i;
            limitSup = s;
        }else{  
            limitSup = i;
            limitInf = s;
        }
    }

    public void setValue(int i) throws Exception{
        if(i >= limitInf && i <= limitSup){
            ivalue = i;
        }else{
            throw new Exception("valor " + i + " por fuera del rango permitido ["+ limitInf +" , "+ limitSup+"]");
            //RUNTIME ERROR!!!
        }
    }

    public void setValue(long l) throws Exception{
        if(l >= limitInf && l <= limitSup){
            lvalue = l;
        }else{
            throw new Exception("valor " + l + " por fuera del rango permitido ["+ limitInf +" , "+ limitSup+"]");
            //RUNTIME ERROR!!!
        }
    }

    public void setValue(float f) throws Exception{
        if(f >= limitInf && f <= limitSup){
            fvalue = f;
        }else{
            throw new Exception("valor " + f + " por fuera del rango permitido ["+ limitInf +" , "+ limitSup+"]");
            //RUNTIME ERROR!!!
        }
    }

    public void setValue(double d) throws Exception{
        if(d >= limitInf && d <= limitSup){
            dvalue = d;
        }else{
            throw new Exception("valor " + d + " por fuera del rango permitido ["+ limitInf +" , "+ limitSup+"]");
            //RUNTIME ERROR!!!
        }
    }

    public void setValue(char c) throws Exception{
        if(c >= limitInf && c <= limitSup){
            cvalue = c;
        }else{
            throw new Exception("valor " + c + " por fuera del rango permitido ["+ limitInf +" , "+ limitSup+"]");
            //RUNTIME ERROR!!!
        }
    }

    public void setValue(String in) throws Exception{
        String desc = Descriptor.of(type);
        try{
            if(desc.equals("I")){
                ivalue = Integer.parseInt(in);
            }else if(desc.equals("J")){
                lvalue = Long.parseLong(in);
            }else if(desc.equals("F")){
                fvalue = Float.parseFloat(in);
            }else if(desc.equals("D")){
                dvalue = Double.parseDouble(in);
            }else if(desc.equals("C")){
                cvalue = in.charAt(0);
            }else{
               Errors.add("Asignación inválida a variable de tipo Subrango", getToken(0));
            }
        } catch(Exception e){
            Errors.add("Asignación inválida a variable de tipo Subrango", getToken(0));
        }

    }

    public int getIvalue(){
        return ivalue;
    }

    public long getJvalue(){
        return lvalue;
    }

    public float getFvalue(){
        return fvalue;
    }

    public double getDvalue(){
        return dvalue;
    }

    public char getCvalue(){
        return cvalue;
    }
    


}
