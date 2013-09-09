/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import javassist.CtClass;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;
import sun.rmi.server.InactiveGroupException;
import static parser.Pseudo.getToken;
import static compiler.Util.INTEGER_TYPE;
/**
 * Clase que genera código para los ciclos "Para" (for)
 * @author Carlos Arturo
 */
public class CicloPara {

    Bytecode code;

    public CicloPara(Variable x, Expresion expr1, Expresion expr2, Expresion expr3, Bytecode bloque) {

        if (expr3 == null) {
            expr3 = new Expresion();
            expr3.setType(expr1.getType());
            if (Descriptor.of(expr3.getType()).equals("J")) {
                expr3.getCode().addLconst(1);
            } else if (Descriptor.of(expr3.getType()).equals("I")) {
                expr3.getCode().addIconst(1);
            } else if (Descriptor.of(expr3.getType()).equals("F")) {
                expr3.getCode().addFconst(1);
            } else if (Descriptor.of(expr3.getType()).equals("D")) {
                expr3.getCode().addDconst(1);
            }
        }
        
        expr1.appendCode(x.getStoreCode());

        if (Descriptor.of(x.getType()).equals(Descriptor.of(expr1.getType())) && 
                Descriptor.of(x.getType()).equals(Descriptor.of(expr3.getType()))) {
            if (Descriptor.of(x.getType()).equals("I")) {
                expr1.appendCode(x.getLoadCode());
                expr1.appendCode(expr2.getCode());
                expr1.add(Bytecode.IF_ICMPGT);
                
                // index =  IF_ICMPEQ + bloque.getSize() + x.getLoadCode().getSize() 
                // + expr3.getCode().getSize() + IADD + x.getStoreCode().getSize() + GOTO
                expr1.getCode().addIndex(3 + bloque.getSize() + x.getLoadCode().getSize() + 
                        expr3.getCode().getSize() + 1 + x.getStoreCode().getSize() + 3);
                
                expr1.appendCode(bloque);
                expr1.appendCode(x.getLoadCode());
                expr1.appendCode(expr3.getCode());
                expr1.add(Bytecode.IADD);
                expr1.appendCode(x.getStoreCode());
                expr1.add(Bytecode.GOTO);
                // index = - (x.getStoreCode().getSize() + IADD + expr3.getCode().getSize() 
                // + x.getLoadCode().getSize() + bloque.getSize() + IF_ICMPGT + 
                // expr2.getCode().getSize() + x.getLoadCode().getSize() - 1)
                expr1.getCode().addIndex(-(x.getStoreCode().getSize() + 1 + expr3.getCode().getSize() 
                        + x.getLoadCode().getSize() + bloque.getSize() + 3 + expr2.getCode().getSize() + 
                        x.getLoadCode().getSize()));
                
            } else if (Descriptor.of(x.getType()).equals("J")) {
                expr1.appendCode(x.getLoadCode());
                expr1.appendCode(expr2.getCode());
                expr1.add(Bytecode.LCMP);
                expr1.add(Bytecode.IFGT);
                
                // index =  IF_ICMPEQ + bloque.getSize() + x.getLoadCode().getSize() 
                // + expr3.getCode().getSize() + IADD + x.getStoreCode().getSize() + GOTO
                expr1.getCode().addIndex(3 + bloque.getSize() + x.getLoadCode().getSize() + 
                        expr3.getCode().getSize() + 1 + x.getStoreCode().getSize() + 3);
                
                expr1.appendCode(bloque);
                expr1.appendCode(x.getLoadCode());
                expr1.appendCode(expr3.getCode());
                expr1.add(Bytecode.LADD);
                expr1.appendCode(x.getStoreCode());
                expr1.add(Bytecode.GOTO);
                
                // index = - (x.getStoreCode().getSize() + LADD + expr3.getCode().getSize() 
                // + x.getLoadCode().getSize() + bloque.getSize() + LCMP + IFEQ 
                // + expr2.getCode().getSize() + x.getLoadCode().getSize() - 1)
                expr1.getCode().addIndex(-(x.getStoreCode().getSize() + 1 + expr3.getCode().getSize() 
                        + x.getLoadCode().getSize() + bloque.getSize() + 1 + 3 
                        + expr2.getCode().getSize() + x.getLoadCode().getSize()));
                
            } else if (Descriptor.of(x.getType()).equals("F")) {
                expr1.appendCode(x.getLoadCode());
                expr1.appendCode(expr2.getCode());
                expr1.add(Bytecode.FCMPL);
                expr1.add(Bytecode.IFGT);
                
                // index =  FCMPL + IFEQ + bloque.getSize() + x.getLoadCode().getSize() 
                // + expr3.getCode().getSize() DADD + x.getStoreCode().getSize() + GOTO
                expr1.getCode().addIndex(1 + 3 + bloque.getSize() + x.getLoadCode().getSize() 
                        + expr3.getCode().getSize() + 1 + x.getStoreCode().getSize() + 3);
                
                expr1.appendCode(bloque);
                expr1.appendCode(x.getLoadCode());
                expr1.appendCode(expr3.getCode());
                expr1.add(Bytecode.FADD);
                expr1.appendCode(x.getStoreCode());
                expr1.add(Bytecode.GOTO);
                
                // index = - (x.getStoreCode().getSize() + DADD + expr3.getCode().getSize() 
                // + x.getLoadCode().getSize() + bloque.getSize() + IF_ICMPEQ + 
                // expr2.getCode().getSize() + x.getLoadCode().getSize() - 1)
                expr1.getCode().addIndex(-(x.getStoreCode().getSize() + 1 
                        + expr3.getCode().getSize() + x.getLoadCode().getSize() 
                        + bloque.getSize() + 3 + expr2.getCode().getSize() 
                        + x.getLoadCode().getSize()));
                
            } else if (Descriptor.of(x.getType()).equals("D")) {
                expr1.appendCode(x.getLoadCode());
                expr1.appendCode(expr2.getCode());
                expr1.add(Bytecode.DCMPL);
                expr1.add(Bytecode.IFGT);
                // index =  FCMPL + IFEQ + bloque.getSize() + x.getLoadCode().getSize() 
                // + expr3.getCode().getSize() DADD + x.getStoreCode().getSize() + GOTO
                expr1.getCode().addIndex(1 + 3 + bloque.getSize() + x.getLoadCode().getSize() 
                        + expr3.getCode().getSize() + 1 + x.getStoreCode().getSize() + 3);
                
                expr1.appendCode(bloque);
                expr1.appendCode(x.getLoadCode());
                expr1.appendCode(expr3.getCode());
                expr1.add(Bytecode.DADD);
                expr1.appendCode(x.getStoreCode());
                expr1.add(Bytecode.GOTO);
                
                // index = - (x.getStoreCode().getSize() + DADD + expr3.getCode().getSize() 
                // + x.getLoadCode().getSize() + bloque.getSize() + IF_ICMPEQ 
                // + expr2.getCode().getSize() + x.getLoadCode().getSize() - 1)
                expr1.getCode().addIndex(-(x.getStoreCode().getSize() + 1 
                        + expr3.getCode().getSize() + x.getLoadCode().getSize() 
                        + bloque.getSize() + 3 + expr2.getCode().getSize() 
                        + x.getLoadCode().getSize()));
            } else {
                Errors.add("El encabezado de la estructura \"PARA\" solo debe tener expresiones reales o enteras", getToken(0));
            }

        } else {
            Errors.add("Estructura \"PARA\" contiene expresiones de tipos incompatibles", getToken(0));
        }

        code = expr1.getCode();
    }


    public Bytecode getCode() {
        return code;
    }
}
