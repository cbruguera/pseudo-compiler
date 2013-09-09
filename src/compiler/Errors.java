/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiler;

import java.util.ArrayList;
import parser.Token;

/**
 * Clase con métodos estáticos para manejar el listado de errores
 * @author Carlos Arturo
 */
public class Errors {
    protected static ArrayList<String> errors = new ArrayList<String>();
    protected static int count = 0;
    protected static int line = 0;
    private static boolean doublePrint = true;


    public static void add(String msg){
        errors.add(msg);
        count++;

        if(doublePrint){
            System.out.println(msg);
        }
    }

    public static void add(String msg, Token tok){
        errors.add(msg + ". Linea: " + tok.beginLine);
        count++;

        if(doublePrint){
            System.out.println(msg + ". Linea: " + tok.beginLine);
        }
    }

    public static void print(){
        for(String mensaje : errors){
            System.out.println(mensaje);
        }
    }
    
    public static int getCount(){
        return count;
    }

    public static void newLine(){
        line++;
    }


}
