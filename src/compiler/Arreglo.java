/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import java.util.ArrayList;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;
import static compiler.Util.declaring;
import static compiler.Util.append;
import static compiler.Util.classPool;
import static compiler.Util.INTEGER_TYPE;
import static compiler.Util.getArrayDescriptor;
import static parser.Pseudo.getToken;

/**
 * Clase para manejar las operaciones y definiciones de los arreglos
 * 
 * @author Carlos Arturo
 */
public class Arreglo extends Variable {

    protected int dimension = 1;
    protected boolean isObject = true;
    //protected CtClass baseType;
    protected Bytecode initCode;
    protected Arreglo subArray = null;
    protected Arreglo superArray = null;
    private Bytecode indexCode = new Bytecode(declaring.getConstPool());

    public Arreglo(CtClass t, int varForm) {
        form = varForm;
        baseType = t;

        descriptor = getArrayDescriptor(1, baseType);

        try {
            type = Descriptor.toCtClass(descriptor, classPool);
        } catch (NotFoundException nf) {
            Errors.add("Error al cargar clase con descriptor: " + descriptor, getToken(0));
        }

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());
    }

    public Arreglo(String nam, CtClass basetp, int f) {
        baseType = basetp;
        name = nam;
        form = f;

        descriptor = getArrayDescriptor(dimension, baseType);

        try {
            type = Descriptor.toCtClass(descriptor, classPool);
        } catch (NotFoundException nf) {
            Errors.add("Error al cargar clase con descriptor: " + descriptor, getToken(0));
        }

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());
    }

    public Arreglo(String nam, CtClass basetp, int f, Expresion[] dim) {
        baseType = basetp;
        name = nam;
        form = f;
        dimension = dim.length;

        descriptor = getArrayDescriptor(dim.length, baseType);

        try {
            type = Descriptor.toCtClass(descriptor, classPool);
        } catch (NotFoundException nf) {
            Errors.add("Error al cargar clase con descriptor: " + descriptor, getToken(0));
        }

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());

        setDimensions(dim);

        if (dimension > 1) {
            subArray = new Arreglo(nam + "[]", basetp, Variable.LOCAL, dimension - 1);
            subArray.getLoadCode().add(Bytecode.AALOAD);
            subArray.getStoreCode().add(Bytecode.AASTORE);
            subArray.superArray = this;
        } else {
            subArray = null;
        }
    }

    public Arreglo(String nam, CtClass basetp, int f, int dim) {
        baseType = basetp;
        name = nam;
        form = f;
        dimension = dim;

        descriptor = getArrayDescriptor(dim, baseType);

        try {
            type = Descriptor.toCtClass(descriptor, classPool);
        } catch (NotFoundException nf) {
            Errors.add("Error al cargar clase con descriptor: " + descriptor, getToken(0));
        }

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());

        if (dimension > 1) {
            subArray = new Arreglo(nam + "[]", basetp, Variable.LOCAL, dimension - 1);
            subArray.getLoadCode().add(Bytecode.AALOAD);
            subArray.getStoreCode().add(Bytecode.AASTORE);
            subArray.superArray = this;
        }
    }

    public Arreglo(String nam, CtClass tp) {

        baseType = tp;
        name = nam;
        descriptor = getArrayDescriptor(dimension, baseType);

        try {
            type = Descriptor.toCtClass(descriptor, classPool);
        } catch (NotFoundException nf) {
            Errors.add("Error al cargar clase con descriptor: " + descriptor, getToken(0));
        }

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());
    }

    public Arreglo() {
        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());
    }

    public Bytecode getInitCode() {
        return initCode;
    }

    public void setInitCode(Bytecode bc) {
        initCode = bc;
    }

    public void setType(CtClass t) {
        type = t;
        descriptor = Descriptor.of(type);

        try {
            if (descriptor.charAt(0) == '[') {
                baseType = Descriptor.toCtClass(Descriptor.toArrayComponent(descriptor, dimension), 
                        classPool);
            } else {
                baseType = type;
            }
        } catch (NotFoundException nf) {
            Errors.add("FATAL: Error al cargar tipo de arreglo", getToken(0));
        }

    }

    public int getDimension() {
        return dimension;
    }

    /**
     * Función que hace algo...
     * @param dim Arreglo de expresiones que corresponden con cada dimensiónn
     * 
     */
    public void setDimensions(Expresion[] dim) {

        initCode = new Bytecode(declaring.getConstPool());

        for (Expresion exp : dim) {
            if (exp.isType(INTEGER_TYPE)) {
                initCode = append(initCode, exp.getCode());
         
                if (Descriptor.of(INTEGER_TYPE).equals("J")) {
                    initCode.add(Bytecode.L2I);
                }
            } else {
                Errors.add("Expresion invalida (no entera) para la construccion de un arreglo", getToken(0));
            }

        }

        if (dimension > 0) {
            initCode.addMultiNewarray(type, dim.length);
        } else {
            Errors.add("Arreglo con tamano igual o menor que cero", getToken(0));
        }
    }

    public Arreglo getSubArray(Bytecode index) {
        subArray.indexCode = index;
        return subArray;
    }

    public Expresion getSizeExpresion() {
        Expresion sizeExpr = new Expresion(INTEGER_TYPE);

        sizeExpr.appendCode(this.loadCode);
        sizeExpr.getCode().add(Bytecode.ARRAYLENGTH);

        if (Descriptor.of(INTEGER_TYPE).equals("J")) {
            sizeExpr.getCode().add(Bytecode.I2L);
        }

        return sizeExpr;
    }

    /*
    public Bytecode getStoreCode() {
    return storeCode;
    }
    
    public Bytecode getLoadCode() {
    return loadCode;
    }*/
    
    
    

    @Override
    public Bytecode getLoadCode() {
        if (superArray == null) {
            return super.getLoadCode(); 
        } else {
            return append(superArray.getLoadCode(), append(indexCode, loadCode));
        }
    }
}
