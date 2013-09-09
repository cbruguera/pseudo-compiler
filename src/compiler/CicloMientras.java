/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import javassist.bytecode.Bytecode;

/**
 * Clase para generar el código de los ciclos "Mientras"
 * @author Carlos Arturo
 */
public class CicloMientras {

    Bytecode code;

    public CicloMientras(Expresion expr, Bytecode bloque) {
        int index, exprSize;
        exprSize = expr.getCode().getSize();
        expr.add(Bytecode.IFEQ);
        
        // index = bloque.getSize() + IFEQ + GOTO;
        index = bloque.getSize() + 3 + 3;
        
        expr.getCode().addIndex(index);
        expr.appendCode(bloque);
        expr.add(Bytecode.GOTO);
        
        // index = - (index + exprSize - GOTO) 
        expr.getCode().addIndex(-(index + exprSize - 3));  

        code = expr.getCode();
    }

    public Bytecode getCode() {
        return code;
    }
}
