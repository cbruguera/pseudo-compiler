/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import javassist.bytecode.Bytecode;

/**
 * Clase para generar código de los condicionales simples (si-sino)
 * 
 * @author Carlos Arturo
 */
public class IfCond{
    Bytecode code;

    public IfCond(Expresion expr, Bytecode bloque) {
        
        expr.add(Bytecode.IFEQ);
        expr.getCode().addIndex(3 + bloque.getSize());  // IFEQ + bloque.getSize()
        expr.appendCode(bloque);
        expr.add(Bytecode.NOP);

        code = expr.getCode();
    }

    public IfCond(Expresion expr, Bytecode bloque, Bytecode elseb) {

        expr.add(Bytecode.IFNE);
        expr.getCode().addIndex(3 + elseb.getSize() + 3);   //IFNE + elseb.getSize() + GOTO
        expr.appendCode(elseb);
        expr.add(Bytecode.GOTO);
        expr.getCode().addIndex(bloque.getSize() + 3);  // index = bloque.getSize() + goto
        expr.appendCode(bloque);
        expr.add(Bytecode.NOP);

        code = expr.getCode();
    }

    public Bytecode getCode(){
        return code;
    }
}
