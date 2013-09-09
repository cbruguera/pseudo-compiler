/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import javassist.bytecode.Bytecode;

/**
 * Clase que genera código para los ciclos "repetir-hasta" (do-while)
 * 
 * @author Carlos Arturo
 */
public class CicloRepetir {

    Bytecode code;

    public CicloRepetir(Bytecode bloque, Expresion expr) {
        Expresion expResult = new Expresion();
        expResult.appendCode(bloque);
        expResult.appendCode(expr.getCode());
        expResult.add(Bytecode.IFEQ);
        
        // index = - (expr.getCode().getSize() + bloque.getSize())
        expResult.getCode().addIndex(-(expr.getCode().getSize() + bloque.getSize())); 

        code = expResult.getCode();
    }

    public Bytecode getCode() {
        return code;
    }
}
