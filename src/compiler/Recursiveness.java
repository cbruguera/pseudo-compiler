/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

/**
 *
 * @author usuario
 */
public class Recursiveness {
    public int factorial(int n){
        if(n <= 1){
            return 1;
        }else{
            return n * factorial(n - 1);
        }
    }
}
