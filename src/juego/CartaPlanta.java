package juego;

import java.awt.Color;
import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

/**
 * Representa una carta (avatar) en el HUD (Req 1).
 * Su principal responsabilidad es gestionar el tiempo de recarga
 * (cooldown) para plantar (Req 2).
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class CartaPlanta {
    private double x;
    private double y;
    private String tipo; // "rosablade", "wallnut", "explosiva"
    private long cooldown; // Duración del cooldown en milisegundos
    private long ultimoUso; // Marca de tiempo de la última vez que se usó
    private Image imagen;
    private double escala;

    // Constantes de la tarjeta (para hitbox y dibujo)
    private final int ANCHO_CARTA = 80;
    private final int ALTO_CARTA = 100;

    /**
     * Constructor de la CartaPlanta.
     *
     * @param x        Posición X en el HUD.
     * @param y        Posición Y en el HUD.
     * @param tipo     Un string que identifica el tipo de planta.
     * @param cooldown El tiempo (en ms) que tarda en recargar.
     * @param img      La ruta a la imagen de la carta.
     */
    public CartaPlanta(double x, double y, String tipo, long cooldown, String img) {
        this.x = x;
        this.y = y;
        this.tipo = tipo;
        this.cooldown = cooldown;
        // Inicia lista para usarse: Se simula que el último uso fue hace
        // 'cooldown' milisegundos en el pasado.
        this.ultimoUso = System.currentTimeMillis() - cooldown;
        this.imagen = Herramientas.cargarImagen(img);
        this.calcularEscala();
    }

    /**
     * Calcula la escala para que la imagen de la carta se ajuste
     * a las dimensiones fijas (80x100).
     */
    private void calcularEscala() {
        if (this.imagen == null) {
            this.escala = 1.0;
            return;
        }
        int anchoOriginal = this.imagen.getWidth(null);
        int altoOriginal = this.imagen.getHeight(null);
        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            this.escala = 1.0;
            return;
        }

        double escalaAncho = (double) ANCHO_CARTA / anchoOriginal;
        double escalaAlto = (double) ALTO_CARTA / altoOriginal;
        this.escala = Math.min(escalaAncho, escalaAlto) * 0.9; // 90% para margen
    }

    /**
     * Dibuja la carta y su barra de recarga (si no está lista).
     */
    public void dibujar(Entorno entorno, long tiempoActual) {

        entorno.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);

        // --- Lógica de la barra de recarga ---
        if (!estaLista(tiempoActual)) {
            // 1. Calcula qué porcentaje de la recarga se ha completado
            long tiempoPasado = tiempoActual - this.ultimoUso;
            double porcentajeRecarga = (double) tiempoPasado / this.cooldown;
            if (porcentajeRecarga > 1.0) porcentajeRecarga = 1.0;

            // 2. Dibuja un "velo" oscuro semitransparente sobre la carta
            Color velo = new Color(0, 0, 0, 150); // Negro con 150 de alfa
            entorno.dibujarRectangulo(this.x, this.y, ANCHO_CARTA, ALTO_CARTA, 0, velo);

            // 3. Dibuja la barra verde de progreso (de abajo hacia arriba)
            double altoBarra = ALTO_CARTA * porcentajeRecarga;
            // Posición Y de la barra (se alinea abajo y "crece" hacia arriba)
            double yBarra = this.y + (ALTO_CARTA / 2.0) - (altoBarra / 2.0);

            entorno.dibujarRectangulo(this.x, yBarra, ANCHO_CARTA, altoBarra, 0, Color.GREEN);
        }
    }

    /**
     * Verifica si el clic del mouse (mx, my) cayó dentro de la hitbox de la carta.
     * * @return true si fue clickeada, false si no.
     */
    public boolean fueClickeada(int mx, int my) {
        return (mx > this.x - ANCHO_CARTA / 2 && mx < this.x + ANCHO_CARTA / 2 &&
                my > this.y - ALTO_CARTA / 2 && my < this.y + ALTO_CARTA / 2);
    }

    /**
     * Verifica si la carta está lista para usarse (si ya pasó el cooldown).
     */
    public boolean estaLista(long tiempoActual) {
        return (tiempoActual - this.ultimoUso) >= this.cooldown;
    }

    /**
     * Marca el tiempo actual como el 'ultimoUso'.
     * Esto inicia el contador del cooldown.
     */
    public void iniciarRecarga(long tiempoActual) {
        this.ultimoUso = tiempoActual;
    }

    // --- Getters ---
    public String getTipo() {
        return this.tipo;
    }

    public Image getImagen() {
        return this.imagen;
    }

    // --- Métodos para Opcional 5 (Items) ---

    /**
     * Acelera la recarga (reduce el cooldown en un 20%).
     * Es llamado por Juego.java cuando se agarra un Item bueno.
     */
    public void acelerarRecarga() {
        this.cooldown = (long) (this.cooldown * 0.80); // Reduce el tiempo un 20%

        // Límite de seguridad: la recarga no puede ser menor a 1 segundo
        if (this.cooldown < 1000) {
            this.cooldown = 1000;
        }
    }

    /**
     * Desacelera la recarga (aumenta el cooldown en un 20%).
     * Es llamado por Juego.java cuando se agarra un Item malo.
     */
    public void desacelerarRecarga() {
        this.cooldown = (long) (this.cooldown * 1.20); // Aumenta el tiempo un 20%
    }
}