/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import java.util.Collection;
import java.util.HashMap;
import javassist.bytecode.Descriptor;


/**
 * Clase que representa un ámbito de localidad en un punto dado del código fuente.
 * Contiene una tabla de símbolos de variables, y otra para los procedimientos.
 * 
 * @author Carlos Arturo
 */
public class Scope {
    //Tabla de variables locales definidas, con tipo e indice en el arreglo de variables locales

    Scope containingScope = null;
    HashMap<String, Variable> symbolTable;
    HashMap<String, Method> methodTable;
    int indexCount = 0;
    int maxLocals = 1; 

    /**
     * Crea un ámbito dentro del ámbito actual y lo retorna
     * @return Ámbito "hijo" del actual
     */
    public Scope createChild() {
        Scope child = new Scope(indexCount);
        //maxLocals = indexCount;
        child.setContainingScope(this);
        child.setIndexCount(indexCount);

        return child;
    }

    /**
     * Se usa para terminar la existencia de un ámbito "hijo" 
     * al momento en que éste termina.
     *
     * @return Ámbito padre del actual
     */
    public Scope destroyChild() {
        if (containingScope != null && maxLocals > containingScope.getMaxLocals()) {
            containingScope.setMaxLocals(maxLocals);   
        }
        
        return containingScope;
    }

    public Scope(Scope father) {
        containingScope = father;

        symbolTable = new HashMap<String, Variable>();
        methodTable = new HashMap<String, Method>();
    }

    public Scope() {
        containingScope = null;

        symbolTable = new HashMap<String, Variable>();
        methodTable = new HashMap<String, Method>();
    }

    public Scope(int ind) {
        indexCount = ind;
        maxLocals = indexCount;
        containingScope = null;

        symbolTable = new HashMap<String, Variable>();
        methodTable = new HashMap<String, Method>();
    }

    public int getIndexCount() {
        return indexCount;
    }

    public void setIndexCount(int i) {
        indexCount = i;
    }

    /**
     * Incrementa el contador de variables dentro del ámbito
     * 
     * @param i Cantidad a sumar al índice de variables
     */
    public void increaseIndexCount(int i) {
        indexCount = indexCount + i;
        maxLocals = maxLocals + i;
    }
    
