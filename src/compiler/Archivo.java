/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author usuario
 */
public class Archivo {

    File file;
    BufferedReader reader;
    BufferedWriter writer;
    int line = 1;

    public void abrir(String nombre) {
        file = new File(nombre);
        line = 1;
        
        try {
            if(!file.exists()){
                if (!file.createNewFile()) {
                    System.out.println("Error al intentar abrir archivo: " + nombre);
                }
            }
            
            reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(nombre))));
            writer = new BufferedWriter(new FileWriter(file));
            
        } catch (IOException e) {
            System.out.println("Error de IO");

        }
    }

    public void cerrar() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            System.out.println("Error al cerrar el archivo " + file.getName());
        }
    }

    public void escribir(String input) {

        try {
            writer.write(input);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error en la escritura de archivo, linea " + line);
        }

        line++;
    }

    public char leer() {
        int result = -1;

        try {
            result = reader.read();
        } catch (IOException e) {
            System.out.println("Error en la lectura del archivo, linea " + line);
        }

        if (result == 10 || result == 13) {
            line++;
        }

        return (char) result;
    }

    public String leerLinea() {
        String result = "ggg";
        try {
            result = reader.readLine();
        } catch (IOException e) {
            System.out.println("Error en la lectura del archivo, linea " + line);
        }

        line++;

        return result;
    }
    /*public boolean finDeArchivo(){
    return 
    }*/
}
