package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;
import java.util.Random;

/**
 * Representa al enemigo "Zombie Grinch".
 * Esta clase maneja el movimiento, la vida y los diferentes tipos
 * de zombies (Normal y Tanque - Opcional).
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class ZombieGrinch {
    private double x;
    private double y;
    private int fila;
    private double velocidad;
    private int vida;
    private Image imagen;
    private double escala;

    // --- Atributos para el disparo (Opcional 1) ---
    private long proximoDisparo; // Marca de tiempo del próximo disparo
    private long COOLDOWN_DISPARO = 5000; // 5 segundos
    private Random rand = new Random();
    // --- FIN ---

    private final int ALTO_CELDA = 100;

    /**
     * Constructor del ZombieGrinch.
     * Recibe un 'tipo' para determinar si es "Normal" o "Tanque".
     *
     * @param x    Posición X inicial (fuera de pantalla).
     * @param y    Posición Y (centro de la fila).
     * @param fila Fila (0-4) por la que avanzará.
     * @param tipo Tipo de zombie (0 = Normal, 1 = Tanque).
     */
    public ZombieGrinch(double x, double y, int fila, int tipo) {
        this.x = x;
        this.y = y;
        this.fila = fila;

        String nombreImagen; // Variable para guardar el nombre del archivo

        // --- Lógica para Tipos de Zombie (Opcional 4) ---
        if (tipo == 1) { // Tipo 1: Tanque
            this.velocidad = 0.25; // Más lento
            this.vida = 5; // Más resistente
            nombreImagen = "recursos/principal2.png"; // Imagen de Tanque
        } else { // Tipo 0: Normal (por defecto)
            this.velocidad = 0.5; // Velocidad normal
            this.vida = 2; // Resistencia normal (Req 6)
            nombreImagen = "recursos/zombie_grinch.png"; // Imagen Normal
        }
        // --- FIN LÓGICA ---

        this.imagen = Herramientas.cargarImagen(nombreImagen);

        // Reescalado (para que mida 90% del alto de la celda)
        if (this.imagen != null) {
            int altoOriginal = this.imagen.getHeight(null);
            this.escala = (double) ALTO_CELDA / altoOriginal * 0.9;
        } else {
            this.escala = 1.0;
        }

        // Temporizador de disparo (ambos tipos de zombie disparan)
        // El primer disparo es aleatorio (entre 3 y 7 segundos)
        this.proximoDisparo = System.currentTimeMillis() + 3000 + rand.nextInt(4000);
    }

    /**
     * Mueve el zombie hacia la izquierda (Req 5).
     * Es llamado desde Juego.java en 'actualizarZombies()'.
     */
    public void mover() {
        this.x -= this.velocidad;
    }

    /**
     * Detiene al zombie (Req 4 - colisión con WallNut).
     * Se llama desde Juego.java cuando colisiona con una WallNut.
     * Suma la velocidad para anular el movimiento de este tick.
     */
    public void detenerse() {
        this.x += this.velocidad;
    }

    /**
     * Dibuja el zombie en el entorno.
     */
    public void dibujar(Entorno entorno) {
        entorno.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);
    }

    /**
     * Reduce la vida del zombie al ser golpeado (Req 6).
     * * @return true si el zombie murió (vida <= 0), false si no.
     */
    public boolean recibirDisparo() {
        this.vida--;
        return this.vida <= 0;
    }

    /**
     * Comprueba si el zombie debe disparar en este tick (Opcional 1).
     * * @param tiempoActual El tiempo actual (System.currentTimeMillis()).
     * @return Un nuevo objeto BolaDeNieve si dispara, o null si no.
     */
    public BolaDeNieve intentarDisparar(long tiempoActual) {
        if (tiempoActual > this.proximoDisparo) {
            // Reinicia el cooldown
            this.proximoDisparo = tiempoActual + COOLDOWN_DISPARO + rand.nextInt(2000); // 5s + (0-2s)
            
            // Crea y devuelve la bola de nieve
            return new BolaDeNieve(this.x, this.y, this.fila);
        }
        return null; // No es tiempo de disparar
    }

    // --- Getters (necesarios para colisiones y lógica) ---
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