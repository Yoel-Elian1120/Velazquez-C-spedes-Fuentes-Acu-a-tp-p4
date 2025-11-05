package juego;

import java.awt.Image;
import java.awt.Color;
import entorno.Entorno;
import entorno.Herramientas;

public class ZombieColosal {

    private double x;
    private double y; // Posición Y (siempre será el centro de la pantalla)
    private double velocidad;
    private int vida;
    
    private Image imagen;
    private double escala;
    private double ancho; // Ancho de la hitbox
    private double alto; // Alto de la hitbox

    public ZombieColosal(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocidad = 0.2; // Es un poco más lento (ajusta este valor)
        this.vida = 50;       // Cuesta mucho derribarlo (ajusta este valor)
        
        // El usuario pidió usar "zombie_principal2.png"
        // Si tu zombie normal se llama "zombie.png", usa ese.
        this.imagen = Herramientas.cargarImagen("recursos/z.png"); 
        
        // Lógica para "estirar" la imagen a 5 filas
        int altoDeseado = 500; // 5 filas * 100px/fila = 500
        
        if (this.imagen != null) {
            int altoOriginal = this.imagen.getHeight(null);
            if (altoOriginal > 0) {
                this.escala = (double) altoDeseado / altoOriginal;
            } else {
                this.escala = 1.0;
            }
            this.ancho = this.imagen.getWidth(null) * this.escala;
            this.alto = altoDeseado;
        } else {
            // Fallback si no carga la imagen
            this.escala = 1.0;
            this.ancho = 100;
            this.alto = 500;
        }
    }
    
    public void mover() {
        this.x -= this.velocidad;
    }
    
    public void dibujar(Entorno e) {
        if (this.imagen != null) {
            e.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);
        } else {
            // Fallback si no carga
            e.dibujarRectangulo(this.x, this.y, this.ancho, this.alto, 0, Color.MAGENTA);
        }
        
        // (Opcional) Dibujar barra de vida del jefe
        double porcentajeVida = (double) this.vida / 50;
        e.dibujarRectangulo(this.x, this.y - (this.alto / 2) - 10, this.ancho * porcentajeVida, 10, 0, Color.RED);
    }
    
    /**
     * El jefe recibe un disparo. Devuelve true si muere.
     */
    public boolean recibirDisparo() {
        this.vida--;
        return this.vida <= 0;
    }
    
    // Getters para colisiones
    public double getX() {
        return this.x;
    }
    
    public double getY() {
        return this.y;
    }
    
    public double getAncho() {
        return this.ancho;
    }
}