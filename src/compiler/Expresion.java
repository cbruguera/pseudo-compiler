/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.*;

import static compiler.Util.declaring;
import static compiler.Util.FLOAT_TYPE;
import static parser.Pseudo.getToken;
/**
 * Clase que maneja todas las definiciones y operaciones de una expresión
 * @author Carlos Arturo
 */
public class Expresion {

    protected Bytecode code;
    protected CtClass type;
    protected boolean isVariable = false;
    protected Variable variable = null;

    public Expresion() {
        this.code = new Bytecode(declaring.getConstPool());
        type = CtClass.voidType;
    }

    public Expresion(CtClass t) {
        this.code = new Bytecode(declaring.getConstPool());
        type = (t != null) ? t : CtClass.voidType;
    }

    public Expresion(Expresion exp2) {
        code = (Bytecode) exp2.getCode().clone();
        type = exp2.getType();
    }

    public void setVariable(boolean val) {
        isVariable = val;
    }

    public boolean isVariable() {
        return isVariable;
    }

    public Bytecode getCode() {
        return code;
    }

    public Bytecode getCopyCode() {
        return (Bytecode) code.clone();
    }

    public CtClass getType() {
        return type;
    }

    public void setType(CtClass t) {
        type = t;
    }

    public boolean isType(CtClass type) {
        if (type.getName().equals(this.type.getName())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adjunta un segmento de bytecode nuevo al final del código actual
     * @param nuevo objeto de tipo Bytecode a añadir
     */
    public void appendCode(Bytecode nuevo) {
        byte[] bytes = nuevo.get();

        for (int i = 0; i < bytes.length; i++) {
            code.add(bytes[i]);
        }
    }

    /**
     * Adjunta un segmento de bytecode nuevo al comienzo del código actual
     * @param nuevo 
     */
    public void preAppendCode(Bytecode nuevo) {
        byte[] newBytes = nuevo.get();
        byte[] thisBytes = this.code.get();

        code = new Bytecode(declaring.getConstPool());

        for (int i = 0; i < newBytes.length; i++) {
            code.add(newBytes[i]);
        }

        for (int i = 0; i < thisBytes.length; i++) {
            code.add(thisBytes[i]);
        }
    }

    /**
     * Adjunta un segmento de bytecode nuevo al final del código actual
     * @param nuevo Arreglo de bytes a añadir
     */
    public void appendCode(byte[] nuevo) {

        for (int i = 0; i < nuevo.length; i++) {
            code.add(nuevo[i]);
        }
    }

    public void add(int instruccion) {
        code.add(instruccion);
    }

    public Variable makeVariable(){
        Variable result = new Variable(type, Variable.LOCAL);
        result.setLoadCode(code);
        
        return result;
    }
    
    /**
     * Aplica un operador unario a la expresión actual
     * @param operator Cadena que define el operador a aplicar
     */
    public void operate(String operator) {
        String desc = Descriptor.of(type);

        if (operator.equals("-")) {
            if (desc.equals("J")) {
                this.add(Bytecode.LNEG);
            } else if (desc.equals("I")) {
                this.add(Bytecode.INEG);
            } else if (desc.equals("D")) {
                this.add(Bytecode.DNEG);
            } else if (desc.equals("F")) {
                this.add(Bytecode.FNEG);
            } else {
                Errors.add("Operación unaria aplicada a un tipo invalido", getToken(0));
            }
        } else if (operator.equals("+")) {
            //NOP
        } else if (operator.equals("!")) {
            if (desc.equals("Z")) {
                this.code.addIconst(1);
                this.add(Bytecode.IXOR);  //XOR con 1 niega el valor booleano
            } else {
                Errors.add("Operación unaria aplicada a un tipo invalido", getToken(0));
            }
        } else {
            Errors.add("Error desconocido en la aplicacion de una operacion unaria: \"" + operator + "\"", getToken(0));
        }
    }

    /**
     * Aplica una operación aritmética
     * binaria a la expresión actual, recibe una operación (string) 
     * y otra expresión como parámetros.
     * 
     * @param operator  Operador a aplicar
     * @param exp2 Segunda expresión como operando
     */
    public void operate(String operator, Expresion exp2) {
        
        String desc1 = Descriptor.of(this.getType());
        String desc2 = Descriptor.of(exp2.getType());
        
        if (operator.equals("*")) {
            if (desc1.equals("J")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LMUL);
                    this.setType(CtClass.longType);
                } else if (desc2.equals("D")) {
                    this.add(Bytecode.L2D); //Convierte la expr1 a float
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DMUL);
                    this.setType(CtClass.doubleType);
                }  else {
                    Errors.add("Operador \"*\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("I")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IMUL);
                    this.setType(CtClass.intType);
                } else if (desc2.equals("F")) {
                    this.add(Bytecode.I2F); //Convierte la expr1 a float
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FMUL);
                    this.setType(CtClass.floatType);
                } else {
                    Errors.add("Operador \"*\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("D")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D); //Convierte la expr1 a float
                    this.add(Bytecode.DMUL);
                    this.setType(CtClass.doubleType);
                } else if (desc2.equals("D")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DMUL);
                    this.setType(CtClass.doubleType);
                } else {
                    Errors.add("Operador \"*\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("F")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2F); //Convierte la expr1 a float
                    this.add(Bytecode.FMUL);
                    this.setType(CtClass.floatType);
                } else if (desc2.equals("F")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FMUL);
                    this.setType(CtClass.floatType);
                } else {
                    Errors.add("Operador \"*\" aplicado a tipos incompatibles", getToken(0));
                }
            } else {
                Errors.add("Operador \"*\" aplicado a tipos incompatibles", getToken(0));
            }
        }