    /**
     * Determina si una variable dada por su nombre existe en el ámbito actual
     * 
     * @param t Nombre de la variable a buscar
     * @return Verdadero o Falso dependiendo de su existencia
     */
    public boolean variableExists(String t) {
        if (symbolTable.containsKey(t)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determina si un procedimiento dado por su nombre y descriptor existen
     * en el ámbito actual
     * 
     * @param nombre Nombre de la función/acción
     * @param desc Descriptor del método
     * @return Verdadero o falso dependiendo de su existencia
     */
    public boolean methodExists(String nombre, String desc) {
        if (methodTable.containsKey(nombre + desc)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Agrega una variable a la tabla de símbolos del ámbito actual
     * 
     * @param var Variable a agregar
     * @return Retorna verdadero si se pudo insertar exitosamente la variable, y
     * falso en caso contrario (ya está declarada en el ámbito actual)
     */
    public boolean add(Variable var) {


        if (!variableExists(var.getName())) {

            if (var.getForm() == Variable.LOCAL) {

                var.makeCodes(indexCount);
                increaseIndexCount(1);
                
                if (var.getDescriptor().equals("J") || var.getDescriptor().equals("D")) {
                    increaseIndexCount(1);
                }
            } else if (var.getForm() == Variable.FIELD) {
                var.makeCodes(-1);
            } else if (var.getForm() == Variable.PARAMETER) { 
                var.makeCodes(indexCount);
                increaseIndexCount(1);
                
                if (var.getDescriptor().equals("J") || var.getDescriptor().equals("D")) {
                    increaseIndexCount(1);
                }
            } else if (var.getForm() == Variable.STATIC) {
                var.makeCodes(-1);
            }
            
            var.setScope(this);
            symbolTable.put(var.getName(), var);
            
            return true;
        } else {
            return false;
        }
    }

    /**
     * Invoca a this.addVariable(Variable v) para un arreglo de variables.
     * 
     * @param varArr Arreglo de variables
     * @return Falso si alguna variable ya se encuentra declarada, Verdadero
     * en caso contrario
     */
    public boolean addVariables(Variable[] varArr) {
        boolean todoBien = true;

        for (int i = 0; i < varArr.length; i++) {
            todoBien = todoBien && add(varArr[i]);
        }

        return todoBien;
    }

    /**
     * Inserta un arreglo de variables que corresponden a parámetros
     * de una función.
     * 
     * @param varArr Arreglo de parámetros
     * @return Falso si algún nombre ya está declarado en el ámbito, Verdadero 
     * en caso contrario
     */
    public boolean addVariables(Parametro[] varArr) {
        boolean todoBien = true;

        for (int i = 0; i < varArr.length; i++) {
            todoBien = todoBien && add(varArr[i].getLocalVariable());
        }

        return todoBien;
    }

    /**
     * Agrega un registro en la tabla para un procedimiento (objeto Method)
     *
     * @param m Método a agregar
     * @return Verdadero o Falso dependiendo de la inserción exitosa en la tabla
     */
    public boolean add(Method m) {
        if (methodExists(m.getName(), Descriptor.ofParameters(m.getParameterTypes()))) {
            return false;
        } else {
            methodTable.put(m.getName() + Descriptor.ofParameters(m.getParameterTypes()), m);
            return true;
        }
    }

    /**
     * Obtiene una variable del ámbito actual dada por su nombre.
     * 
     * @param name Nombre de la variable a buscar
     * @return Un objeto de tipo Variable, retorna null en caso de no existir
     */
    public Variable getVariable(String name) {
        if (variableExists(name)) {
            return symbolTable.get(name);
        } else if (containingScope != null) {
            return containingScope.getVariable(name);
        } else {
            return null;
        }
    }

    /**
     * Obtiene un objeto de tipo Method del ámbito actual
     * dado por su nombre y descriptor.
     * 
     * @param name Nombre del método
     * @param desc Descriptor
     * @return objeto de tipo Method. Null en caso de no existir (en el ámbito)
     */
    public Method getMethod(String name, String desc) {
        if (methodExists(name, desc)) {
            return methodTable.get(name + desc);
        } else if (containingScope != null) {
            return containingScope.getMethod(name, desc);
        } else {
            return null;
        }
    }

    public HashMap<String, Variable> getSymbolTable() {
        return symbolTable;
    }

    public void increaseMaxLocals(int inc) {
        this.maxLocals = this.maxLocals + inc;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public void setMaxLocals(int max) {
        maxLocals = max;
    }

    public Scope getContainingScope() {
        return containingScope;
    }

    public void setContainingScope(Scope f) {
        containingScope = f;
    }

    /**
     * Imprime los nombres y descriptores de todos los símbolos de variables
     * definidos en el ámbito actual. Se usa para depurar.
     */
    public void printVariables() {
        Collection<Variable> arr = symbolTable.values();

        System.out.println("");
        System.out.println("Symbol Table:");
        System.out.println("-------------");
        for (Variable var : arr) {
            System.out.println("\"" + var.getName() + "\"" + " (" + Descriptor.of(var.getType()) + ")");
        }

    }

    /**
     * Imprime los nombres y descriptores de todos los símbolos de variables
     * definidos en el ámbito actual y todos los ámbitos padres del actual.
     * Se usa para depurar.
     */
    public void printAllVariables() {
        Collection<Variable> arr = symbolTable.values();

        printVariables();
        if (containingScope != null) {
            System.out.println("<--- containing scope:");
            containingScope.printAllVariables();
        }
    }

    /**
     * Imprime los métodos declarados en el ámbito actual. Se usa para depurar.
     */
    public void printMethods() {
        Collection<Method> arr = methodTable.values();

        System.out.println("");
        System.out.println("Method Table:");
        System.out.println("-------------");
        for (Method met : arr) {
            System.out.println("\"" + met.getName() + "\"" + met.getDescriptor());
        }
    }
}
