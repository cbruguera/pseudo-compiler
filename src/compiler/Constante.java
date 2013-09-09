/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compiler;

import javassist.CtClass;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;
import static compiler.Util.declaring;

/**
 * Clase que representa un valor constante de cualquier tipo en el código fuente
 * @author Carlos Arturo
 */
public class Constante {
    protected String image;
    CtClass type;
    String descriptor;
    Bytecode code;


    public int ivalue;
    public long lvalue;
    public float fvalue;
    public double dvalue;
    public boolean bvalue;
    public char cvalue;
    public String svalue;

    public Constante(CtClass tipo, String tok){
        this.type = tipo;
        descriptor = Descriptor.of(this.type);
        image = tok;
        code = new Bytecode(declaring.getConstPool());
        generateBytecode();
    }

    public String getImage(){
        return image;
    }

    public CtClass getType(){
        return type;
    }

    public void setType(CtClass t){
        type = t;
        descriptor = Descriptor.of(type);
    }

    public void setImage(String i){
        image = i;
    }

    private void generateBytecode(){
        svalue = image;

        if(descriptor.equals("C")){
            cvalue = image.charAt(1);
            code.addIconst((int) cvalue);
        }
        if(descriptor.equals("D")){
            dvalue = Double.parseDouble(image);
            code.addDconst(dvalue);
        }
        if(descriptor.equals("F")){
            fvalue = Float.parseFloat(image);
            code.addFconst(fvalue);
        }
        if(descriptor.equals("I")){
            ivalue = Integer.parseInt(image);
            code.addIconst(ivalue);
        }
        if(descriptor.equals("J")){
            lvalue = Long.parseLong(image);
            ivalue = Integer.parseInt(image);
            code.addLconst(lvalue);
        }
        if(descriptor.equals("V")){
            // Null
            code.add(Bytecode.ACONST_NULL);
        }
        if(descriptor.equals("Z")){
            if(image.charAt(0) == 'v'){
                bvalue = true;
                code.addIconst(1);
            }else{
                bvalue = false;
                code.addIconst(0);
            }
        }
        if(descriptor.equals("Ljava/lang/String;"))
        {
            svalue = new String(image);
            code.addLdc(svalue);
        }
    }

    public Bytecode getCode(){
        return code;
    }

}
