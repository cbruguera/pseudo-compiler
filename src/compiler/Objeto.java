/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import java.util.HashMap;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;
import static compiler.Util.declaring;
/**
 * NO SE USA
 * @author Carlos Arturo
 */
public class Objeto extends Variable {
    HashMap<String, Variable> fields;

    public Objeto(CtClass t, int varForm) {
        form = varForm;
        baseType = t;
        descriptor = Descriptor.of(t);

        loadCode = new Bytecode(declaring.getConstPool());
        storeCode = new Bytecode(declaring.getConstPool());
        preStoreCode = new Bytecode(declaring.getConstPool());
    }

    
}
