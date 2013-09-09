/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import java.util.ArrayList;
import javassist.bytecode.Bytecode;

/**
 * Clase para generar el código de los bloques de Selección múltiple
 * @author Carlos Arturo
 */
public class BloqueSeleccion {

    Bytecode code;

    public BloqueSeleccion(ArrayList<Expresion> listaExp, ArrayList<Bytecode> listaBloques) {
        int expCount = listaExp.size();
        Expresion expBuff;
        Bytecode bloque;
        Expresion expResult = new Expresion();
        int selSize = 0;

        for (int i = 0; i < expCount; i++) {
            expBuff = listaExp.get(i);
            bloque = listaBloques.get(i);
            selSize = selSize + expBuff.getCode().getSize() + 3 + bloque.getSize() + 3;
        }

        for (int i = 0; i < expCount; i++) {
            expBuff = listaExp.get(i);
            bloque = listaBloques.get(i);
            expResult.appendCode(expBuff.getCode());
            expResult.add(Bytecode.IFEQ);
            
            // index = IFEQ + bloque + GOTO
            expResult.getCode().addIndex(3 + bloque.getSize() + 3);
            
            expResult.appendCode(bloque);
            expResult.add(Bytecode.GOTO);
            
            // index = selSize - (expBuff + IFEQ + bloque)
            selSize = selSize - (expBuff.getCode().getSize() + 3 + bloque.getSize() + 3);

            expResult.getCode().addIndex(selSize + 3);  //selSize + GOTO
        }

        code = expResult.getCode();
    }

    public Bytecode getCode() {
        return code;
    }
}
