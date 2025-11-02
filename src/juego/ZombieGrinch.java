package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;
import java.util.Random;

public class ZombieGrinch {
    private double x;
    private double y;
    private int fila;
    private double velocidad;
    private int vida;
    private Image imagen;
    private double escala;
    
    // --- Atributos para el disparo ---
    private long proximoDisparo;
    private long COOLDOWN_DISPARO = 5000; // 5 segundos entre disparos
    private Random rand = new Random();
    // --- FIN DE LO NUEVO ---

    // Constantes (deben coincidir con Juego.java)
    private final int ALTO_CELDA = 100;

    // <-- MODIFICADO: El constructor ahora recibe 'tipo' en lugar de 'vidaInicial' -->
    public ZombieGrinch(double x, double y, int fila, int tipo) {
        this.x = x;
        this.y = y;
        this.fila = fila;
        
        String nombreImagen; // Variable para guardar el nombre del archivo

        // <-- Lógica para Tipos de Zombie -->
        if (tipo == 1) { // Tipo 1: Tanque (principal2.png)
            this.velocidad = 0.25; // Menos velocidad (0.5 era el normal)
            this.vida = 5;         // Más resistencia (2 era el normal)
            nombreImagen = "recursos/principal2.png";
        } else { // Tipo 0: Normal (por defecto)
            this.velocidad = 0.5; // Velocidad normal
            this.vida = 2;         // Resistencia normal
            nombreImagen = "recursos/zombie_grinch.png";
        }
        // --- FIN LÓGICA ---

        this.imagen = Herramientas.cargarImagen(nombreImagen); // <-- Carga la imagen correcta
        
        // Reescalado (funciona para cualquier imagen)
        if (this.imagen != null) {
            int altoOriginal = this.imagen.getHeight(null);
            this.escala = (double) ALTO_CELDA / altoOriginal * 0.9; // 90% del alto celda
        } else {
            this.escala = 1.0;
        }

        // Temporizador de disparo (ambos tipos de zombie disparan)
        this.proximoDisparo = System.currentTimeMillis() + 3000 + rand.nextInt(4000);
    }
    // --- FIN DE LA MODIFICACIÓN ---

    /**
     * Mueve el zombie hacia la izquierda.
     */
    public void mover() {
        this.x -= this.velocidad;
    }

    /**
     * Detiene al zombie.
     * Se llama desde Juego.java cuando colisiona con una WallNut.
     * Deshace el movimiento de este tick (this.x -= velocidad) 
     * sumando la velocidad, para que el efecto neto sea 0.
     */
    public void detenerse() {
        this.x += this.velocidad;
    }
    
    /**
     * Dibuja el zombie.
     */
    public void dibujar(Entorno entorno) {
        entorno.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);
    }

    /**
     * Reduce la vida del zombie al ser golpeado.
     * @return true si el zombie murió (vida <= 0), false si no.
     */
    public boolean recibirDisparo() {
        this.vida--;
        return this.vida <= 0;
    }

    // --- Método que comprueba si debe disparar ---
    /**
     * Comprueba si el zombie debe disparar en este tick.
     * @param tiempoActual El tiempo actual (System.currentTimeMillis())
     * @return Un nuevo objeto BolaDeNieve si dispara, o null si no.
     */
    public BolaDeNieve intentarDisparar(long tiempoActual) {
        
        // NOTA: Tal como está tu lógica, el zombie disparará incluso
        // si está "detenido" comiendo una nuez, porque 'detenerse()'
        // solo revierte la posición, no cambia un estado de "comiendo".
        // ¡Esto es aceptable como mecánica de juego!
        
        if (tiempoActual > this.proximoDisparo) {
            // Reinicia el cooldown
            this.proximoDisparo = tiempoActual + COOLDOWN_DISPARO;
            
            // Crea y devuelve la bola de nieve.
            return new BolaDeNieve(this.x, this.y, this.fila);
        }
        
        return null; // No es tiempo de disparar
    }
    
    
    // --- Getters ---
    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public int getFila() {
        return this.fila;
    }
}