/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import java.util.ArrayList;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;

import static compiler.Util.declaring;
import static compiler.Util.append;
import static parser.Pseudo.getToken;
import static compiler.Util.classPool;
import static compiler.Util.printBytecode;
import static compiler.Util.INTEGER_TYPE;
import static compiler.Util.currentScope;
import static compiler.Util.getStoreStringCode;
import static compiler.Util.compatibleTypes;

/**
 * Clase que representa una variable para registrar en la tabla de símbolos.
 * Contiene información de nombre, tipo y bytecodes para carga y almacenamiento.
 * 
 * @author Carlos Arturo
 */
public class Variable {

    protected Scope scope;
    protected Bytecode storeCode;
    protected Bytecode loadCode;
    protected Bytecode preStoreCode;
    protected CtClass type;
    protected CtClass baseType;
    protected String declaringClassName;
    protected String name;
    protected String descriptor;
    protected int form;
    protected int localIndex;
    protected int size = 1;
    protected boolean constant = false;
    protected boolean functionCall = false;
    protected boolean objectField = false;
    public static final int LOCAL = 1;
    public static final int FIELD = 2;
    public static final int PARAMETER = 3;
    public static final int STATIC = 4;

   
     
    public Variable(CtClass t, int varForm) {
        form = varForm;
        type = t;
        baseType = t;
        descriptor = Descriptor.of(t);

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());

        declaringClassName = declaring.getName();

