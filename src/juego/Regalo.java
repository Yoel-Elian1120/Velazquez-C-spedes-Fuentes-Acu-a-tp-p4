package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

/**
 * Representa los regalos en la columna 0 que deben ser protegidos.
 * * @author [Velázquez Yoel,Céspedes Isaías,González Acuña Elías,Fuentes Elian]
 */
public class Regalo {
    double x, y, escala;
    Entorno e;
    Image regalo;

    /**
     * 'static' significa que esta variable es COMPARTIDA por TODAS
     * las instancias de Regalo. Esto asegura que todos los regalos
     * giren exactamente al mismo tiempo y ángulo (soluciona el problema de
     * desincronización).
     */
    static double anguloCompartido = 0.0;

    // Velocidad de giro constante
    private final double VELOCIDAD_GIRO = 0.002;

    /**
     * Constructor del Regalo.
     *
     * @param x           Posición X (centro de la celda 0).
     * @param y           Posición Y (centro de la fila).
     * @param e           Referencia al Entorno (para poder dibujar).
     * @param anchoCelda  Ancho de la celda (para escalar).
     * @param altoCelda   Alto de la celda (para escalar).
     */
    public Regalo(double x, double y, Entorno e, double anchoCelda, double altoCelda) {
        this.x = x;
        this.y = y;
        this.e = e;

        String rutaImagen = "recursos/regalo.png";
        regalo = Herramientas.cargarImagen(rutaImagen);

        if (regalo == null) {
            System.out.println("ERROR: No se pudo cargar regalo.png desde " + rutaImagen);
        }

        // Calcular la escala automáticamente para que quepa en la celda
        if (regalo != null) {
            double imgAncho = regalo.getWidth(null);
            double imgAlto = regalo.getHeight(null);

            if (imgAncho > 0 && imgAlto > 0) {
                // Escala al 90% de la celda, manteniendo la proporción
                double escalaAncho = (anchoCelda * 0.9) / imgAncho;
                double escalaAlto = (altoCelda * 0.9) / imgAlto;
                this.escala = Math.min(escalaAncho, escalaAlto);
            } else {
                this.escala = 0.45; // Escala de emergencia
            }
        } else {
            this.escala = 0.45; // Escala de emergencia
        }
    }

    /**
     * Dibuja el regalo.
     * Este método es llamado por Juego.java en cada tick.
     */
    public void dibujar() {
        // 1. Actualiza la variable ESTÁTICA (compartida)
        Regalo.anguloCompartido += VELOCIDAD_GIRO;

        // 2. Dibuja el regalo usando ese ángulo compartido
        if (this.regalo != null) {
            e.dibujarImagen(regalo, this.x, this.y, Regalo.anguloCompartido, this.escala);
        }
    }
}