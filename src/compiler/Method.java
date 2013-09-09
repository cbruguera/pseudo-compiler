/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import javassist.NotFoundException;
import java.util.ArrayList;
import javassist.CtClass;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;

import static compiler.Util.declaring;
import static compiler.Util.classPool;
import static parser.Pseudo.getToken;

/**
 * Clase que comprende la información de una función/acción para almacenar 
 * en un registro de la tabla de símbolos.
 * 
 * @author Carlos Arturo
 */
public class Method {

    protected String name;
    protected Bytecode callCode;
    protected CtClass returnType;
    protected int accessFlag;
    protected String descriptor;
    protected CtClass[] parameters; //lista de tipos de los parámetros del método
    
    //public static final int STATIC = 1;  
    //public static final int NONSTATIC = 2;

    public Method(String n, CtClass t) {
        name = n;
        parameters = new CtClass[0];
        returnType = t;
        descriptor = Descriptor.ofMethod(t, parameters);
    }

    public Method(String n, CtClass t, CtClass[] params) {
        name = n;
        parameters = params;
        returnType = t;
        descriptor = Descriptor.ofMethod(t, params);
        
        callCode = new Bytecode(declaring.getConstPool());
    }

    public Method(String n, CtClass t, int flags, CtClass[] params) {

        accessFlag = flags;
        name = n;
        parameters = params;
        returnType = t;
        descriptor = Descriptor.ofMethod(t, params);
        
        callCode = new Bytecode(declaring.getConstPool());
    }

    public Method(String n, String desc) {
        name = n;
        
        try{
            parameters = Descriptor.getParameterTypes(desc, classPool);
            returnType = Descriptor.getReturnType(desc, classPool);
        }catch(NotFoundException e){
            Errors.add(e.getMessage(), getToken(0));
        }
        
        descriptor = desc;

        callCode = new Bytecode(declaring.getConstPool());
    }

    public Method(String n) {
        name = n;
        parameters = new CtClass[0];
        returnType = CtClass.voidType;
        descriptor = Descriptor.ofMethod(returnType, parameters);

    }

    /**
     * Dada una lista de tipos (CtClass) verificar la correspondencia con los
     * parámetros de la función
     * 
     * @param args Arreglo de tipos
     * @return Retorna verdadero si los tipos coinciden, falso en caso contrario.
     */
    public boolean validateParameterTypes(CtClass[] args){
        if(args.length == parameters.length){

            for(int i = 0; i < args.length; i++){
                if(!Descriptor.of(args[i]).equals(Descriptor.of(parameters[i]))){
                    return false;
                }
            }
            return true;
            
        }else{
            return false;
            
        }
    }

    /**
     * Retorna una expresión con el código de la llamada del método actual, dada
     * una lista de sus argumentos.
     * 
     * @param args Argumentos de la llamada
     * @return  Expresión que representa la invocación del método
     */
    public Expresion getCallExpresion(Expresion[] args) {
        Expresion call = new Expresion();
        ArrayList<CtClass> types = new ArrayList<CtClass>();
        
        if((accessFlag & 8) == 0){  //Si no es estático
            call.getCode().addAload(0);
        }
        
        for (Expresion arg : args) {
            call.appendCode(arg.getCode());
            types.add(arg.getType());
        }

        if(!this.validateParameterTypes(types.toArray(new CtClass[0]))){
            Errors.add("Uno o mas argumentos de tipo invalido para la invocacion del procedimiento " 
                    + this.getName(), getToken(0));
        }

        if ((accessFlag & 8) > 0) { // si el método es estático
            call.getCode().addInvokestatic(declaring.getName(), name, descriptor);
        } else if ((accessFlag & 8) == 0) { 
            call.getCode().addInvokevirtual(declaring.getName(), name, descriptor);
        }
        
        call.setType(returnType);
        return call;
    }
    
    /**
     * Retorna una expresión con el código de la invocación de un método que se 
     * ejecuta a partir de un objeto.
     * 
     * @param obj Instancia desde donde se ejecuta el método
     * @param args Arreglo de argumentos de la llamada
     * @return Expresión que representa el código de la llamada
     */
    public Expresion getCallExpresion(Variable obj, Expresion[] args) {
        if(obj == null){
            return getCallExpresion(args);
        }
        
        Expresion call = new Expresion();
        ArrayList<CtClass> types = new ArrayList<CtClass>();

        call.appendCode(obj.getLoadCode());
        
        for (Expresion arg : args) {
            call.appendCode(arg.getCode());
            types.add(arg.getType());
        }

        if(!this.validateParameterTypes(types.toArray(new CtClass[0]))){
            Errors.add("Uno o mas argumentos de tipo invalido para la invocacion del procedimiento " 
                    + this.getName(), getToken(0));
        }

        call.getCode().addInvokevirtual(obj.getType().getName(), name, descriptor);
        
        call.setType(returnType);
        
        return call;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public CtClass getType() {
        return returnType;
    }

    public void setType(CtClass t) {
        returnType = t;
        descriptor = Descriptor.ofMethod(t, parameters);
    }

    public void setParameterTypes(CtClass[] types) {
        parameters = types;
        descriptor = Descriptor.ofMethod(returnType, types);
    }

    public CtClass[] getParameterTypes() {
        return parameters;
    }

}
