/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;


import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;
import static compiler.Util.classPool;

/**
 * Clase que representa un parámetro de entrada para la definición de un método
 * 
 * @author Carlos Arturo
 */
public class Parametro {

    Bytecode loadCode;
    CtClass type;
    String name;
    String descriptor;
    Variable variable;

    public Parametro(CtClass t, String n) {
        type = t;
        name = n;
        descriptor = Descriptor.of(type);
        String aux = Descriptor.of(type);
        CtClass baseType;
        if(descriptor.charAt(0) == '['){
            while(aux.charAt(0) == '['){
                aux = aux.substring(1);
            }
            try{
                baseType = Descriptor.toCtClass(aux, classPool);
            }catch(NotFoundException nf){
                baseType = CtClass.voidType;
            }
            variable = new Arreglo(name, baseType, Variable.LOCAL);
        }else{
            variable = new Variable(name, type, Variable.LOCAL);
        }


    }



    public Variable getLocalVariable(){
        return variable;
    }

    public void setLoadCode(Bytecode bc){
        loadCode = bc;
    }

    public Bytecode getLoadCode(){
        return loadCode;
    }

    public CtClass getType(){
        return type;
    }



    
}