        if (operator.toLowerCase().equals("mod")) {
            if (desc1.equals("J") && desc2.equals(desc1)) {
                this.appendCode(exp2.getCode());
                this.add(Bytecode.LREM);
            } else if (desc1.equals("I") && desc2.equals(desc1)) {
                this.appendCode(exp2.getCode());
                this.add(Bytecode.IREM);
            } else {
                Errors.add("Operador \"MOD\" aplicado a tipos incompatibles", getToken(0));
            }
        }

        if (operator.toLowerCase().equals("div")) {
            if (desc1.equals("J") && desc2.equals(desc1)) {
                this.appendCode(exp2.getCode());
                this.add(Bytecode.LDIV);
            } else if (desc1.equals("I") && desc2.equals(desc1)) {
                this.appendCode(exp2.getCode());
                this.add(Bytecode.IDIV);
            } else {
                Errors.add("Operador \"DIV\" aplicado a tipos incompatibles", getToken(0));
            }
        }

        if (operator.equals("/")) {
            if (desc1.equals("J")) {
                this.add(Bytecode.L2D);
            } else if (desc1.equals("I")) {
                this.add(Bytecode.I2F);
            } else if (desc1.equals("D")) {
               
            } else if (desc1.equals("F")) {
               
            } else {
                Errors.add("Operador \"/\" aplicado a tipos incompatibles", getToken(0));
            }

            this.appendCode(exp2.getCode());    

            if (desc2.equals("J")) {
                this.add(Bytecode.L2D);
       
            } else if (desc2.equals("I")) {
                this.add(Bytecode.I2F);
         
            } else if (desc2.equals("D")) {
   
            } else if (desc2.equals("F")) {
                
            } else {
                Errors.add("Operador \"/\" aplicado a tipos incompatibles", getToken(0));
            }

            this.setType(FLOAT_TYPE);

            if (Descriptor.of(FLOAT_TYPE).equals("D")) {
                this.add(Bytecode.DDIV);
            } else if (Descriptor.of(FLOAT_TYPE).equals("F")) {
                this.add(Bytecode.FDIV);
            }
        }

