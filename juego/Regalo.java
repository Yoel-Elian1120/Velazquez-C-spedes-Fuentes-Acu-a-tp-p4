package juego;

import java.awt.Image;
import entorno.Entorno;
import entorno.Herramientas;

public class Regalo {
	double x, y, escala;
	Entorno e;
	Image regalo;
	
	// --- CAMBIO 1: El ángulo ahora es 'static' ---
	// Esto significa que TODOS los regalos comparten esta MISMA variable.
	static double anguloCompartido = 0.0;
	
	// --- CAMBIO 2: Velocidad de giro constante ---
	// (Ya no es aleatoria, puedes ajustar este valor)
	private final double VELOCIDAD_GIRO = 0.002; 

	
	public Regalo(double x, double y, Entorno e, double anchoCelda, double altoCelda) {
		this.x = x;
		this.y = y;
		this.e = e;
		
		// --- CAMBIO 3: Limpiamos la carga de imágenes ---
		// Ya sabemos que la ruta correcta es "recursos/regalo.png"
		String rutaImagen = "recursos/regalo.png";
		regalo = Herramientas.cargarImagen(rutaImagen);
		
		if (regalo != null) {
		 	System.out.println("Regalo cargado desde: " + rutaImagen);
		} else {
			System.out.println("ERROR: No se pudo cargar regalo.png desde " + rutaImagen);
		}
		// --- FIN CAMBIO 3 ---

		// Calcular la escala automáticamente
		if (regalo != null) {
		 	double imgAncho = regalo.getWidth(null);
		 	double imgAlto = regalo.getHeight(null);

		 	if (imgAncho > 0 && imgAlto > 0) {
		 		double escalaAncho = (anchoCelda * 0.9) / imgAncho; 
		 		double escalaAlto = (altoCelda * 0.9) / imgAlto; 
		 		this.escala = Math.min(escalaAncho, escalaAlto); 
		 	} else {
		 		this.escala = 0.45; // Escala de emergencia
		 	}
		} else {
		 	this.escala = 0.45; // Escala de emergencia
		}
		
		// --- CAMBIO 4: Eliminamos la variable 'sentido' ---
		// (Ya no es necesaria)
	}
	
	public void dibujar() {
		// --- CAMBIO 5: Actualizamos el ángulo compartido ---
		// (Todos los regalos usarán este mismo valor)
		Regalo.anguloCompartido += VELOCIDAD_GIRO; 
		
		// Dibujamos usando el ángulo compartido
		e.dibujarImagen(regalo, this.x, this.y, Regalo.anguloCompartido, this.escala);
	}
}