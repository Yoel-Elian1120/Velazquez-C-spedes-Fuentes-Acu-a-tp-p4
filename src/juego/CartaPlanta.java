package juego;

import java.awt.Color;
import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

public class CartaPlanta {
    private double x;
    private double y;
    private String tipo;
    private long cooldown; // Duración del cooldown en milisegundos
    private long ultimoUso;
    private Image imagen;
    
    // Constantes de la tarjeta
    private final int ANCHO_CARTA = 80;
    private final int ALTO_CARTA = 100;

    private double escala;

    public CartaPlanta(double x, double y, String tipo, long cooldown, String img) {
        this.x = x;
        this.y = y;
        this.tipo = tipo;
        this.cooldown = cooldown;
        this.ultimoUso = -cooldown; // Inicia lista para usarse
        
        this.imagen = Herramientas.cargarImagen(img); 
        
        this.calcularEscala();
    }
    
    private void calcularEscala() {
        if (this.imagen == null) {
            this.escala = 1.0;
            return;
        }
        
        int anchoOriginal = this.imagen.getWidth(null);
        int altoOriginal = this.imagen.getHeight(null);

        double escalaAncho = (double) ANCHO_CARTA / anchoOriginal;
        double escalaAlto = (double) ALTO_CARTA / altoOriginal;
        
        this.escala = Math.min(escalaAncho, escalaAlto) * 0.9; // 90% para margen
    }


    public void dibujar(Entorno entorno, long tiempoActual) {
        
        entorno.dibujarImagen(this.imagen, this.x, this.y, 0, this.escala);

        // Lógica de la barra de recarga
        if (!estaLista(tiempoActual)) {
            long tiempoPasado = tiempoActual - this.ultimoUso;
            double porcentajeRecarga = (double) tiempoPasado / this.cooldown;

            Color velo = new Color(0, 0, 0, 150);
            entorno.dibujarRectangulo(this.x, this.y, ANCHO_CARTA, ALTO_CARTA, 0, velo);
            
            double altoBarra = ALTO_CARTA * porcentajeRecarga;
            double yBarra = this.y + (ALTO_CARTA / 2.0) - (altoBarra / 2.0);
            
            entorno.dibujarRectangulo(this.x, yBarra, ANCHO_CARTA, altoBarra, 0, Color.GREEN);
        }
    }

    public boolean fueClickeada(int mx, int my) {
        return (mx > this.x - ANCHO_CARTA / 2 && mx < this.x + ANCHO_CARTA / 2 &&
                my > this.y - ALTO_CARTA / 2 && my < this.y + ALTO_CARTA / 2);
    }

    public boolean estaLista(long tiempoActual) {
        return (tiempoActual - this.ultimoUso) >= this.cooldown;
    }

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
    
    public double getEscala() {
        return this.escala;
    }
    
    // <-- NUEVO (Opcional 5: Items) -->
    /**
     * Acelera la recarga (reduce el cooldown en un 20%).
     */
    public void acelerarRecarga() {
        // Reduce el tiempo de cooldown (ej: 5000ms -> 4000ms)
        this.cooldown = (long)(this.cooldown * 0.80); 
        
        // Límite de seguridad: la recarga no puede ser menor a 1 segundo
        if (this.cooldown < 1000) {
            this.cooldown = 1000;
        }
    }

    /**
     * Desacelera la recarga (aumenta el cooldown en un 20%).
     */
    public void desacelerarRecarga() {
        // Aumenta el tiempo de cooldown (ej: 5000ms -> 6000ms)
        this.cooldown = (long)(this.cooldown * 1.20);
    }
    // --- FIN NUEVO ---
}