        calcSize();
    }

    public Variable(String nam, CtClass tp, int f) {
        type = tp;
        baseType = tp;
        name = nam;
        form = f;
        descriptor = Descriptor.of(type);

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());

        declaringClassName = declaring.getName();

        calcSize();
    }

    public Variable(String nam, CtClass tp) {
        type = tp;
        baseType = tp;
        name = nam;
        descriptor = Descriptor.of(type);

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());

        declaringClassName = declaring.getName();

        calcSize();
    }

    public Variable(String nam) {
        name = nam;
        size = 1;

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());

        declaringClassName = declaring.getName();
    }

    public Variable() {
        size = 1;
        form = LOCAL;        

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());

        declaringClassName = declaring.getName();
    }

    public boolean isFunctionCall() {
        return functionCall;
    }

    public void setFunctionCall(boolean val) {
        functionCall = val;
    }

    public Expresion getSizeExpresion() {
        return null;
    }

    public void setScope(Scope sc) {
        scope = sc;
    }

    public Scope getScope() {
        return scope;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getBaseDescriptor() {
        return Descriptor.of(baseType);
    }

    public String getName() {
        return name;
    }

    public CtClass getType() {
        return type;
    }

    public CtClass getBaseType() {
        return baseType;
    }

    public void setType(CtClass t) {
        type = t;
        descriptor = Descriptor.of(type);

        baseType = t;   //???
        calcSize();
    }

    /**
     * Calcula el tamaño de bytes que la variable ocupa en la JVM.
     */
    protected void calcSize() {
        if (descriptor.equals("J") || descriptor.equals("D")) {
            //Si el tipo es Long o Double, ocupa 2 bytes
            size = 2;
        } else {
            size = 1;
        }
    }

    public void setBaseType(CtClass t) {
        baseType = t;
    }

    public Bytecode getLoadCode() {
        if (form == FIELD && declaring.getName().equals(declaringClassName) && !objectField) {
                        
            Bytecode thisCode = new Bytecode(declaring.getConstPool());
            thisCode.addAload(0);
            return append(thisCode, loadCode);
        } else {
            return loadCode;
        }
    }

    public Bytecode getStoreCode() {
        return storeCode;
    }

    public Bytecode getPreStoreCode() {
        if (form == FIELD && declaring.getName().equals(declaringClassName) && !objectField) {
            
            Bytecode thisCode = new Bytecode(declaring.getConstPool());
            thisCode.addAload(0);
            return append(thisCode, preStoreCode);
        } else {
            return preStoreCode;
        }
    }

    public void setPreStoreCode(Bytecode bc) {
        preStoreCode = new Bytecode(declaring.getConstPool());
        byte[] bytes = bc.get();

        for (int i = 0; i < bytes.length; i++) {
            preStoreCode.add(bytes[i]);
        }
    }

    public void setLoadCode(Bytecode bc) {
        loadCode = bc;
    }

    public void setStoreCode(Bytecode bc) {
        storeCode = bc;
    }

    /**
     * Inserta un bytecode dado al final del código de carga
     * 
     * @param bc Bytecode a insertar
     */
    public void appendLoadCode(Bytecode bc) {

        byte[] bytes = bc.get();

        for (int i = 0; i < bytes.length; i++) {
            loadCode.add(bytes[i]);
        }
    }

    /**
     * Inserta un bytecode dado al comienzo del código de carga
     * 
     * @param bc Bytecode a insertar
     */
    public void preAppendLoadCode(Bytecode bc) {
        byte[] newBytes = bc.get();
        byte[] thisBytes = this.loadCode.get();

        loadCode = new Bytecode(declaring.getConstPool());

        for (int i = 0; i < newBytes.length; i++) {
            loadCode.add(newBytes[i]);
        }

        for (int i = 0; i < thisBytes.length; i++) {
            loadCode.add(thisBytes[i]);
        }
    }

    /**
     * Inserta un bytecode dado al fnial del código de almacenamiento
     * 
     * @param bc Bytecode a insertar
     */
    public void appendStoreCode(Bytecode bc) {

        byte[] bytes = bc.get();

        for (int i = 0; i < bytes.length; i++) {
            storeCode.add(bytes[i]);
        }
    }

    /**
     * Inserta un bytecode dado al comienzo del código de almacenamiento
     * 
     * @param bc Bytecode a insertar
     */
    public void preAppendStoreCode(Bytecode bc) {
        byte[] newBytes = bc.get();
        byte[] thisBytes = this.storeCode.get();

        storeCode = new Bytecode(declaring.getConstPool());

        for (int i = 0; i < newBytes.length; i++) {
            storeCode.add(newBytes[i]);
        }

        for (int i = 0; i < thisBytes.length; i++) {
            storeCode.add(thisBytes[i]);
        }
    }

    /**
     * Inserta un bytecode dado al final del código de precarga (utilizado para
     * acceder elementos de un arreglo y objetos)
     * 
     * @param bc Bytecode a insertar
     */
    public void appendPreStoreCode(Bytecode bc) {

        byte[] bytes = bc.get();

        for (int i = 0; i < bytes.length; i++) {
            preStoreCode.add(bytes[i]);
        }
    }

    /**
     * Inserta un bytecode dado al comienzo del código de precarga (utilizado 
     * para acceder elementos de un arreglo y objetos)
     * 
     * @param bc Bytecode a insertar
     */
    public void preAppendPreStoreCode(Bytecode bc) { // :-S
        byte[] newBytes = bc.get();
        byte[] thisBytes = this.preStoreCode.get();

        preStoreCode = new Bytecode(declaring.getConstPool());

        for (int i = 0; i < newBytes.length; i++) {
            preStoreCode.add(newBytes[i]);
        }

        for (int i = 0; i < thisBytes.length; i++) {
            preStoreCode.add(thisBytes[i]);
        }
    }

    public int getForm() {
        return form;
    }

    public void setForm(int i) {
        form = i;
    }

    public boolean isObject() {
        if (descriptor.charAt(0) == 'L') {
            return true;
        } else {
            return false;
        }
    }

    public boolean isConstant() {
        return constant;
    }

    public void setConstant(boolean v) {
        constant = v;
    }

    public boolean isArray() {
        return (descriptor.charAt(0) == '[');
    }

    /**
     * Si la variable es un objeto, retorna el tipo de un campo dado por su nombre
     * 
     * @param fname Nombre del campo
     * @return un Ctclass correspondiente al tipo de campo, retorna null
     * si el objeto no tiene dicho campo
     */
    public CtClass getFieldType(String fname) {
        CtClass ret = null;
        CtField field;
        if (this.isObject()) {
            try {
                field = type.getField(fname);   // Esto revisa la superclase???
                ret = field.getType();
            } catch (NotFoundException nf) {
                Errors.add("No se encuentra el campo " + fname + " en la clase " 
                        + type.getName(), getToken(0));
                return null;
            }
        } else {
            //System.out.println(getName() + ".desc = " + descriptor);
            Errors.add("La variable " + getName() + " no es un objeto", getToken(0));
        }

        return ret;
    }

    /**
     * Si la variable actual es un objeto, obtiene el tipo de retorno de un 
     * método dado por su nombre y tipos de parámetros
     * 
     * @param mname Nombre del método
     * @param parameters Arreglo de tipos de parámetros
     * @return un tipo "CtClass" correspondiente al tipo del método, retorna
     * null si el método no se encuentra
     */
    public Method getMethod(String mname, CtClass[] parameters) {
        CtMethod[] methods;
        
        if (this.isObject()) {
            try {
                methods = type.getMethods();
                //System.out.println("Buscando metodo " + mname + " en la clase " + type.getName());
                for (CtMethod m : methods) {
                  //  System.out.println(m.getName() + m.getSignature());
                    
                    if (m.getName().equals(mname)) {
                        CtClass[] ptypes = m.getParameterTypes();
                        //System.out.println("comparando: " );
                        if (Descriptor.ofParameters(ptypes).equals(Descriptor.ofParameters(parameters))) {
                    //        System.out.println("Se encontro el metodo " + m.getName() + m.getSignature());
                      //      System.out.println("");
                            
                            return new Method(m.getName(), m.getReturnType(), AccessFlag.of(m.getModifiers()), parameters);
                            
                        }
                    }
                }
                //System.out.println("");
            } catch (NotFoundException nf) {
                Errors.add("No se encuentra el metodo " + mname + " en la clase" 
                        + type.getName(), getToken(0));
                
                return null;
            }
            
        } else {
            Errors.add("La variable " + getName() + " no es un objeto", getToken(0));
        }

        return null;
    }

    public int getLocalIndex() {
        return localIndex;
    }

    /**
     * Genera los códigos de acceso (carga y almacenamiento) de la variable
     * basados en su tipo e índice dentro del ámbito actual
     * 
     * @param ind Índice de la variable en la tabla del ámbito actual
     */
    public void makeCodes(int ind) {
        localIndex = ind;

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());

        if (form == LOCAL) {
            if (descriptor.equals("I")) {   // Entero
                loadCode.addIload(ind);
                storeCode.addIstore(ind);
            } else if (descriptor.equals("F")) {   // Real
                loadCode.addFload(ind);
                storeCode.addFstore(ind);
            } else if (descriptor.equals("J")) {   // Entero largo
                loadCode.addLload(ind);
                storeCode.addLstore(ind);
            } else if (descriptor.equals("D")) {    // Real doble
                loadCode.addDload(ind);
                storeCode.addDstore(ind);
            } else if (descriptor.equals("Z")) {    // Logico
                loadCode.addIload(ind);
                storeCode.addIstore(ind);
            } else if (descriptor.equals("C")) {    // Caracter
                loadCode.addIload(ind);
                storeCode.addIstore(ind);
            } else if (descriptor.charAt(0) == '[') {    // Arreglo
                storeCode.addAstore(ind);
                loadCode.addAload(ind);
                //preLoad?? //????????????? //??????????????????????
            } else if (descriptor.equals("Lcompiler/Subrango;")) {

                preStoreCode.addAload(ind);
                storeCode.addInvokevirtual("compiler.Subrango", "setValue", "(" + Descriptor.of(baseType) + ")V");

                loadCode.addAload(ind);
                loadCode.addInvokevirtual("compiler.Subrango", "get" + Descriptor.of(baseType) + "value", "()" + Descriptor.of(baseType));
            } else if (descriptor.equals("Lcompiler/Enumerado;")) {
                preStoreCode.addAload(ind);
                storeCode.addInvokevirtual("compiler.Enumerado", "setValue", "(Ljava/lang/String;)V");

                loadCode.addAload(ind);
                loadCode.addInvokevirtual("compiler.Enumerado", "get" + Descriptor.of(baseType) + "value", "()" + Descriptor.of(baseType));
            } else if (descriptor.charAt(0) == 'L') {    // Objeto
                storeCode.addAstore(ind);
                loadCode.addAload(ind);
            } else {
                Errors.add("Variable con  descriptor invalido: " + descriptor, getToken(0));
            }
        } else if (form == FIELD) {


            if (descriptor.equals("Lcompiler/Subrango;")) {   // subrango
                preStoreCode.addGetfield(declaringClassName, name, descriptor);
                storeCode.addInvokevirtual("compiler.Subrango", "setValue", "(" + Descriptor.of(baseType) + ")V");
                loadCode.addGetfield(declaringClassName, name, descriptor);
                loadCode.addInvokevirtual("compiler.Subrango", "get" + Descriptor.of(baseType) + "value", "()" + Descriptor.of(baseType));
            } else if (descriptor.equals("Lcompiler/Enumerado;")) { //enumerado
                preStoreCode.addGetfield(declaringClassName, name, descriptor);
                storeCode.addInvokevirtual("compiler.Enumerado", "setValue", "(Ljava/lang/String;)V");
                loadCode.addGetfield(declaringClassName, name, descriptor);
                loadCode.addInvokevirtual("compiler.Enumerado", "get" + Descriptor.of(baseType) + "value", "()" + Descriptor.of(baseType));
            } else {
                loadCode.addGetfield(declaringClassName, name, descriptor);
                storeCode.addPutfield(declaringClassName, name, descriptor);
            }

        } else if (form == STATIC) {
            if (descriptor.equals("Lcompiler/Subrango;")) { // subrango
                preStoreCode.addGetstatic(declaringClassName, name, descriptor);
                storeCode.addInvokevirtual("compiler.Subrango", "setValue", "(" + Descriptor.of(baseType) + ")V");
                loadCode.addGetstatic(declaringClassName, name, descriptor);
                loadCode.addInvokevirtual("compiler.Subrango", "get" + Descriptor.of(baseType) + "value", "()" + Descriptor.of(baseType));
            } else if (descriptor.equals("Lcompiler/Enumerado;")) { //enumerado

                preStoreCode.addGetstatic(declaringClassName, name, descriptor);
                storeCode.addInvokevirtual("compiler.Enumerado", "setValue", "(Ljava/lang/String;)V");
                loadCode.addGetstatic(declaringClassName, name, descriptor);
                loadCode.addInvokevirtual("compiler.Enumerado", "get" + Descriptor.of(baseType) + "value", "()" + Descriptor.of(baseType));
            } else {
                loadCode.addGetstatic(declaringClassName, name, descriptor);
                storeCode.addPutstatic(declaringClassName, name, descriptor);
            }
        }


    }

    public void setDeclaringClassName(String dc) {
        declaringClassName = dc;
    }

    public String getDeclaringClassName() {
        return declaringClassName;
    }

    public Expresion makeExpresion() {
        Expresion result = new Expresion(this.type);

        result.setVariable(true);
        result.appendCode(getLoadCode());

        return result;
    }

    public Variable getAccessCodes(Expresion[] exprs) {
        boolean isString = (this.descriptor.equals("Ljava/lang/String;"))? true : false;
        String corchetes = "";

        for (int x = 0; x < exprs.length; x++) {
            corchetes += "[]";
        }

        if (!this.isArray() && !this.descriptor.equals("Ljava/lang/String;")) {
            return null;
        }

        Variable result = new Variable(name + corchetes);

        result.appendLoadCode(this.getLoadCode());
        result.appendPreStoreCode(this.getLoadCode());

        if(isString){
            result.getLoadCode().addInvokevirtual("java.lang.String", "toCharArray", "()[C");
            result.getPreStoreCode().addInvokevirtual("java.lang.String", "toCharArray", "()[C");
            result.getPreStoreCode().add(Bytecode.DUP);
                    
            if(exprs.length > 1){
                Errors.add("Una cadena no puede tener mas de una dimension", getToken(0));
                return new VariableError();
            }
        }
        
        for (int i = 0; i < exprs.length; i++) {
            
            Expresion index = exprs[i];
            
            if(compatibleTypes(index.getType(), INTEGER_TYPE)) {
                if (Descriptor.of(INTEGER_TYPE).equals("J")) {
                    index.getCode().addLconst(1);   //los arreglos comienzan desde 1
                    index.getCode().add(Bytecode.LSUB);
                    index.add(Bytecode.L2I);
                } else {
                    index.getCode().addIconst(1);
                    index.getCode().add(Bytecode.ISUB);
                }

                result.appendLoadCode(index.getCode());
                result.appendPreStoreCode(index.getCode());
                char prox;
                
                if(isString){
                    prox = 'C';
                }else{
                    try {
                        prox = descriptor.charAt(i + 1);
                    } catch (IndexOutOfBoundsException ob) {
                        Errors.add("Acceso fuera de los limites del arreglo " + name, getToken(0));

                        return new VariableError();
                    }
                }

                if (prox == 'J') {    // Entero
                    result.getLoadCode().add(Bytecode.LALOAD);
                    result.getStoreCode().add(Bytecode.LASTORE);
                } else if (prox == 'I') {    // Entero
                    result.getLoadCode().add(Bytecode.IALOAD);
                    result.getStoreCode().add(Bytecode.IASTORE);
                } else if (prox == 'D') {  // Real
                    result.getLoadCode().add(Bytecode.DALOAD);
                    result.getStoreCode().add(Bytecode.DASTORE);
                } else if (prox == 'F') {  // Real
                    result.getLoadCode().add(Bytecode.FALOAD);
                    result.getStoreCode().add(Bytecode.FASTORE);
                } else if (prox == 'Z') {  // Logico
                    result.getLoadCode().add(Bytecode.BALOAD);
                    result.getStoreCode().add(Bytecode.BASTORE);
                } else if (prox == 'C') {  // Caracter
                    result.getLoadCode().add(Bytecode.CALOAD);
                    result.getStoreCode().add(Bytecode.CASTORE);
                    
                    if(isString){
                        result.getStoreCode().addInvokestatic("java.lang.String", "copyValueOf", "([C)Ljava/lang/String;");
                        result.appendStoreCode(this.getStoreCode());
                    }
                    
                } else if (prox == 'L') {  // Objeto
                    result.getLoadCode().add(Bytecode.AALOAD);
                    result.getStoreCode().add(Bytecode.AASTORE);
                } else if (prox == '[') {  // Arreglo
                    result.getLoadCode().add(Bytecode.AALOAD);
                    if (i + 1 == exprs.length) {
                        result.getStoreCode().add(Bytecode.AASTORE);  //??
                    }
                    result.getPreStoreCode().add(Bytecode.AALOAD);
                } else {
                    Errors.add("Arreglo con descriptor invalido: " + prox, getToken(0));
                }
            } else {
                Errors.add("Indice no entero para arreglo: " 
                        + Descriptor.of(index.getType()), getToken(0));
            }
        }

        try {
            if(isString){
                result.setType(CtClass.charType);
            }else{
                result.setType(Descriptor.toCtClass(descriptor.substring(exprs.length), classPool));
            }
        } catch (NotFoundException nf) {
            Errors.add("FATAL: Error al construir descriptor de un arreglo", getToken(0));
        }

        return result;
    }

    
    public void printInfo() {
    
       /* protected Bytecode storeCode;
        protected Bytecode loadCode;
        protected Bytecode preStoreCode; //se usa para los arreglos
        protected CtClass type;
        protected CtClass baseType;
        protected String declaringClassName;
        protected String name;
        protected String descriptor;
        protected int form;
        protected int localIndex;
        protected int size = 1;
        protected boolean constant = false;
        protected boolean functionCall = false;*/
        
        System.out.println("");
        System.out.print("Variable info: " + this.getName() + "(");
        
        switch(this.form){
            case Variable.LOCAL:
                System.out.println("LOCAL)");
                break;
            case Variable.FIELD:
                System.out.println("FIELD)");
                break;
            case Variable.PARAMETER:
                System.out.println("PARAMETER)");
                break;
            case Variable.STATIC:
                System.out.println("STATIC)");
                break;
        }
        
        System.out.println("----------------------------------");
        System.out.println("Type descriptor: " + this.getDescriptor());
        System.out.println("declaring Class: " + this.getDeclaringClassName());
        System.out.println("loadCode: ");
        printBytecode(this.getLoadCode());
        System.out.println("storeCode: ");
        printBytecode(this.getStoreCode());
        System.out.println("preStoreCode: ");
        printBytecode(this.getPreStoreCode());
        System.out.println("");
        System.out.println("");

    }
    
    
    public Bytecode getReadCode(){
        
        // Buffer para cada caracter a leer
        Variable buffer = new Variable("buffxxx" + currentScope.getIndexCount(), CtClass.intType, Variable.LOCAL);
        Variable bufferedReader = new Variable();
       
        // Cadena en donde se irá concatenando la lectura
        Variable stringInput = new Variable();        
        try {
            stringInput = new Variable("xxxstringInputxxx" + currentScope.getIndexCount(), classPool.get("java.lang.String"), Variable.LOCAL);
            bufferedReader = new Variable("readerxxx" + currentScope.getIndexCount(), classPool.get("java.io.BufferedReader"), Variable.LOCAL);
            
        } catch (NotFoundException nf) {
            Errors.add("FATAL: Error al cargar la clase String", getToken(0));
        }

        // Añade las variables al contexto actual
        currentScope.add(bufferedReader);
        currentScope.add(stringInput);
        currentScope.add(buffer);
        
        // Bloques de código, separados para calcular saltos
        Bytecode code = new Bytecode(declaring.getConstPool());
        Bytecode code2 = new Bytecode(declaring.getConstPool());
        Bytecode code3 = new Bytecode(declaring.getConstPool());
                
        // instancia un nuevo objeto String
        code.addNew("java.lang.String");    //3
        code.add(Bytecode.DUP); //1
        code.addInvokespecial("java.lang.String", "<init>", "()V"); //3
        // Lo almacena para la cadena acumuladora
        code = append(code, stringInput.getStoreCode());
        
        code.addNew("java.io.BufferedReader");    //3
        code.add(Bytecode.DUP); //1
        
        code.addNew("java.io.InputStreamReader");    //3
        code.add(Bytecode.DUP); //1
        code.addGetstatic("java.lang.System", "in", "Ljava/io/InputStream;");    //3
        code.addInvokespecial("java.io.InputStreamReader", "<init>", "(Ljava/io/InputStream;)V"); //3
        
        code.addInvokespecial("java.io.BufferedReader", "<init>", "(Ljava/io/Reader;)V"); //3   
        code = append(code, bufferedReader.getStoreCode());

        int loopIndexOffset = code.getSize();

        // Lee un caracter por pantalla
        code = append(code, bufferedReader.getLoadCode());   
        code.addInvokevirtual("java.io.Reader", "read", "()I");    //3
        code = append(code, buffer.getStoreCode());
        
        //si el caracter leído es Enter, sale del ciclo        
        code = append(code, buffer.getLoadCode());
        code.addIconst(10); //-
        code.add(Bytecode.IF_ICMPEQ);   
        
        code2 = append(code2, buffer.getLoadCode());
        code2.addIconst(13);
        code2.add(Bytecode.IF_ICMPEQ);
        
        // va concatenando cada caracter en un objeto StringBuilder
        code3.addNew("java.lang.StringBuilder"); //3
        code3.add(Bytecode.DUP); //1
        code3.addInvokespecial("java.lang.StringBuilder", "<init>", "()V");  //3
        code3 = append(code3, stringInput.getLoadCode());
        code3.addInvokevirtual("java.lang.StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");    //3
        code3 = append(code3, buffer.getLoadCode());
        code3.add(Bytecode.I2C);
        code3.addInvokevirtual("java.lang.StringBuilder", "append", "(C)Ljava/lang/StringBuilder;"); //3
        code3.addInvokevirtual("java.lang.StringBuilder", "toString", "()Ljava/lang/String;");   //3
        code3 = append(code3, stringInput.getStoreCode());
        code3 = append(code3, buffer.getLoadCode());
        code3.addIconst(10); //1
        code3.add(Bytecode.IF_ICMPNE);
        
        // calcula los tamaños de los bloques e inserta los saltos
        code2.addIndex(code3.getSize() + 5);
        code2 = append(code2, code3);
       
        code.addIndex(code2.getSize() + 5);
        code = append(code, code2);
        code.addIndex(- code.getSize() + loopIndexOffset + 1);

       // code.addGetstatic("java.lang.System", "in", "Ljava/io/InputStream;");    //3
        //code.addInvokevirtual("java.io.InputStream", "close", "()V");    //3
        // Almacena en la variable actual el valor convertido a su tipo respectivo
        code = getStoreStringCode(code, this, stringInput);
        
        return code;
    }
    
    public void setObjectField(boolean b){
        objectField = b;
    }
}
