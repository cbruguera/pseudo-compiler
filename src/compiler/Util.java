/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import javassist.bytecode.BadBytecode;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import javassist.CtMethod;
import javassist.CtConstructor;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;

import static parser.Pseudo.getToken;

/**
 * Clase que contiene una serie de métodos estáticos
 * de utilidad general para otros métodos y clases.
 * 
 * @author Carlos Arturo
 */
public class Util {

    public static boolean debug = false;
    
    public static ClassPool classPool;
    public static ClassFile declaring;
    public static Method declaringMethod;
    public static TypeTable declaredTypes;
    public static ArrayList<String> pendingTypes;
    public static ArrayList<CtClass> registryList;
    public static Scope globalScope;
    public static Scope currentScope;
    public static Variable[] mainParameters;
        
    // Valores por defecto para los tipos entero y real (podrían ser tipos de doble precisión)
    public static CtClass INTEGER_TYPE = CtClass.intType;
    public static CtClass FLOAT_TYPE = CtClass.floatType;

    
    public static ClassFile getClassFile(String name) {
        try {
            CtClass cls = classPool.get(name);
            return cls.getClassFile();
        } catch (NotFoundException e) {
            return null;
        }       

    }

    public static CtClass getCtClass(String name) {
        try {
            CtClass cls = classPool.get(name);
            return cls;
        } catch (NotFoundException e) {
            return null;
        }
    }

    /**
     * Obtiene una clase del pool de clases, si ésta no existe, la crea.
     * 
     * @param name Nombre de la clase
     * @return objeto de tipo ClassFile
     */
    public static ClassFile getOrCreateClass(String name) {
        /*      File fl = new File(name + ".class");
        
        
        if (fl.exists()) {
        fl.delete();    // ????????
        } else {
        }*/

        ClassFile cfile;

        cfile = getClassFile(name);
        if (cfile != null) {
            return cfile;
        } else {
            CtClass ct = classPool.makeClass(name);
            return ct.getClassFile();
        }
    }

    /**
     * Crea una nueva clase con el nombre dado.
     * @param name Nombre de la clase
     * @return objeto tipo ClassFile
     */
    public static ClassFile createClass(String name) {
        File fl = new File(name + ".class");

        if (fl.exists()) {
            fl.delete();
        } else {
        }

        CtClass ct = classPool.makeClass(name);
        return ct.getClassFile();
    }

    /**
     * Concatena dos secuencias de Bytecode y devuelve el resultado.
     * 
     * @param code1 Bytecode 1
     * @param code2 Bytecode 2
     * @return Bytecode resultante de la concatenación
     */
    public static Bytecode append(Bytecode code1, Bytecode code2) {
        byte[] bytes1 = code1.get();
        byte[] bytes2 = code2.get();
        Bytecode result = new Bytecode(declaring.getConstPool());

        for (int i = 0; i < bytes1.length; i++) {
            result.add(bytes1[i]);
        }

        for (int i = 0; i < bytes2.length; i++) {
            result.add(bytes2[i]);
        }

        return result;
    }

    /**
     * Concatena dos secuencias de Bytecode y devuelve el resultado.
     * 
     * @param code1 Bytecode 1
     * @param code2 Bytecode 2
     * @return Bytecode resultante de la concatenación
     */
    public static Bytecode append(byte[] bytes1, byte[] bytes2) {

        Bytecode result = new Bytecode(declaring.getConstPool());

        for (int i = 0; i < bytes1.length; i++) {
            result.add(bytes1[i]);
        }

        for (int i = 0; i < bytes2.length; i++) {
            result.add(bytes2[i]);
        }

        return result;
    }

    /**
     * Toma un objeto de tipo CodeAttribute y retorna su respectivo equivalente
     * de tipo Bytecode.
     * 
     * @param attr objeto CodeAttribute
     * @param cp ConstantPool
     * @return Bytecode resultante
     */
    public static Bytecode toBytecode(CodeAttribute attr, ConstPool cp) {
        Bytecode bc = new Bytecode(cp);
        CodeIterator codeIter = attr.iterator();

        int length = codeIter.getCodeLength();
        for (int i = 0; i < length; i++) {
            bc.add(codeIter.byteAt(i));
        }

        return bc;
    }