        if (operator.equals("^")) {
            if (desc1.equals("J")) {
                this.add(Bytecode.L2D);
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D);
                } else if (desc2.equals("D")) {
                } else {
                    Errors.add("Operador \"^\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("I")) {
                this.add(Bytecode.I2D);
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2D);
                } else if (desc2.equals("F")) {
                } else {
                    Errors.add("Operador \"^\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("D")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D);
                } else if (desc2.equals("D")) {
                    this.appendCode(exp2.getCode());
                } else {
                    Errors.add("Operador \"^\" aplicado a tipos incompatibles", getToken(0));
                }


            } else if (desc1.equals("F")) {
                this.add(Bytecode.F2D);
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2D);
                } else if (desc2.equals("F")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.F2D);
                } else {
                    Errors.add("Operador \"^\" aplicado a tipos incompatibles", getToken(0));
                }
            } else {
                Errors.add("Operador \"^\" aplicado a tipos incompatibles", getToken(0));
            }

            Bytecode bc = new Bytecode(declaring.getConstPool());
            try {
                CtClass[] classArr = {CtClass.doubleType, CtClass.doubleType};
                bc.addInvokestatic(ClassPool.getDefault().get("java.lang.Math"), "pow", 
                        Descriptor.ofMethod(CtClass.doubleType, classArr));
                
                if(Descriptor.of(FLOAT_TYPE).equals("F")){
                    bc.add(Bytecode.D2F);
                }
                this.appendCode(bc);    // POW!!!
                
            } catch (NotFoundException e) {
                Errors.add("Error al ejecutar operacion de potenciacion", getToken(0));
            }
        }

        if (operator.equals("+")) {
            if (desc1.equals("J")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LADD);
                } else if (desc2.equals("D")) {
                    this.add(Bytecode.L2D);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DADD);
                    this.setType(CtClass.doubleType);
                } else { 
                    Errors.add("Operador \"+\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("I")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IADD);
                } else if (desc2.equals("F")) {
                    this.add(Bytecode.I2F);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FADD);
                    this.setType(CtClass.floatType);
                } else {
                    Errors.add("Operador \"+\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("D")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D);
                    this.add(Bytecode.DADD);
                    this.setType(CtClass.doubleType);
                } else if (desc2.equals("D")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DADD);
                    this.setType(CtClass.doubleType);
                } else {
                    Errors.add("Operador \"+\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("F")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2F);
                    this.add(Bytecode.FADD);
                    this.setType(CtClass.floatType);
                } else if (desc2.equals("F")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FADD);
                    this.setType(CtClass.floatType);
                } else {
                    Errors.add("Operador \"+\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("Ljava/lang/String;")) {
                Expresion aux = new Expresion();

                aux.getCode().addNew("java.lang.StringBuilder");
                aux.add(Bytecode.DUP);
                aux.getCode().addInvokespecial("java.lang.StringBuilder", "<init>", "()V");
                aux.appendCode(this.code);
                aux.getCode().addInvokevirtual("java.lang.StringBuilder", "append", "(" 
                        + desc1 + ")Ljava/lang/StringBuilder;");
                aux.appendCode(exp2.getCode());
                aux.getCode().addInvokevirtual("java.lang.StringBuilder", "append", "(" 
                        + desc2 + ")Ljava/lang/StringBuilder;");
                aux.getCode().addInvokevirtual("java.lang.StringBuilder", "toString", 
                        "()Ljava/lang/String;");
                this.code = aux.getCode();

            } else {
                Errors.add("Operador \"+\" aplicado a tipos incompatibles", getToken(0));
            }
        }

        if (operator.equals("-")) {
            if (desc1.equals("J")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LSUB);
                } else if (desc2.equals("D")) {
                    this.add(Bytecode.L2D);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DSUB);
                    this.setType(CtClass.doubleType);
                } else {
                    Errors.add("Operador \"-\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("I")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.ISUB);
                } else if (desc2.equals("F")) {
                    this.add(Bytecode.I2F);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FSUB);
                    this.setType(CtClass.floatType);
                } else {
                    Errors.add("Operador \"-\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("D")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D);
                    this.add(Bytecode.DSUB);
                    this.setType(CtClass.doubleType);
                } else if (desc2.equals("D")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DSUB);
                    this.setType(CtClass.doubleType);
                } else {
                    Errors.add("Operador \"-\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("F")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2F);
                    this.add(Bytecode.FSUB);
                    this.setType(CtClass.floatType);
                } else if (desc2.equals("F")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FSUB);
                    this.setType(CtClass.floatType);
                } else {
                    Errors.add("Operador \"-\" aplicado a tipos incompatibles", getToken(0));
                }
            } else {
                Errors.add("Operador \"-\" aplicado a tipos incompatibles", getToken(0));
            }
        }
    }

    /**
     * Aplica un operador binario de comparación a la expresión actual.
     * 
     * @param operator String de operador a aplicar
     * @param exp2 Segunda expresión como operando
     */
    public void compare(String operator, Expresion exp2) {
        String desc1 = Descriptor.of(type);
        String desc2 = Descriptor.of(exp2.getType());

        if (operator.equals("<")) {
            if (desc1.equals("J")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("D")) {
                    this.add(Bytecode.L2D);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2L);
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"<\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("I")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.add(Bytecode.I2F);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"<\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("D")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D);
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("D")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"<\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("F")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2F);
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"<\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("C")) {
                if (desc2.equals("C") || desc2.equals("I")) {

                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("J")) {
                    this.add(Bytecode.I2L);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFLT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                } else {
                    Errors.add("Operador \"<\" aplicado a tipos incompatibles", getToken(0));
                }
            } else {
                Errors.add("Operador \"<\" aplicado a tipos incompatibles", getToken(0));
            }
        }

        if (operator.equals(">")) {
            if (desc1.equals("J")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("D")) {
                    this.add(Bytecode.L2D);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2L);
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \">\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("I")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.add(Bytecode.I2F);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \">\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("D")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D);
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("D")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \">\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("F")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2F);
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \">\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("C")) {
                if (desc2.equals("C") || desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("J")) {
                    this.add(Bytecode.I2L);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFGT);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                    
                } else {
                    Errors.add("Operador \"<=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else {
                Errors.add("Operador \">\" aplicado a tipos incompatibles", getToken(0));
            }
        }

        if (operator.equals("<=")) {
            if (desc1.equals("J")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("D")) {
                    this.add(Bytecode.L2D);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2L);
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"<=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("I")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.add(Bytecode.I2F);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"<=\" aplicado a tipos incompatibles", getToken(0));
                }
                
            } else if (desc1.equals("D")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D);
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("D")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"<=\" aplicado a tipos incompatibles", getToken(0));
                }
                
            } else if (desc1.equals("F")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2F);
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"<=\" aplicado a tipos incompatibles", getToken(0));
                }
                
            } else if (desc1.equals("C")) {
                if (desc2.equals("C") || desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                } else if (desc2.equals("J")) {
                    this.add(Bytecode.I2L);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFLE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                } else {
                    Errors.add("Operador \"<=\" aplicado a tipos incompatibles", getToken(0));
                }
                
            } else {
                Errors.add("Operador \"<=\" aplicado a tipos incompatibles", getToken(0));
            }
        }

        if (operator.equals(">=")) {
            if (desc1.equals("J")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("D")) {
                    this.add(Bytecode.L2D);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2L);
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \">=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("I")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.add(Bytecode.I2F);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \">=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("D")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D);
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                    // END of comparison
                } else if (desc2.equals("D")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                    // END of comparison
                } else {
                    Errors.add("Operador \">=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("F")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2F);
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \">=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("C")) {
                if (desc2.equals("C") || desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("J")) {
                    this.add(Bytecode.I2L);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFGE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                } else {
                    Errors.add("Operador \">=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else {
                Errors.add("Operador \">=\" aplicado a tipos incompatibles", getToken(0));
            }
        }

        if (operator.equals("==")) {
            if (desc1.equals("J")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("D")) {
                    this.add(Bytecode.L2D);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2L);
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"==\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("I")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.add(Bytecode.I2F);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"==\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("D")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D);
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("D")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"==\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("F")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2F);
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"==\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("Ljava/lang/String;") && desc2.equals(desc1)) {

                String descEq = "(Ljava/lang/Object;)Z";

                if (this.isVariable()) {
                    this.appendCode(exp2.getCode());
                } else {
                    Bytecode bc = new Bytecode(declaring.getConstPool());

                    //instanciar nuevo String
                    bc.addNew("java.lang.String");
                    bc.add(Bytecode.DUP);
                    this.preAppendCode(bc);
                    this.getCode().addInvokespecial("java.lang.String", "<init>", "(Ljava/lang/String;)V");
                    this.appendCode(exp2.getCode());
                }

                this.getCode().addInvokevirtual("java.lang.String", "equals", descEq);

            } else if (desc1.equals("C")) { //caracter
                if (desc2.equals("C") || desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("J")) {
                    this.add(Bytecode.I2L);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFEQ);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                } else {
                    Errors.add("Operador \"==\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("Z") && desc1.equals(desc2)) { //logico
                this.appendCode(exp2.getCode());
                this.add(Bytecode.IF_ICMPEQ);
                this.code.addIndex(7);
                this.code.addIconst(0); // false
                this.add(Bytecode.GOTO);
                this.code.addIndex(4);
                this.code.addIconst(1); // true

            } else if (desc1.equals("V")) {
                this.appendCode(exp2.getCode());
                this.add(Bytecode.IFNULL);
                this.code.addIndex(7);
                this.code.addIconst(0); // false
                this.add(Bytecode.GOTO);
                this.code.addIndex(4);
                this.code.addIconst(1); // true
            } else if (desc2.equals("V")) {

                this.add(Bytecode.IFNULL);
                this.code.addIndex(7);
                this.code.addIconst(0); // false
                this.add(Bytecode.GOTO);
                this.code.addIndex(4);
                this.code.addIconst(1); // true
            } else {
                Errors.add("Operador \"==\" aplicado a tipos incompatibles", getToken(0));
            }
        }

        if (operator.equals("!=")) {
            if (desc1.equals("J")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("D")) {
                    this.add(Bytecode.L2D);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2L);
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"!=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("I")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("F")) {
                    this.add(Bytecode.I2F);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else if (desc2.equals("C")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true

                } else {
                    Errors.add("Operador \"!=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("D")) {
                if (desc2.equals("J")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.L2D);
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                    // END of comparison
                } else if (desc2.equals("D")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.DCMPL);
                    this.add(Bytecode.IFNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                    // END of comparison
                } else {
                    Errors.add("Operador \"!=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("F")) {
                if (desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.I2F);
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                    // END of comparison
                } else if (desc2.equals("F")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.FCMPL);
                    this.add(Bytecode.IFNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                    // END of comparison
                } else {
                    Errors.add("Operador \"!=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("Ljava/lang/String;") && desc2.equals(desc1)) {

                String descEq = "(Ljava/lang/Object;)Z";

                if (this.isVariable()) {
                    this.appendCode(exp2.getCode());
                } else {
                    Bytecode bc = new Bytecode(declaring.getConstPool());

                    //instanciar nuevo String
                    bc.addNew("java.lang.String");
                    bc.add(Bytecode.DUP);
                    this.preAppendCode(bc);
                    this.getCode().addInvokespecial("java.lang.String", "<init>", "(Ljava/lang/String;)V");
                    this.appendCode(exp2.getCode());
                }

                this.getCode().addInvokevirtual("java.lang.String", "equals", descEq);
                this.getCode().addIconst(1);
                this.add(Bytecode.IXOR);
            } else if (desc1.equals("C")) { //caracter
                if (desc2.equals("C") || desc2.equals("I")) {
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.IF_ICMPNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                } else if (desc2.equals("J")) {
                    this.add(Bytecode.I2L);
                    this.appendCode(exp2.getCode());
                    this.add(Bytecode.LCMP);
                    this.add(Bytecode.IFNE);
                    this.code.addIndex(7);
                    this.code.addIconst(0); // false
                    this.add(Bytecode.GOTO);
                    this.code.addIndex(4);
                    this.code.addIconst(1); // true
                } else {
                    Errors.add("Operador \"!=\" aplicado a tipos incompatibles", getToken(0));
                }
            } else if (desc1.equals("Z") && desc1.equals(desc2)) { //logico
                this.appendCode(exp2.getCode());
                this.add(Bytecode.IF_ICMPNE);
                this.code.addIndex(7);
                this.code.addIconst(0); // false
                this.add(Bytecode.GOTO);
                this.code.addIndex(4);
                this.code.addIconst(1); // true
            } else if (desc1.equals("V")) {
                this.appendCode(exp2.getCode());
                this.add(Bytecode.IFNONNULL);
                this.code.addIndex(7);
                this.code.addIconst(0); // false
                this.add(Bytecode.GOTO);
                this.code.addIndex(4);
                this.code.addIconst(1); // true
            } else if (desc2.equals("V")) {

                this.add(Bytecode.IFNONNULL);
                this.code.addIndex(7);
                this.code.addIconst(0); // false
                this.add(Bytecode.GOTO);
                this.code.addIndex(4);
                this.code.addIconst(1); // true
            } else {
                Errors.add("Operador \"!=\" aplicado a tipos incompatibles", getToken(0));
            }
        }

        this.setType(CtClass.booleanType);
    }

        
    /**
     * Functión que obtiene un bytecode encargado de invocar una instrucción de
     * imprimir, para desplegar el valor de la expresión por pantalla.
     *
     * @return Objeto de tipo Bytecode con el código de la impresión
     */
    public Bytecode getPrintln() {
        String desc = new String();

        desc = "(" + Descriptor.of(type) + ")V";

        Expresion buff = new Expresion();


        buff.getCode().addGetstatic("java.lang.System", "out", "Ljava/io/PrintStream;");
        buff.appendCode(code);
        if (Descriptor.of(type).equals("Z")) {

            buff.add(Bytecode.IFEQ);
            //index = IFEQ + LDC + GOTO
            buff.getCode().addIndex(3 + 2 + 3);
            buff.getCode().addLdc("Verdad");
            buff.add(Bytecode.GOTO);
            //index = GOTO + LDC
            buff.getCode().addIndex(3 + 2);
            buff.getCode().addLdc("Falso");

            desc = "(Ljava/lang/String;)V";
        }
        buff.getCode().addInvokevirtual("java.io.PrintStream", "println", desc);    

        return buff.getCode();
    }

    /**
     * Imprime las instrucciones de la secuencia de bytecode de la expresión.
     * Se usa para depurar.
     */
    public void printbc() {
        CodeIterator ci = code.toCodeAttribute().iterator();
        ci.begin();
        int index;

        System.out.println("");
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
     * Obtiene el descriptor del tipo de la expresión actual
     * @return Cadena con el descriptor
     */
    public String getTypeDescriptor() {
        return Descriptor.of(type);
    }
    
    
    /**
     * Función que retorna una copia exacta de la expresión,
     * sin modificar la actual.
     * 
     * @return Copia de la expresións
     */
    @Override
    public Expresion clone() {
        Expresion expc = new Expresion(type);
        expc.appendCode(code.toCodeAttribute().get());
        return expc;
    }

    /**
     * Inicializa el código bytecode como vacío.
     */
    public void clearCode() {
        code = new Bytecode(declaring.getConstPool());
    }
}