    /**
     * Dado un objeto de tipo ClassFile, escribe su respectivo archivo .class
     * en el directorio base.
     * 
     * @param cf objeto ClassFile a escribir
     */
    public static void writeFile(ClassFile cf) {
        CtClass ct;
        try {

            String classname = cf.getName();
            String filename = "." + File.separatorChar
                    + classname.replace('.', File.separatorChar) + ".class";
            int pos = filename.lastIndexOf(File.separatorChar);
            if (pos > 0) {
                String dir = filename.substring(0, pos);
                if (!dir.equals(".")) {
                    new File(dir).mkdirs();
                }
            }

            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(filename)));

            cf.write(out);
            out.close();

        } catch (IOException e) {
            //e.printStackTrace();
            Errors.add("Error al escribir el archivo " + cf.getName() + ": " + e.getMessage(),
                    getToken(0));

        }
    }

    /**
     * Activa el algoritmo para la recuperación en caso de error. 
     * No hace NADA actualmente.
     */
    public static void doErrorRecovery() {
    }

    /**
     * Imprime algunos parámetros de información de un ClassFile dado.
     * Se usa para depurar.
     * @param cf 
     */
    public static void printClassInfo(ClassFile cf) {
        ArrayList<MethodInfo> methods = (ArrayList) cf.getMethods();
        Expresion aux = new Expresion();
        System.out.println("");
        System.out.println("Writing file " + cf.getName());
        System.out.println("minor = " + cf.getMinorVersion());
        System.out.println("major = " + cf.getMajorVersion());
        System.out.println("Superclass = " + cf.getSuperclass());
        int count = 1;

        for (MethodInfo method : methods) {

            System.out.print("Method #" + count + ": ");
            System.out.print(method.getName() + " ");
            System.out.println(method.getDescriptor());

            aux.appendCode(method.getCodeAttribute().getCode());
            aux.clearCode();
            count++;
        }

    }

    /**
     * Imprime algunos parámetros de información de un ClassFile dado.
     * Se usa para depurar.
     * @param cf 
     */
    public static void printClassInfo(CtClass cl) {
        //ArrayList<MethodInfo> methods = (ArrayList) cl.getMethods();
        CtMethod methods[] = cl.getDeclaredMethods();
        CtConstructor constructors[] = cl.getConstructors();
        Expresion aux = new Expresion();
        System.out.println("");
        System.out.println("Writing class " + cl.getName());

        //System.out.println("minor = " + cl.getMinorVersion());
        //System.out.println("major = " + cl.getMajorVersion());
        try {
            System.out.println("Superclass = " + cl.getSuperclass().getName());
        } catch (NotFoundException nf) {
            Errors.add("No se encuentra la superclase de " + cl.getName(), getToken(0));
        }
        int count = 1;

        System.out.println("Numer of methods: " + methods.length);
        for (CtMethod method : methods) {

            System.out.print("Method #" + count + ": ");
            System.out.print(method.getName() + ":  ");
            System.out.println(method.getSignature());

            count++;
        }

        count = 1;

        System.out.println("Number of constructors: " + constructors.length);
        for (CtConstructor con : constructors) {
            System.out.print("Constructor #" + count + ": ");
            System.out.println(con.getLongName());
            count++;
        }

    }

    /**
     * Imprime la secuencia de bytecode de un segmento dado.
     * Se usa para depurar.
     * 
     * @param code Bytecode a imprimir
     */
    public static void printBytecode(Bytecode code) {
        CodeIterator ci = code.toCodeAttribute().iterator();
        ci.begin();
        int index;

        System.out.println("Bytecode (size: " + code.getSize() + ")");
        System.out.println("---------");


        while (ci.hasNext()) {
            try {
                index = ci.next();
                int op = ci.byteAt(index);
                System.out.println(Mnemonic.OPCODE[op]);
            } catch (Exception e) {
                Errors.add("Error de bytecode en la expresión", getToken(0));
            }
        }

        System.out.println();
    }

    /**
     * Imprime la secuencia Bytecode de un objeto CodeAttribute dado.
     * Se usa para depurar.
     * 
     * @param code CodeAttribute a imprimir
     */
    public static void printBytecode(CodeAttribute code) {
        CodeIterator ci = code.iterator();
        ci.begin();
        int index;

        System.out.println("Bytecode:");
        System.out.println("---------");


        while (ci.hasNext()) {
            try {
                index = ci.next();
                int op = ci.byteAt(index);
                System.out.println(Mnemonic.OPCODE[op]);
            } catch (Exception e) {
                Errors.add("Error de bytecode en la expresión", getToken(0));
            }
        }

        System.out.println();
    }

    /**
     * Genera el código de inicialización para una variable de tipo Enumerado
     * NO SE ESTÁ USANDO
     * 
     * @param var
     * @param varForm
     * @param ids
     * @return 
     */
    /*public static Bytecode getEnumeradoInitCode(Variable var, int varForm, ArrayList<String> ids) {
    Bytecode result = new Bytecode(declaring.getConstPool());
    
    result.addNew("compiler.Enumerado");
    result.add(Bytecode.DUP);
    for (String id : ids) {
    result.addLdc(id);
    }
    
    result.addInvokespecial("compiler.Enumerado", "<init>", "([Ljava/lang/String;)V");
    
    if (varForm == Variable.LOCAL || varForm == Variable.PARAMETER) {
    result.addAstore(var.getLocalIndex());
    } else if (varForm == Variable.STATIC) {
    result.addPutstatic("compiler.Enumerado", var.getName(), var.getDescriptor());
    } else if (varForm == Variable.FIELD) {
    result.addPutfield("compiler.Enumerado", var.getName(), var.getDescriptor());
    }
    return result;
    }*/
    /**
     * Genera el código de inicialización en tiempo de ejecución para un objeto
     * de tipo Subrango.
     * 
     * @param var Variable de tipo subrango
     * @param varForm Forma de la variable (local, atributo de clase, etc)
     * @param exp1 Expresión del límite inferior del subrango
     * @param exp2 Expresión del límite superior del subrango
     * @return Bytecode de la inicialización del objeto creado
     */
    public static Bytecode getSubrangoInitCode(Variable var, int varForm, Expresion exp1, Expresion exp2) {
        String desc = var.getBaseDescriptor();
        String exp1Desc = Descriptor.of(exp1.getType());
        String exp2Desc = Descriptor.of(exp2.getType());
        Bytecode result = new Bytecode(declaring.getConstPool());

        if (exp1Desc.equals(desc) && exp2Desc.equals(desc)) {
            result.addNew("compiler.Subrango");
            result.add(Bytecode.DUP);
            result = append(result, exp1.getCode());
            result = append(result, exp2.getCode());
            result.addInvokespecial("compiler.Subrango", "<init>", "(" + desc + desc + ")V");

            if (varForm == Variable.LOCAL || varForm == Variable.PARAMETER) {
                result.addAstore(var.getLocalIndex());
            } else if (varForm == Variable.STATIC) {
                result.addPutstatic("compiler.Subrango", var.getName(), var.getDescriptor());
            } else if (varForm == Variable.FIELD) {
                result.addPutfield("compiler.Subrango", var.getName(), var.getDescriptor());
            }

        } else {
            Errors.add("Declaracion de limites invalidos para el subrango", getToken(0));
        }


        return result;
    }

    /**
     * Dado un número de dimensiones y un tipo base, construye el descriptor de
     * un arreglo.
     * 
     * @param dim Dimensiones
     * @param baseType Tipo base
     * @return Cadena del descriptor del arreglo
     */
    public static String getArrayDescriptor(int dim, CtClass baseType) {
        String buff = new String();
        for (int i = 0; i < dim; i++) {
            buff = buff + "[";
        }
        buff = buff + Descriptor.of(baseType);
        return buff;
    }

    /**
     * Genera el código para un constructor de arreglo, dado un tipo base y una
     * lista de expresiones para los valores iniciales del arreglo.
     * 
     * @param listaExp Lista de valores (tipo Expresión)
     * @param tipo Tipo base del arreglo
     * @return Expresión con el código del constructor del arreglo
     */
    public static Expresion getArrayConstructorCode(Expresion[] listaExp, CtClass tipoBase) {
        String desc = "[";

        Expresion newExp = new Expresion();
        Bytecode bc = new Bytecode(declaring.getConstPool());

        CtClass arrayType = CtClass.voidType;
        CtClass subtype = CtClass.voidType;

        if (Descriptor.of(tipoBase).equals("V")) {
            if (listaExp.length > 0) {
                subtype = listaExp[0].getType();
            }
        } else {
            subtype = tipoBase;
        }

        desc = desc + Descriptor.of(subtype);

        try {
            arrayType = Descriptor.toCtClass(desc, classPool);

        } catch (NotFoundException nf) {
            Errors.add("FATAL: Error al cargar clase con descriptor: " + desc, getToken(0));
        }

        if (desc.charAt(1) == '[' || desc.charAt(1) == 'L') {
            bc.addAnewarray(subtype, listaExp.length);
        } else {
            if (!Descriptor.of(subtype).equals("V")) {
                bc.addNewarray(getClassType(subtype), listaExp.length);
            } else {
                Errors.add("Constructores de arreglos vacíos no están permitidos", getToken(0));
            }
        }

        for (int i = 0; i < listaExp.length; i++) {
            Expresion exp = listaExp[i];
            String exprDesc = Descriptor.of(exp.getType());

            if (compatibleTypes(subtype, exp.getType())) {
                bc.add(Bytecode.DUP);
                bc.addIconst(i);
                bc = append(bc, exp.getCode());

                if (exprDesc.equals("I")) {
                    bc.add(Bytecode.IASTORE);
                } else if (exprDesc.equals("J")) {
                    bc.add(Bytecode.LASTORE);
                } else if (exprDesc.equals("F")) {
                    bc.add(Bytecode.FASTORE);
                } else if (exprDesc.equals("D")) {
                    bc.add(Bytecode.DASTORE);
                } else if (exprDesc.equals("C")) {
                    bc.add(Bytecode.CASTORE);
                } else if (exprDesc.equals("B")) {
                    bc.add(Bytecode.BASTORE);
                } else {
                    bc.add(Bytecode.AASTORE);
                }
            } else {
                Errors.add("Constructor de arreglos con tipos de expresiones de tipos desiguales", getToken(0));
                break;
            }
        }

        newExp = new Expresion(arrayType);
        newExp.appendCode(bc);

        return newExp;
    }

    public static Expresion getArrayTypeInit(Expresion[] dimensiones, CtClass tipoBase) {
        String desc = "";

        Expresion newExp = new Expresion();
        Bytecode code = new Bytecode(declaring.getConstPool());
        CtClass arrayType = CtClass.voidType;

        for (int i = 0; i < dimensiones.length; i++) {
            Expresion expr = dimensiones[i];

            if (compatibleTypes(expr.getType(), INTEGER_TYPE)) {
                desc = desc + "[";
                code = append(code, expr.getCode());

                if (Descriptor.of(INTEGER_TYPE).equals("J")) {
                    code.add(Bytecode.L2I);
                }
            } else {
                Errors.add("Tamano no entero para definir un arreglo", getToken(0));
                return new ExpresionError();    // añadir descripcion del error?
            }
        }

        desc = desc + Descriptor.of(tipoBase);

        try {
            arrayType = Descriptor.toCtClass(desc, classPool);
        } catch (NotFoundException nf) {
            Errors.add("Error al traducir descriptor: " + desc, getToken(0));
            return new ExpresionError();
        }


        code.addMultiNewarray(arrayType, dimensiones.length);

        newExp = new Expresion(arrayType);
        newExp.appendCode(code);

        return newExp;
    }

    /**
     * Procedimiento para añadir al final de una secuencia de bytecode, código 
     * para almacenar un string (un objeto de tipo Variable) dentro de
     * otra variable haciendo la conversión necesaria de tipos
     * 
     * @param code Bytecode en donde será almacenado el código
     * @param variable Variable en donde será almacenada la cadena
     * @param stringInput Cadena a almacenar
     */
    public static Bytecode getStoreStringCode(Bytecode code, Variable variable, Variable stringInput) {

        Bytecode result = append(code, new Bytecode(declaring.getConstPool()));

        String varDesc = Descriptor.of(variable.getType());

        result = append(result, variable.getPreStoreCode());
        //variable.printInfo();

        if (varDesc.equals("I")) {

            result = append(result, stringInput.getLoadCode());
            result.addInvokestatic("java.lang.Integer", "parseInt", "(Ljava/lang/String;)I");
            result = append(result, variable.getStoreCode());
        } else if (varDesc.equals("J")) {
            result = append(result, stringInput.getLoadCode());
            result.addInvokestatic("java.lang.Long", "parseLong", "(Ljava/lang/String;)J");
            result = append(result, variable.getStoreCode());
        } else if (varDesc.equals("F")) {
            result = append(result, stringInput.getLoadCode());
            result.addInvokestatic("java.lang.Float", "parseFloat", "(Ljava/lang/String;)F");
            result = append(result, variable.getStoreCode());
        } else if (varDesc.equals("D")) {
            result = append(result, stringInput.getLoadCode());
            result.addInvokestatic("java.lang.Double", "parseDouble", "(Ljava/lang/String;)D");
            result = append(result, variable.getStoreCode());
        } else if (varDesc.equals("C")) {
            result = append(result, stringInput.getLoadCode());
            result.addIconst(0);
            result.addInvokevirtual("java.lang.String", "charAt", "(I)C");
            result = append(result, variable.getStoreCode());
        } /*else if (varDesc.equals("Z")) {  
        }*/ else if (varDesc.equals("Ljava/lang/String;")) {
            result = append(result, stringInput.getLoadCode());
            result = append(result, variable.getStoreCode());
        } else if (varDesc.equals("Lcompiler/Subrango;")) {
            result = append(result, variable.getPreStoreCode());
            result = append(result, stringInput.getLoadCode());
            result = append(result, variable.getStoreCode());
        } else if (varDesc.equals("Lcompiler/Enumerado;")) {
            result = append(result, variable.getPreStoreCode());
            result = append(result, stringInput.getLoadCode());
            result = append(result, variable.getStoreCode());
        } else {
            Errors.add("Tipo invalido para la lectura de la variable", getToken(0));
        }

        return result;
    }

    /**
     * Obtiene un tipo de clase (en formato entero) a partir de un tipo CtClass
     * @param c clase a convertir
     * @return entero que representa el tipo de la clase dada
     */
    public static int getClassType(CtClass c) {
        String desc = Descriptor.of(c);
        if (desc.equals("I")) {
            return Bytecode.T_INT;
        } else if (desc.equals("J")) {
            return Bytecode.T_LONG;
        } else if (desc.equals("F")) {
            return Bytecode.T_FLOAT;
        } else if (desc.equals("D")) {
            return Bytecode.T_DOUBLE;
        } else if (desc.equals("C")) {
            return Bytecode.T_CHAR;
        } else if (desc.equals("B")) {
            return Bytecode.T_BOOLEAN;
        } else {
            Errors.add("Intento de obtener tipo base a partir de una clase no soportada: " + Descriptor.of(c), getToken(0));
            return -1;
        }
    }

    /**
     * Obtiene el código de instanciación de un objeto (sin argumentos para el 
     * constructor).
     * 
     * @param type Clase a instanciar
     * @return Bytecode con la secuencia de instanciación.
     */
    public static Bytecode getInstantiationCode(CtClass type) {
        return getInstantiationCode(type, new Expresion[0]);
    }

    /**
     * Obtiene el código de instanciación de un objeto (con un arreglo de
     * argumentos para el constructor)
     * 
     * @param type Clase a instanciar
     * @param args Arreglo de argumentos para el constructor
     * @return Bytecode con las instrucciones de la instanciación
     */
    public static Bytecode getInstantiationCode(CtClass type, Expresion[] args) {
        Bytecode bc = new Bytecode(declaring.getConstPool());
        ArrayList<CtClass> types = new ArrayList<CtClass>();
        CtClass[] ctArgumentos = new CtClass[args.length];
        for (int i = 0; i < args.length; i++) {
            ctArgumentos[i] = args[i].getType();
        }

        bc.addNew(type.getName());
        bc.add(Bytecode.DUP);

        for (Expresion arg : args) {
            bc = append(bc, arg.getCode());
            types.add(arg.getType());
        }

        String desc = Descriptor.ofConstructor(types.toArray(new CtClass[0]));
        bc.addInvokespecial(type.getName(), "<init>", desc);


        return bc;
    }

    public static boolean addRegistry(CtClass clazz) {

        if (registryList == null) {
            return false;
        } else {
            return registryList.add(clazz);
        }

    }

    public static boolean isRegistry(CtClass clazz) {
        return registryList.contains(clazz);
    }

    public static boolean addClassField(Variable var, int varForm, int accessFlags) {
        if (varForm == Variable.STATIC) {
            FieldInfo campo = new FieldInfo(declaring.getConstPool(), var.getName(), var.getDescriptor());

            if (accessFlags == AccessFlag.PROTECTED) {
                campo.setAccessFlags(AccessFlag.setProtected(AccessFlag.STATIC));
            } else if (accessFlags == AccessFlag.PUBLIC) {
                campo.setAccessFlags(AccessFlag.setPublic(AccessFlag.STATIC));
            } else if (accessFlags == AccessFlag.PRIVATE) {
                campo.setAccessFlags(AccessFlag.setPrivate(AccessFlag.STATIC));
            }

            try {
                declaring.addField(campo);
            } catch (DuplicateMemberException dm) {
                Errors.add("Definicion duplicada del campo " + campo.getName()
                        + "para la clase principal", getToken(0));
                return false;
            }
        } else if (varForm == Variable.FIELD) {
            FieldInfo campo = new FieldInfo(declaring.getConstPool(), var.getName(), var.getDescriptor());
            campo.setAccessFlags(accessFlags);

            try {
                declaring.addField(campo);
            } catch (DuplicateMemberException dm) {
                Errors.add("Definicion duplicada del campo " + campo.getName()
                        + " para la clase " + declaring.getName(), getToken(0));
                return false;
            }
        }

        return true;
    }

    public static boolean addClassConstructor(ClassFile clase, Bytecode atributosInit) {
        MethodInfo constructorInit = new MethodInfo(clase.getConstPool(), MethodInfo.nameInit, "()V");
        Bytecode init = new Bytecode(declaring.getConstPool());
        init.addAload(0);

        try {
            init.addInvokespecial(classPool.get(clase.getSuperclass()), MethodInfo.nameInit, "()V");
        } catch (NotFoundException nf) {
            Errors.add("FATAL: No se encuentra la clase Object", getToken(0));
            return false;
        }

        atributosInit.add(Bytecode.RETURN);
        atributosInit = append(init, atributosInit);
        CodeAttribute initCode = atributosInit.toCodeAttribute();


        initCode.setMaxLocals(currentScope.getMaxLocals());

        try {
            initCode.computeMaxStack();

        } catch (BadBytecode bb) {
            Errors.add("FATAL (BadBytecode): Error en bytecode del constructor de la clase "
                    + clase.getName() + "\nCausa: " + bb.getMessage(), getToken(0));
            //System.out.println("\nBAD BYTECODE (constructor)");
            //printBytecode(initCode);
            //return false;

        }

        constructorInit.setCodeAttribute(initCode);

        try {
            constructorInit.setAccessFlags(AccessFlag.PUBLIC);
            clase.addMethod(constructorInit);
        } catch (DuplicateMemberException e) {
            Errors.add("Definicion duplicada para constructor de la clase " + clase.getName(),
                    getToken(0));
        }

        return true;
    }


    public static Bytecode preAppendThisCode(Bytecode bc) {
        Bytecode thisbc = new Bytecode(declaring.getConstPool());
        thisbc.addAload(0);
        return append(thisbc, bc);
    }

    /**
     * Función que genera código para inicializar (instanciar) todas las posiciones
     * de un arreglo de algún tipo registro. Esta función utiliza un ciclo "for" por
     * cada dimensión del arreglo para recorrerlo e instanciar cada posición.
     * 
     * @param tipo: Tipo del registro
     * @param array: Variable de tipo arreglo que requiere inicialización
     * @return una secuencia de Bytecode
     */
    public static Bytecode getRegistryArrayInitCode(CtClass tipo, Arreglo array) {
        Bytecode init = new Bytecode(declaring.getConstPool());
        CicloPara loop;

        Expresion expr1 = new Expresion(INTEGER_TYPE);
        Expresion expr2 = new Expresion(INTEGER_TYPE);
        Expresion expr3 = new Expresion(INTEGER_TYPE);

        expr1.getCode().addIconst(1);   //PARA ENTERO x = 1

        Expresion size = array.getSizeExpresion();
        expr2.appendCode(size.getCode());   // HASTA arr.length

        expr3.getCode().addIconst(1);   // EN 1 //x++

        //HACER 

        // int x: contador del ciclo
        Variable x = new Variable("rrrArrInitCountrrr" + currentScope.getIndexCount(), expr1.getType(), Variable.LOCAL);

        if (!currentScope.add(x)) {
            Errors.add("Error al crear indice de ciclo Para", getToken(0));
        }

        if (array.getDimension() == 1) {
            init = append(init, array.getLoadCode());
            init = append(init, x.getLoadCode());

            init = append(init, getInstantiationCode(tipo));    //instancia el registro
            init.add(Bytecode.AASTORE);
        } else {

            Arreglo sub = array.getSubArray(x.getLoadCode());
            init = append(init, getRegistryArrayInitCode(tipo, sub));
        }

        loop = new CicloPara(x, expr1, expr2, expr3, init);

        return loop.getCode();
    }

    /**
     * Función que compara dos tipos CtClass para verificar que sean compatibles 
     * al momento de una asignación de variable
     * 
     * @param type1: primer tipo (usualmente del lado izquierdo de la asignación)
     * @param type2: segundo tipo (usualmente del lado derecho de una asignación)
     * @return true en caso de ser compatibles, false en caso contrario
     */
    public static boolean compatibleTypes(CtClass type1, CtClass type2) {

        // Si ambos son exactamente el mismo tipo
        if (Descriptor.of(type1).equals(Descriptor.of(type2))) {
            return true;
        }
        
        // si tipo2 hereda de tipo1
        if (type2.subclassOf(type1)) {
            return true;
        }

        // Compatibilidad en asignación de entero a caracter
        if (Descriptor.of(type1).equals("C") && Descriptor.of(type2).equals(Descriptor.of(INTEGER_TYPE))) {
            return true;
        }

        // Compatibilidad en asignación de caracter a entero
        if (Descriptor.of(type2).equals("C") && Descriptor.of(type1).equals(Descriptor.of(INTEGER_TYPE))) {
            return true;
        }

        return false;
    }
    
    public static void print(String s){
        System.out.println(s);
    }
}
