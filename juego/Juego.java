package juego;

// Importaciones obligatorias del entorno
import entorno.Entorno;
import entorno.Herramientas;
import entorno.InterfaceJuego;

// Importaciones estandar de Java (permitidas por el temario)
import java.awt.Color;
import java.awt.Image;
import java.util.Random;

// --- ¡AGREGADAS! Importaciones para MIDI ---
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
// --- FIN DE IMPORTACIONES MIDI ---

/**
 * Clase principal del juego "La Invasión de los Zombies Grinch".
 * * Esta clase hereda de InterfaceJuego (provista por la cátedra) y actúa como
 * el cerebro central del juego. Se encarga de:
 * 1. Inicializar todos los componentes (tablero, HUD, imágenes, música).
 * 2. Controlar el bucle principal del juego (método tick()).
 * 3. Gestionar las entradas del usuario (mouse y teclado).
 * 4. Actualizar el estado de todos los objetos (plantas, zombies, proyectiles).
 * 5. Detectar colisiones entre objetos.
 * 6. Dibujar todos los elementos en la pantalla.
 * 7. Comprobar las condiciones de victoria y derrota.
 *
 * Proyecto para Programación I.
 *
 * @author [Aquí ponen sus nombres]
 * @version 1.0 (Noviembre 2025)
 */
public class Juego extends InterfaceJuego {

	// -----------------------------------------------------------------
	// 1. ATRIBUTOS Y CONSTANTES
	// -----------------------------------------------------------------

	// --- El Entorno ---
	/**
	 * El objeto 'entorno' es la conexión principal con la biblioteca de la cátedra.
	 * Controla la ventana, el dibujo y la ejecución del bucle 'tick()'.
	 */
	private Entorno entorno;

	// --- Imágenes (cargadas una sola vez) ---
	/**
	 * Guardamos las imágenes como atributos para cargarlas UNA SOLA VEZ en el
	 * constructor.
	 * Cargarlas dentro de 'tick()' sería un suicidio de rendimiento (se cargarían 60
	 * veces por segundo).
	 */
	private Image imgFondo;
	private Regalo[] regalos;
	private Image imgExplosion; // (Opcional 3)

	// --- ¡NUEVAS IMÁGENES DEL HUD! ---
	/**
	 * Imágenes específicas para la Interfaz de Usuario (HUD).
	 * 'imgPanelStats' es el fondo de la barra de estadísticas.
	 * Las 'imgCarta...' son las imágenes para el panel de tipos de zombies.
	 */
	private Image imgPanelStats;
	private Image imgCartaZombieNormal;
	private Image imgCartaZombieTanque;
	private Image imgCartaZombieColosal;

	// --- ¡NUEVAS ESCALAS PARA EL HUD! ---
	/**
	 * Variables para almacenar la escala (el "zoom") de las imágenes del HUD.
	 * Las calculamos en el constructor para que no se vean deformadas.
	 */
	private double escalaZombieNormal;
	private double escalaZombieTanque;
	private double escalaZombieColosal;

	// --- Constantes del Tablero (Ajusta estos valores) ---
	/**
	 * CONSTANTES (final) para definir la estructura del juego.
	 * Usar constantes hace que el código sea fácil de modificar.
	 * Si queremos un tablero de 10x10, solo cambiamos los 'final' aquí.
	 * 'OFFSET' define el margen (en píxeles) entre el borde de la ventana
	 * y el inicio del tablero de juego.
	 */
	private final int FILAS = 5;
	private final int COLUMNAS = 9;
	private final int ANCHO_CELDA = 80;
	private final int ALTO_CELDA = 100;
	private final int OFFSET_X_TABLERO = 80; // Margen izquierdo
	private final int OFFSET_Y_TABLERO = 100; // Margen superior (para dejar espacio al HUD)

	// --- Constantes del HUD (Ajusta estos valores) ---
	/**
	 * Constantes para posicionar la Interfaz de Usuario (HUD) en la parte
	 * superior de la pantalla.
	 */
	private final int ALTO_HUD = 100;
	private final int POS_X_CARTA_ROSA = 50;
	private final int POS_X_CARTA_NUEZ = 150;
	private final int POS_X_CARTA_EXPLOSIVA = 250; // (Opcional 3)
	private final int POS_Y_CARTAS = 50; // Posición Y (vertical) de todas las cartas

	private final double ANCHO_CARTA_HUD = 80; // Ancho deseado para reescalar
	private final double ALTO_CARTA_HUD = 90; // Alto deseado para reescalar

	// --- Constantes del Juego ---
	/**
	 * Reglas principales del juego (Requerimientos 7 y 8).
	 */
	private final int ZOMBIES_TOTALES_A_ELIMINAR = 50; // Req 8
	private final int MAX_ZOMBIES_SIMULTANEOS = 15; // Req 7

	// --- Arreglos (Estructuras de datos permitidas) ---
	/**
	 * Arreglos (Arrays) que almacenan TODOS los objetos dinámicos del juego.
	 * - tablero: Grilla 2D que guarda qué hay en cada celda (Planta, Tumba, o
	 * null).
	 * - zombies: Guarda TODOS los zombies actualmente en pantalla.
	 * - proyectiles: Guarda las bolas de fuego de las RoseBlade.
	 * - cartasHUD: Guarda las 3 cartas de plantas (para su recarga).
	 * - bolasDeNieve: Guarda los proyectiles de los zombies (Opcional 1).
	 * - items: Guarda los items de bonus/malus (Opcional 5).
	 */
	private Casilla[][] tablero;
	private ZombieGrinch[] zombies;
	private Proyectil[] proyectiles;
	private CartaPlanta[] cartasHUD;
	private BolaDeNieve[] bolasDeNieve; // (Opcional 1)

	// <-- (Opcional 5: Items) -->
	private Item[] items;
	// --- FIN ---

	// --- NUEVO (Opcional 6: Jefe Final) ---
	/**
	 * Variable para el jefe final. Es 'null' durante la mayor parte del juego
	 * y se crea (instancia) al final. 'MODO_PRUEBA_JEFE' es una bandera de
	 * programador
	 * para testearlo rápido.
	 */
	private ZombieColosal jefeFinal;
	private final boolean MODO_PRUEBA_JEFE = false; // ¡Poner en 'true' para probar al jefe!
	// --- FIN NUEVO ---

	// --- Variables de Estado del Juego ---
	/**
	 * Variables "vivas" o de estado. Son contadores y "banderas" (boolean)
	 * que controlan el flujo del juego.
	 */
	private int zombiesEliminados; // Contador para el HUD y la condición de victoria
	private int zombiesEnPantalla; // Contador para no superar MAX_ZOMBIES_SIMULTANEOS
	private long tiempoInicioJuego; // "Marca" de tiempo de cuándo empezó todo
	private long proximoSpawnZombie; // "Marca" de tiempo de cuándo debe aparecer el próximo
	private Random random; // Objeto para generar números aleatorios
	private boolean juegoGanado; // Bandera: se vuelve 'true' al ganar
	private boolean juegoPerdido; // Bandera: se vuelve 'true' al perder

	// --- VARIABLES DEL SISTEMA DE NIVELES ---
	/**
	 * Sistema simple de dificultad. El nivel sube y afecta la generación de
	 * zombies.
	 */
	private int nivelActual;
	private final int KILLS_POR_NIVEL = 10; // Sube de nivel cada 10 enemigos muertos
	// --- FIN ---

	// --- Variables de Estado de Input (Req 2 y 3) ---
	/**
	 * Variables para manejar las acciones del mouse.
	 * - plantaArrastrando: Guarda la CARTA que se está arrastrando desde el HUD.
	 * - plantaSeleccionadaParaMover: Guarda la PLANTA que se seleccionó en el
	 * TABLERO.
	 * Son mutuamente excluyentes (o haces una cosa, o la otra).
	 */
	private CartaPlanta plantaArrastrando;
	private Planta plantaSeleccionadaParaMover;

	// --- Variables de Efecto de Explosión (Opcional 3) ---
	/**
	 * Variables para el efecto visual de la explosión.
	 * 'duracionExplosion' es un contador simple que baja en cada tick.
	 */
	private int duracionExplosion;
	private double explosionX;
	private double explosionY;

	// --- ¡AGREGADOS! Atributos para MIDI ---
	/**
	 * Objetos de Java para cargar y reproducir la música de fondo (formato MIDI).
	 */
	private Sequence secuencia;
	private Sequencer secuenciador;
	// --- FIN ATRIBUTOS MIDI ---

	// -----------------------------------------------------------------
	// 2. CONSTRUCTOR Y MÉTODO MAIN
	// -----------------------------------------------------------------

	/**
	 * Constructor de la clase Juego.
	 * Se ejecuta UNA SOLA VEZ al inicio del programa.
	 * Su función es inicializar la ventana, cargar recursos y preparar el estado
	 * inicial del juego.
	 */
	Juego() {
		// 1. Inicializa el objeto entorno (crea la ventana)
		// Parámetros: (this, "Título de la ventana", ancho, alto)
		this.entorno = new Entorno(this, "La Invasión de los Zombies Grinch", 800, 600);

		// 2. Inicializa el generador de números aleatorios
		this.random = new Random();

		// 3. Cargar imágenes estáticas (las que se usarán siempre)
		// Se usa 'Herramientas.cargarImagen' provisto por la cátedra.
		this.imgFondo = Herramientas.cargarImagen("recursos/fondo.png");
		this.imgExplosion = Herramientas.cargarImagen("recursos/explosion.png");

		// 4. Cargar imágenes del HUD
		this.imgPanelStats = Herramientas.cargarImagen("recursos/panel_stats.png");
		this.imgCartaZombieNormal = Herramientas.cargarImagen("recursos/carta_zombie_normal.png");
		this.imgCartaZombieTanque = Herramientas.cargarImagen("recursos/carta_zombie_tanque.png");
		this.imgCartaZombieColosal = Herramientas.cargarImagen("recursos/carta_zombie_colosal.png");

		// 5. Calcular escalas de las imágenes (para que no se deformen)
		// Se usa un método auxiliar (helper) definido en la Sección 7.
		this.escalaZombieNormal = calcularEscalaImagen(this.imgCartaZombieNormal, ANCHO_CARTA_HUD, ALTO_CARTA_HUD);
		this.escalaZombieTanque = calcularEscalaImagen(this.imgCartaZombieTanque, ANCHO_CARTA_HUD, ALTO_CARTA_HUD);
		this.escalaZombieColosal = calcularEscalaImagen(this.imgCartaZombieColosal, ANCHO_CARTA_HUD, ALTO_CARTA_HUD);

		// 6. Inicializar variables de estado del juego
		this.zombiesEliminados = 0;
		this.zombiesEnPantalla = 0;
		this.juegoGanado = false;
		this.juegoPerdido = false;
		this.plantaArrastrando = null; // Nadie está arrastrando nada al inicio
		this.plantaSeleccionadaParaMover = null; // No hay planta seleccionada
		this.tiempoInicioJuego = System.currentTimeMillis(); // Guarda el "tiempo cero"
		this.nivelActual = 1;
		this.proximoSpawnZombie = this.tiempoInicioJuego + 3000; // El 1er zombie aparece a los 3 seg
		this.duracionExplosion = 0; // Sin explosiones al inicio

		// 7. Inicializar los arreglos (crear los "contenedores" vacíos)
		// NOTA: Los arreglos de objetos se inicializan en 'null' por defecto.
		this.tablero = new Casilla[FILAS][COLUMNAS];
		this.cartasHUD = new CartaPlanta[3]; // Para las 3 cartas de plantas
		this.zombies = new ZombieGrinch[MAX_ZOMBIES_SIMULTANEOS];
		this.proyectiles = new Proyectil[50]; // Límite de 50 proyectiles en pantalla
		this.bolasDeNieve = new BolaDeNieve[100]; // Límite de 100 bolas de nieve
		this.items = new Item[20]; // Límite de 20 items en suelo
		this.jefeFinal = null; // El jefe no existe al inicio

		// 8. Llamar a los métodos 'helper' para "llenar" los arreglos
		// (Ver Sección 4 para estos métodos)
		inicializarTablero(); // Crea las 45 (5x9) casillas de pasto
		inicializarHUD(); // Crea los 3 objetos CartaPlanta
		inicializarRegalos(); // Crea los 5 objetos Regalo

		// 9. Cargar y reproducir la música MIDI (Opcional)
		// Se usa un bloque 'try-catch' porque la carga de archivos o
		// el hardware de sonido pueden fallar (y Java nos obliga a manejarlo).
		try {
			// Usamos 'getSystemResourceAsStream' que es más fiable para encontrar
			// el archivo "sup.mid" (debe estar en la carpeta 'src' o 'recursos').
			java.io.InputStream midiStream = ClassLoader.getSystemResourceAsStream("sup.mid");

			// Si el archivo se encontró (no es nulo)...
			if (midiStream != null) {
				this.secuencia = MidiSystem.getSequence(midiStream);
				this.secuenciador = MidiSystem.getSequencer();
				this.secuenciador.open();
				this.secuenciador.setSequence(this.secuencia);
				this.secuenciador.setTempoFactor(1.0f); // Velocidad normal
				this.secuenciador.setLoopCount(Sequencer.LOOP_CONTINUOUSLY); // Repetir sin parar
				this.secuenciador.start(); // ¡Suena la música!
			} else {
				// Si no encuentra el archivo, avisa por consola.
				System.out.println("Error al cargar MIDI: No se encontró el archivo 'sup.mid'.");
			}
		} catch (NullPointerException e) {
			System.out.println("Error (Null) al cargar MIDI: No se encontró 'sup.mid'.");
			e.printStackTrace();
		} catch (InvalidMidiDataException | IOException | MidiUnavailableException e) {
			// Captura otros errores (archivo corrupto, no hay placa de sonido)
			System.out.println("Error al cargar el archivo MIDI 'sup.mid': " + e.getMessage());
			e.printStackTrace();
		}
		// --- FIN DEL BLOQUE MIDI ---

		// 10. Inicia el juego!
		// Esta es la última línea. Llama al motor del 'entorno' para
		// que empiece a ejecutar el método 'tick()' en bucle.
		this.entorno.iniciar();
	}

	// -----------------------------------------------------------------
	// 3. MÉTODO TICK (Bucle principal del juego)
	// -----------------------------------------------------------------

	/**
	 * El método 'tick()' es el corazón del juego. Se ejecuta en un bucle
	 * continuo (aprox. 60 veces por segundo) gracias a 'entorno.iniciar()'.
	 *
	 * Sigue un orden lógico estricto en cada "fotograma":
	 * 1. PROCESAR ENTRADAS: ¿Qué hizo el jugador?
	 * 2. ACTUALIZAR ESTADO: Mover todo, aplicar lógica (disparar, etc.).
	 * 3. GENERAR NUEVAS ENTIDADES: ¿Aparecen nuevos zombies?
	 * 4. DETECTAR COLISIONES: ¿Algo chocó? (Balas, zombies, plantas).
	 * 5. DIBUJAR TODO: Limpiar la pantalla y dibujar todo en su nueva posición.
	 * 6. COMPROBAR ESTADO: ¿Alguien ganó o perdió?
	 */
	public void tick() {
		// --- 0. Comprobar si el juego ya terminó ---
		// Si ganamos o perdimos, solo dibujamos la escena final y
		// usamos 'return' para salir del 'tick()' inmediatamente.
		// Esto "congela" el juego.
		if (juegoGanado || juegoPerdido) {
			dibujarEscena(); // Dibuja el estado final
			dibujarFinDeJuego(); // Dibuja el texto "GANASTE" o "PERDISTE"
			return; // No ejecuta nada más del 'tick'
		}

		// Obtenemos el tiempo actual (en milisegundos)
		// Se usa para lógicas de tiempo (cooldowns, disparos, etc.)
		long tiempoActual = System.currentTimeMillis();

		// --- 1. PROCESAR ENTRADAS ---
		// Revisa el mouse y el teclado.
		// (Ver Sección 5)
		manejarInput(tiempoActual);

		// --- 2. ACTUALIZAR ESTADO ---
		// Mueve los objetos y aplica su lógica interna.
		// (Ver Sección 5)
		actualizarZombies(tiempoActual); // Mover zombies, decidir si disparan

		if (this.jefeFinal != null) { // Si el jefe existe, moverlo
			this.jefeFinal.mover();
		}

		actualizarProyectiles(); // Mover bolas de fuego
		actualizarBolasDeNieve(); // Mover bolas de nieve
		actualizarPlantas(tiempoActual); // Decidir si las plantas disparan
		actualizarEfectoExplosion(); // Reducir el contador de la explosión

		// --- 3. GENERAR NUEVAS ENTIDADES ---
		// Decide si es momento de crear un nuevo zombie.
		// (Ver Sección 5)
		generarZombies(tiempoActual);

		// --- 4. DETECTAR COLISIONES ---
		// Revisa si los objetos se chocaron.
		// (Ver Sección 5)
		detectarColisiones();

		// --- 5. DIBUJAR TODO ---
		// Limpia la pantalla y dibuja todo en su nueva posición.
		// (Ver Sección 6)
		dibujarEscena();

		// --- 6. COMPROBAR ESTADO ---
		// Revisa si se cumplió la condición de victoria o derrota.
		// (Ver Sección 5)
		comprobarEstadoJuego();
	}

	// -----------------------------------------------------------------
	// 4. MÉTODOS DE INICIALIZACIÓN (Llamados desde el constructor)
	// -----------------------------------------------------------------

	/**
	 * Crea los 5 objetos Regalo y los guarda en el arreglo 'regalos'.
	 * Se colocan en la columna 0 (la primera).
	 */
	private void inicializarRegalos() {
		this.regalos = new Regalo[FILAS];
		double x = celdaAPixelX(0);
		for (int f = 0; f < FILAS; f++) {
			double y = celdaAPixelY(f);
			this.regalos[f] = new Regalo(x, y, this.entorno, ANCHO_CELDA, ALTO_CELDA);
		}
	}

	/**
	 * Recorre la grilla (Fila x Columna) y crea un objeto 'Casilla'
	 * en cada posición del arreglo 'tablero'.
	 * Asigna texturas de pasto intercaladas para el efecto "tablero de ajedrez".
	 */
	private void inicializarTablero() {
		for (int f = 0; f < FILAS; f++) {
			for (int c = 0; c < COLUMNAS; c++) {
				double x = celdaAPixelX(c);
				double y = celdaAPixelY(f);
				String textura;
				if ((f + c) % 2 == 0) {
					textura = "recursos/pastoverde.jpg";
				} else {
					textura = "recursos/pastoseco.jpg";
				}
				this.tablero[f][c] = new Casilla(x, y, textura);
			}
		}
	}

	/**
	 * Crea los 3 objetos 'CartaPlanta' (Rosa, Nuez, Explosiva) y los
	 * guarda en el arreglo 'cartasHUD'.
	 * Define sus cooldowns (tiempos de recarga) iniciales.
	 */
	private void inicializarHUD() {
		long cooldownRosa = 10;
		long cooldownNuez = 10; // <-- Sigue en modo Test (10ms)

		this.cartasHUD[0] = new CartaPlanta(POS_X_CARTA_ROSA, POS_Y_CARTAS,
				"rosablade", cooldownRosa, "recursos/carta_rosa.png");
		this.cartasHUD[1] = new CartaPlanta(POS_X_CARTA_NUEZ, POS_Y_CARTAS,
				"wallnut", cooldownNuez, "recursos/carta_nuez.png");

		long cooldownExplosiva = 20000;
		this.cartasHUD[2] = new CartaPlanta(POS_X_CARTA_EXPLOSIVA, POS_Y_CARTAS,
				"explosiva", cooldownExplosiva, "recursos/cartaexplosiva.png");
	}

	/**
	 * Método "helper" (ayudante) para el Opcional 2 (Obstáculos).
	 * Coloca una 'Tumba' en una celda específica (f, c) si está vacía.
	 */
	private void colocarTumba(int f, int c) {
		if (f >= 0 && f < FILAS && c > 0 && c < COLUMNAS) {
			if (this.tablero[f][c].ocupante == null) {
				double x = celdaAPixelX(c);
				double y = celdaAPixelY(f);
				this.tablero[f][c].ocupante = new Tumba(x, y, f, c);
			}
		}
	}

	// -----------------------------------------------------------------
	// 5. MÉTODOS DE LÓGICA (Llamados desde tick())
	// -----------------------------------------------------------------

	/**
	 * Método principal de gestión de entradas. Se llama en cada 'tick'.
	 * Controla el "estado" del mouse (presionar, soltar, arrastrar).
	 */
	private void manejarInput(long tiempoActual) {

		// --- 1. LÓGICA DE PRESIONAR EL MOUSE (Botón Izquierdo) ---
		if (entorno.sePresionoBoton(entorno.BOTON_IZQUIERDO)) {

			// Primero, chequear si se clickeó el HUD para plantar
			// Recorremos las cartas del HUD
			for (int i = 0; i < cartasHUD.length; i++) {
				// Si hicimos clic en una carta Y está lista (sin cooldown)...
				if (cartasHUD[i].fueClickeada(entorno.mouseX(), entorno.mouseY()) &&
						cartasHUD[i].estaLista(tiempoActual)) {
					//...la guardamos en 'plantaArrastrando'.
					this.plantaArrastrando = cartasHUD[i];
					this.plantaSeleccionadaParaMover = null; // Deselecciona cualquier otra (Req 2 y 3)
					break; // Dejamos de revisar las otras cartas
				}
			}

			// Si NO estamos arrastrando una carta nueva...
			if (this.plantaArrastrando == null) {
				// ...chequear si se clickeó un item (Opcional 5)
				boolean itemClickeado = chequearClickItem();

				// Si no fue un item, chequear si se clickeó una PLANTA en el tablero
				if (!itemClickeado) {
					// Este método PONE una planta en 'plantaSeleccionadaParaMover'
					// si se hace clic sobre ella, o la pone en NULL si se clickea pasto.
					manejarInputSeleccionPlanta();
				}
			}
		}

		// --- 2. LÓGICA DE SOLTAR EL MOUSE (Botón Izquierdo) ---
		if (entorno.seLevantoBoton(entorno.BOTON_IZQUIERDO)) {

			// --- Caso A: Estaba arrastrando una NUEVA planta (desde el HUD) ---
			if (this.plantaArrastrando != null) {
				// Convertimos el pixel del mouse a una celda (fila, columna)
				int[] celda = pixelACelda(entorno.mouseX(), entorno.mouseY());
				int f = celda[0];
				int c = celda[1];

				// Si la celda es válida para plantar...
				if (esCeldaValidaParaPlantar(f, c)) {
					double x = celdaAPixelX(c);
					double y = celdaAPixelY(f);
					// Creamos la planta correspondiente
					if (this.plantaArrastrando.getTipo().equals("rosablade")) {
						this.tablero[f][c].ocupante = new RoseBlade(x, y, f, c);
					} else if (this.plantaArrastrando.getTipo().equals("wallnut")) {
						this.tablero[f][c].ocupante = new WallNut(x, y, f, c);
					} else if (this.plantaArrastrando.getTipo().equals("explosiva")) {
						this.tablero[f][c].ocupante = new PlantaExplosiva(x, y, f, c);
					}

					// Iniciamos el cooldown de la carta
					this.plantaArrastrando.iniciarRecarga(tiempoActual);
				}
				// Soltamos la planta (ya sea que se plantó o no)
				this.plantaArrastrando = null; // Termina el arrastre
			}

			// --- Caso B: Estaba moviendo una planta EXISTENTE (con clic-arrastrar-soltar) ---
			else if (this.plantaSeleccionadaParaMover != null) {

				// Celda original (donde estaba la planta)
				int f_actual = this.plantaSeleccionadaParaMover.getFila();
				int c_actual = this.plantaSeleccionadaParaMover.getCol();

				// Celda destino (donde se soltó el mouse)
				int[] celdaDestino = pixelACelda(entorno.mouseX(), entorno.mouseY());
				int f_nueva = celdaDestino[0];
				int c_nueva = celdaDestino[1];

				if (f_nueva == f_actual && c_nueva == c_actual) {
					// **Sub-caso 1: Se soltó en la MISMA celda.**
					// El usuario solo hizo clic, quiere usar las teclas (WASD).
					// No hacemos nada, la planta sigue seleccionada.
				} else if (esCeldaValidaParaPlantar(f_nueva, c_nueva)) {
					// **Sub-caso 2: Se soltó en una NUEVA celda válida.**
					// ¡Movemos la planta!

					Planta plantaAMover = this.plantaSeleccionadaParaMover;

					// Actualizamos sus coordenadas y fila/col
					plantaAMover.actualizarPosicion(celdaAPixelX(c_nueva), celdaAPixelY(f_nueva), f_nueva, c_nueva);

					// La movemos en el tablero
					this.tablero[f_nueva][c_nueva].ocupante = plantaAMover;
					this.tablero[f_actual][c_actual].ocupante = null; // Vaciar la celda vieja

					// Deseleccionamos, ya que el movimiento se completó
					this.plantaSeleccionadaParaMover = null;
				} else {
					// **Sub-caso 3: Se soltó en un LUGAR INVÁLIDO.**
					// (Fuera del tablero, en la columna 0, o celda ocupada)
					// Se cancela la acción. Deseleccionamos la planta.
					this.plantaSeleccionadaParaMover = null;
				}
			}
		}

		// --- 3. LÓGICA DE MOVER CON TECLADO (WASD) ---
		// Esta parte solo se ejecutará si la planta sigue seleccionada
		// (es decir, si el usuario está en el "Sub-caso 1" de soltar mouse).
		if (this.plantaSeleccionadaParaMover != null) {
			manejarInputMoverPlanta();
		}
	}

	/**
	 * Método helper (ayudante) de 'manejarInput'.
	 * Se llama cuando se presiona el mouse.
	 * Revisa si el clic fue sobre una planta en el tablero y, si es así,
	 * la guarda en 'plantaSeleccionadaParaMover'.
	 * Si se hace clic en el pasto, deselecciona todo (pone 'null').
	 */
	private void manejarInputSeleccionPlanta() {
		int[] celda = pixelACelda(entorno.mouseX(), entorno.mouseY());
		int f = celda[0];
		int c = celda[1];
		// Si se clickea fuera del tablero
		if (f == -1 || c == -1) {
			this.plantaSeleccionadaParaMover = null;
			return;
		}
		Casilla casillaClickeada = this.tablero[f][c];

		// Si hay una planta en la casilla (y no es una Tumba)
		if (casillaClickeada.ocupante != null &&
				!(casillaClickeada.ocupante instanceof Tumba)) {
			// La seleccionamos
			this.plantaSeleccionadaParaMover = casillaClickeada.ocupante;
		} else {
			// Si es pasto o tumba, deseleccionamos
			this.plantaSeleccionadaParaMover = null;
		}
	}

	/**
	 * Método helper (ayudante) de 'manejarInput'.
	 * Se llama cuando el jugador presiona WASD o las flechas.
	 * Solo funciona si 'plantaSeleccionadaParaMover' NO es 'null'.
	 * Mueve la planta a la nueva celda si es válida.
	 */
	private void manejarInputMoverPlanta() {
		// Tecla "Enter" para deseleccionar
		if (entorno.sePresiono(entorno.TECLA_ENTER)) {
			this.plantaSeleccionadaParaMover = null;
			return;
		}

		if (this.plantaSeleccionadaParaMover == null) {
			return;
		}

		int filaActual = this.plantaSeleccionadaParaMover.getFila();
		int colActual = this.plantaSeleccionadaParaMover.getCol();

		// Check de seguridad: si la planta ya no está donde creíamos, deseleccionar
		if (this.tablero[filaActual][colActual].ocupante != this.plantaSeleccionadaParaMover) {
			this.plantaSeleccionadaParaMover = null;
			return;
		}

		int filaNueva = filaActual;
		int colNueva = colActual;

		// Detectar qué tecla se presionó
		if (entorno.sePresiono(entorno.TECLA_ARRIBA) || entorno.sePresiono('w')) {
			filaNueva--;
		}
		if (entorno.sePresiono(entorno.TECLA_ABAJO) || entorno.sePresiono('s')) {
			filaNueva++;
		}
		if (entorno.sePresiono(entorno.TECLA_IZQUIERDA) || entorno.sePresiono('a')) {
			colNueva--;
		}
		if (entorno.sePresiono(entorno.TECLA_DERECHA) || entorno.sePresiono('d')) {
			colNueva++;
		}

		// Si no se presionó nada nuevo
		if (filaNueva == filaActual && colNueva == colActual) {
			return;
		}

		// Si la nueva celda es un destino válido...
		if (esCeldaValidaParaPlantar(filaNueva, colNueva)) {

			Planta plantaAMover = this.plantaSeleccionadaParaMover;

			// Moverla
			plantaAMover.actualizarPosicion(celdaAPixelX(colNueva), celdaAPixelY(filaNueva), filaNueva, colNueva);
			this.tablero[filaNueva][colNueva].ocupante = plantaAMover;
			this.tablero[filaActual][colActual].ocupante = null; // Dejar la celda vieja vacía
		}
		// Nota: la planta sigue seleccionada, permitiendo moverla varias veces
	}

	/**
	 * Actualiza el estado de TODOS los zombies en el arreglo 'zombies'.
	 * - Llama a 'zombie.mover()'
	 * - Llama a 'zombie.intentarDisparar()' (Opcional 1)
	 * - Elimina zombies (pone 'null') si se salen de la pantalla por la izquierda.
	 */
	private void actualizarZombies(long tiempoActual) {
		for (int i = 0; i < this.zombies.length; i++) {
			if (this.zombies[i] != null) {
				// 1. Mover el zombie
				this.zombies[i].mover();
				
				// 2. Intentar disparar (Opcional 1)
				BolaDeNieve bola = this.zombies[i].intentarDisparar(tiempoActual);
				if (bola != null) {
					// Si disparó, buscar un espacio libre en el arreglo de bolas de nieve
					for (int b = 0; b < this.bolasDeNieve.length; b++) {
						if (this.bolasDeNieve[b] == null) {
							this.bolasDeNieve[b] = bola;
							break; // Dejar de buscar espacio
						}
					}
				}
				
				// 3. Limpiar zombies que se fueron de pantalla
				if (this.zombies[i].getX() < -50) { // -50 de margen
					this.zombies[i] = null;
					this.zombiesEnPantalla--;
				}
			}
		}
	}

	/**
	 * Actualiza el estado de TODOS los proyectiles (bolas de fuego).
	 * - Llama a 'proyectil.mover()'
	 * - Elimina proyectiles (pone 'null') si se salen de la pantalla por la
	 * derecha.
	 */
	private void actualizarProyectiles() {
		for (int i = 0; i < this.proyectiles.length; i++) {
			if (this.proyectiles[i] != null) {
				this.proyectiles[i].mover();
				// Limpiar si se va de pantalla (Req 11)
				if (this.proyectiles[i].getX() > entorno.ancho() + 50) { // +50 de margen
					this.proyectiles[i] = null;
				}
			}
		}
	}

	/**
	 * Actualiza el estado de TODAS las bolas de nieve (Opcional 1).
	 * - Llama a 'bola.mover()'
	 * - Elimina bolas (pone 'null') si se salen de la pantalla por la izquierda.
	 */
	private void actualizarBolasDeNieve() {
		for (int i = 0; i < this.bolasDeNieve.length; i++) {
			if (this.bolasDeNieve[i] != null) {
				this.bolasDeNieve[i].mover();
				// Limpiar si se va de pantalla (Req 11)
				if (this.bolasDeNieve[i].getX() < 0) {
					this.bolasDeNieve[i] = null;
				}
			}
		}
	}

	/**
	 * Actualiza el contador del efecto visual de la explosión.
	 * Es un simple temporizador que descuenta en cada tick.
	 */
	private void actualizarEfectoExplosion() {
		if (this.duracionExplosion > 0) {
			this.duracionExplosion--;
		}
	}

	/**
	 * Actualiza el estado de TODAS las plantas en el 'tablero'.
	 * Recorre la grilla y, si encuentra una 'RoseBlade', le pregunta
	 * si 'puedeDisparar()'. Si es así, crea un nuevo 'Proyectil'.
	 */
	private void actualizarPlantas(long tiempoActual) {
		for (int f = 0; f < FILAS; f++) {
			for (int c = 0; c < COLUMNAS; c++) {
				Planta p = this.tablero[f][c].ocupante;
				// Si la planta es una RoseBlade...
				if (p != null && p instanceof RoseBlade) {
					RoseBlade rosa = (RoseBlade) p; // Conversión (cast)
					// ...y puede disparar (pasó su cooldown)...
					if (rosa.puedeDisparar(tiempoActual)) {
						// ...buscar un espacio en el arreglo de proyectiles
						for (int i = 0; i < this.proyectiles.length; i++) {
							if (this.proyectiles[i] == null) {
								this.proyectiles[i] = rosa.disparar();
								break; // Dejar de buscar espacio
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Controla CUÁNDO y QUÉ TIPO de zombie aparece (spawn).
	 * - Revisa si ya pasó el tiempo de 'proximoSpawnZombie'.
	 * - Revisa si hay espacio (menos de 'MAX_ZOMBIES_SIMULTANEOS').
	 * - Revisa si aún quedan zombies por eliminar.
	 * - Lógica de Nivel: A más nivel, más chance de 'ZombieTanque'.
	 * - Controla la aparición del Jefe Final.
	 */
	private void generarZombies(long tiempoActual) {

		int zombiesRestantes = ZOMBIES_TOTALES_A_ELIMINAR - this.zombiesEliminados;

		// --- Lógica del JEFE FINAL (Opcional 6) ---
		// Si NO estamos en modo prueba, ya matamos a todos Y no quedan zombies...
		if (!MODO_PRUEBA_JEFE && zombiesRestantes <= 0 && this.zombiesEnPantalla == 0) {
			// ...y el jefe aún no existe...
			if (this.jefeFinal == null) {
				// ¡Creamos al JEFE!
				double xSpawn = this.entorno.ancho() + 150;
				double ySpawn = (this.entorno.alto() + ALTO_HUD) / 2.0; // Centrado vertical
				this.jefeFinal = new ZombieColosal(xSpawn, ySpawn);
			}
		}
		// --- FIN LÓGICA DEL JEFE ---

		// --- Lógica de spawn de zombies normales ---
		
		// 1. ¿Hay lugar para más zombies? (Req 7)
		if (this.zombiesEnPantalla >= MAX_ZOMBIES_SIMULTANEOS) {
			return; // No spawnea
		}
		// 2. ¿Pasó el tiempo de cooldown?
		if (tiempoActual < this.proximoSpawnZombie) {
			return; // No spawnea
		}
		// 3. ¿Quedan zombies por spawnear? (Req 8)
		if (this.zombiesEliminados + this.zombiesEnPantalla >= ZOMBIES_TOTALES_A_ELIMINAR) {
			return; // No spawnea
		}

		// Si pasa todos los filtros, buscamos un espacio libre en el arreglo 'zombies'
		for (int i = 0; i < this.zombies.length; i++) {
			if (this.zombies[i] == null) {

				// Posición de spawn
				int filaRandom = this.random.nextInt(FILAS); // Fila aleatoria (0 a 4)
				double xSpawn = this.entorno.ancho() + 50; // Fuera de pantalla (derecha)
				double ySpawn = celdaAPixelY(filaRandom);

				// --- INICIO: LÓGICA DE DIFICULTAD POR NIVEL ---
				// A más nivel, más chance de que sea un 'Tanque'
				double chanceTanque = 0.10 + (this.nivelActual * 0.10); // 10% base + 10% por nivel
				if (chanceTanque > 0.8) { // Límite de 80%
					chanceTanque = 0.8;
				}

				int tipoZombie;
				if (this.random.nextDouble() < chanceTanque) {
					tipoZombie = 1; // Tanque
				} else {
					tipoZombie = 0; // Normal
				}
				// Creamos el zombie
				this.zombies[i] = new ZombieGrinch(xSpawn, ySpawn, filaRandom, tipoZombie);

				// --- FIN: LÓGICA DE DIFICULTAD POR NIVEL ---

				// --- LÓGICA DE COOLDOWN DE SPAWN ---
				// A más nivel, más rápido aparecen
				long baseCooldown = 2000 - (this.nivelActual * 200); // 2 seg base, -0.2s por nivel
				int rangoCooldown = 3000 - (this.nivelActual * 200); // 3 seg rango, -0.2s por nivel

				if (baseCooldown < 500) baseCooldown = 500; // Mínimo 0.5 seg
				if (rangoCooldown < 1000) rangoCooldown = 1000; // Mínimo 1 seg

				// Próximo spawn = tiempo actual + (cooldown base + un extra aleatorio)
				long tiempoRandom = baseCooldown + this.random.nextInt(rangoCooldown);
				this.proximoSpawnZombie = tiempoActual + tiempoRandom;
				
				this.zombiesEnPantalla++; // Aumentamos el contador
				break; // Dejamos de buscar espacio
			}
		}
	} // <-- FIN del método generarZombies

	/**
	 * Lógica de la 'PlantaExplosiva' (Opcional 3).
	 * Se llama cuando una 'PlantaExplosiva' colisiona.
	 * Recorre el arreglo 'zombies' y elimina a todos los que estén
	 * dentro de un 'RADIO_EXPLOSION'.
	 * También daña al Jefe Final si está en el radio.
	 */
	private void explotarEn(double x, double y) {
		double RADIO_EXPLOSION = 150; // Radio en píxeles
		this.explosionX = x;
		this.explosionY = y;
		this.duracionExplosion = 10; // Duración del efecto visual (10 ticks)

		// Revisar zombies normales
		for (int i = 0; i < this.zombies.length; i++) {
			ZombieGrinch z = this.zombies[i];
			if (z != null) {
				// Teorema de Pitágoras para calcular la distancia
				double dist = Math.sqrt(
						Math.pow(z.getX() - x, 2) +
								Math.pow(z.getY() - y, 2));

				if (dist < RADIO_EXPLOSION) {
					// ¡Boom!
					this.zombies[i] = null;
					this.zombiesEliminados++;
					this.zombiesEnPantalla--;

					// Checkear si sube de nivel
					if (this.zombiesEliminados % KILLS_POR_NIVEL == 0
							&& this.zombiesEliminados < ZOMBIES_TOTALES_A_ELIMINAR) {
						this.nivelActual++;
						System.out.println("¡NIVEL " + this.nivelActual + " ALCANZADO!");
					}
				}
			}
		}

		// Revisar Jefe Final
		if (this.jefeFinal != null) {
			double distJefe = Math.sqrt(
					Math.pow(this.jefeFinal.getX() - x, 2) +
							Math.pow(this.jefeFinal.getY() - y, 2));
			if (distJefe < RADIO_EXPLOSION) {
				// La explosión le da 10 golpes de una
				for (int d = 0; d < 10; d++) {
					boolean murioJefe = this.jefeFinal.recibirDisparo();
					if (murioJefe) {
						this.jefeFinal = null;
						this.zombiesEliminados += 100; // Gana el juego
						break;
					}
				}
			}
		}
	}

	/**
	 * Revisa si el jugador hizo clic sobre un 'Item' en el suelo (Opcional 5).
	 * Se llama desde 'manejarInput'.
	 * * @return 'true' si se clickeó un item, 'false' si no.
	 */
	private boolean chequearClickItem() {
		int mouseX = entorno.mouseX();
		int mouseY = entorno.mouseY();

		for (int i = 0; i < this.items.length; i++) {
			Item item = this.items[i];

			if (item != null && item.fueClickeado(mouseX, mouseY)) {
				aplicarEfectoItem(item.getTipo());
				this.items[i] = null; // El item desaparece
				return true;
			}
		}
		return false;
	}

	/**
	 * Aplica el efecto de un 'Item' (Opcional 5).
	 * Acelera o desacelera la recarga de TODAS las cartas del HUD.
	 * * @param tipo 0 para item bueno (acelera), 1 para item malo (desacelera)
	 */
	private void aplicarEfectoItem(int tipo) {
		for (int i = 0; i < this.cartasHUD.length; i++) {
			if (this.cartasHUD[i] != null) {
				if (tipo == 0) { // Beneficioso
					this.cartasHUD[i].acelerarRecarga();
				} else { // Perjudicial
					this.cartasHUD[i].desacelerarRecarga();
				}
			}
		}
	}

	/**
	 * Crea un nuevo objeto 'Item' y lo añade al arreglo 'items' (Opcional 5).
	 * Se llama cuando un zombie muere (con cierta probabilidad).
	 */
	private void crearItem(double x, double y, int tipo) {
		// Buscar un espacio libre en el arreglo de items
		for (int i = 0; i < this.items.length; i++) {
			if (this.items[i] == null) {
				this.items[i] = new Item(x, y, tipo);
				break;
			}
		}
	}

	/**
	 * Método CRÍTICO de lógica.
	 * Compara todos los arreglos entre sí para ver si hay colisiones.
	 * Usa bucles 'for' anidados.
	 * * 1. Proyectiles vs Zombies
	 * 2. Zombies vs Plantas (y Tumbas)
	 * 3. BolasDeNieve vs Plantas
	 * 4. Proyectiles vs Tumbas
	 * 5. Proyectiles vs Jefe Final
	 * 6. Jefe Final vs Plantas
	 * * Cuando detecta una colisión, aplica la lógica (resta vida, elimina objetos).
	 */
	private void detectarColisiones() {

		// --- 1. Proyectiles (Bolas de Fuego) vs Zombies ---
		for (int i = 0; i < this.proyectiles.length; i++) {
			Proyectil p = this.proyectiles[i];
			if (p == null) continue; // Si el proyectil no existe, saltar

			for (int j = 0; j < this.zombies.length; j++) {
				ZombieGrinch z = this.zombies[j];
				if (z == null) continue; // Si el zombie no existe, saltar

				// Optimización: solo chequear si están en la misma fila
				if (p.getFila() == z.getFila() && colisionan(p, z)) {
					this.proyectiles[i] = null; // Destruir proyectil
					boolean murio = z.recibirDisparo(); // Zombie recibe daño

					if (murio) {
						double xZombie = z.getX();
						double yZombie = z.getY();
						int[] celdaZombie = pixelACelda(xZombie, yZombie);
						int f = celdaZombie[0];
						int c = celdaZombie[1];

						// Lógica Opcional: % de chance de soltar Tumba (Opcional 2) o Item
						// (Opcional 5)
						if (this.zombiesEliminados == 0) {
							crearItem(xZombie, yZombie, 0); // El primer kill siempre da item bueno
						} else {
							if (this.random.nextDouble() < 0.10) { // 10% chance de Tumba
								colocarTumba(f, c);
							} else if (this.random.nextDouble() < 0.15) { // 15% chance de Item
								int tipoItem = this.random.nextInt(2); // 0 o 1
								crearItem(xZombie, yZombie, tipoItem);
							}
						}

						// Eliminar zombie
						this.zombies[j] = null;
						this.zombiesEliminados++;
						this.zombiesEnPantalla--;

						// Chequear si sube de nivel
						if (this.zombiesEliminados % KILLS_POR_NIVEL == 0
								&& this.zombiesEliminados < ZOMBIES_TOTALES_A_ELIMINAR) {
							this.nivelActual++;
							System.out.println("¡NIVEL " + this.nivelActual + " ALCANZADO!");
						}
					}
					break; // El proyectil ya chocó, no puede chocar con más zombies
				}
			}
		}

		// --- 2. Zombies vs Plantas (y Tumbas) ---
		for (int j = 0; j < this.zombies.length; j++) {
			ZombieGrinch z = this.zombies[j];
			if (z == null) continue;

			// Celda donde está el zombie
			int[] celdaZombie = pixelACelda(z.getX(), z.getY());
			int fz = celdaZombie[0];
			int cz = celdaZombie[1];
			if (fz == -1 || cz == -1) continue; // Si está fuera del tablero, saltar

			Planta planta = this.tablero[fz][cz].ocupante;

			// Si hay una planta/tumba en la celda del zombie Y colisionan...
			if (planta != null && colisionan(z, planta)) {

				if (planta instanceof RoseBlade) {
					this.tablero[fz][cz].ocupante = null; // Muere instantáneo (Req 4)
				} else if (planta instanceof WallNut) {
					z.detenerse(); // El zombie deja de avanzar
					boolean murioNuez = planta.recibirDanio(1); // La nuez recibe daño
					if (murioNuez) {
						this.tablero[fz][cz].ocupante = null;
					}
				} else if (planta instanceof Tumba) {
					z.detenerse(); // El zombie deja de avanzar
					boolean murioTumba = planta.recibirDanio(1); // La tumba recibe daño
					if (murioTumba) {
						this.tablero[fz][cz].ocupante = null;
					}
				} else if (planta instanceof PlantaExplosiva) {
					double explosionX = planta.getX();
					double explosionY = planta.getY();
					this.tablero[fz][cz].ocupante = null; // La planta desaparece
					explotarEn(explosionX, explosionY); // ...y explota
				}
			}
		}

		// --- 3. BolasDeNieve vs Plantas (Opcional 1) ---
		for (int i = 0; i < this.bolasDeNieve.length; i++) {
			BolaDeNieve bola = this.bolasDeNieve[i];
			if (bola == null) continue;

			int[] celdaBola = pixelACelda(bola.getX(), bola.getY());
			int fb = celdaBola[0];
			int cb = celdaBola[1];
			if (fb == -1 || cb == -1) continue;

			Planta planta = this.tablero[fb][cb].ocupante;

			// Si hay una planta (y no es explosiva) y colisionan...
			if (planta != null && !(planta instanceof PlantaExplosiva) && colisionan(bola, planta)) {

				this.bolasDeNieve[i] = null; // Destruir bola de nieve
				boolean murioPlanta = planta.recibirDanio(1); // Planta recibe daño
				if (murioPlanta) {
					this.tablero[fb][cb].ocupante = null;
				}
				break; // La bola ya chocó
			}
		}

		// --- 4. Proyectiles (Bolas de Fuego) vs Tumbas (Opcional 2) ---
		for (int i = 0; i < this.proyectiles.length; i++) {
			Proyectil p = this.proyectiles[i];
			if (p == null) continue;

			int[] celdaProy = pixelACelda(p.getX(), p.getY());
			int fp = celdaProy[0];
			int cp = celdaProy[1];
			if (fp == -1 || cp == -1) continue;

			Planta planta = this.tablero[fp][cp].ocupante;

			// Si hay algo en la celda Y es una Tumba...
			if (planta != null && (planta instanceof Tumba)) {
				// ...y colisionan
				if (colisionan(p, planta)) {
					this.proyectiles[i] = null; // Destruir proyectil
					boolean murioObstaculo = planta.recibirDanio(1); // Tumba recibe daño
					if (murioObstaculo) {
						this.tablero[fp][cp].ocupante = null;
					}
					break; // El proyectil chocó
				}
			}
		}

		// --- 5. Proyectiles vs JEFE FINAL (Opcional 6) ---
		if (this.jefeFinal != null) {
			for (int i = 0; i < this.proyectiles.length; i++) {
				Proyectil p = this.proyectiles[i];
				if (p == null) continue;

				if (colisionan(p, this.jefeFinal)) {
					this.proyectiles[i] = null; // Destruir proyectil
					boolean murioJefe = this.jefeFinal.recibirDisparo(); // Jefe recibe daño

					if (murioJefe) {
						this.jefeFinal = null;
						this.zombiesEliminados += 100; // Suma muchos puntos para asegurar victoria
					}
					break; // Proyectil chocó
				}
			}
		}

		// --- 6. Jefe vs Plantas (Opcional 6 - CORREGIDO) ---
		if (this.jefeFinal != null) {

			// Calcular el borde frontal y trasero del jefe
			double frenteJefeX = this.jefeFinal.getX() - (this.jefeFinal.getAncho() / 2.0);
			double atrasJefeX = this.jefeFinal.getX() + (this.jefeFinal.getAncho() / 2.0);

			// Obtener la columna del frente y la de atrás
			int colFrente = pixelACelda(frenteJefeX, celdaAPixelY(0))[1];
			int colAtras = pixelACelda(atrasJefeX, celdaAPixelY(0))[1];

			// Asegurarse de que estamos dentro del tablero
			if (colFrente < 0) {
				colFrente = 0;
			}
			if (colAtras == -1 || colAtras >= COLUMNAS) {
				colAtras = COLUMNAS - 1;
			}

			// Iterar sobre TODO el rango de columnas que ocupa el jefe
			if (colFrente != -1) {
				for (int c = colFrente; c <= colAtras; c++) {
					// Iterar sobre todas las filas en esa columna
					for (int f = 0; f < FILAS; f++) {
						Planta planta = this.tablero[f][c].ocupante;

						// Si hay una planta, el jefe interactúa
						if (planta != null && colisionan(this.jefeFinal, planta)) {

							if (planta instanceof RoseBlade) {
								this.tablero[f][c].ocupante = null;
							} else if (planta instanceof PlantaExplosiva) {
								double explosionX = planta.getX();
								double explosionY = planta.getY();
								this.tablero[f][c].ocupante = null;
								explotarEn(explosionX, explosionY);
							} else if (planta instanceof WallNut || planta instanceof Tumba) {
								boolean murioObstaculo = planta.recibirDanio(1);
								if (murioObstaculo) {
									this.tablero[f][c].ocupante = null;
								}
							}
						}
					}
				}
			}
		}

	} // <-- Fin de detectarColisiones()

	/**
	 * Revisa en cada 'tick' si se cumplió una condición de fin de juego.
	 * - VICTORIA: Si 'zombiesEliminados' supera el total (incluye al jefe).
	 * - DERROTA: Si un 'ZombieGrinch' o el 'jefeFinal' llega a la columna 0.
	 * Si se cumple, activa la bandera 'juegoGanado' o 'juegoPerdido'.
	 */
	private void comprobarEstadoJuego() {
		// Condición de Victoria (Req 8)
		// (Se suma 100 al matar al jefe, así que esto funciona)
		if (this.zombiesEliminados > ZOMBIES_TOTALES_A_ELIMINAR) {
			this.juegoGanado = true;
		}

		// Si ya perdimos, no chequear de nuevo
		if (this.juegoPerdido)
			return;

		// Límite de la derrota (columna 0)
		double limiteRegalosX = OFFSET_X_TABLERO + (ANCHO_CELDA / 2.0);

		// Condición de Derrota (Zombies Normales) (Req 9)
		for (int j = 0; j < this.zombies.length; j++) {
			if (this.zombies[j] != null && this.zombies[j].getX() < limiteRegalosX) {
				this.juegoPerdido = true;
				return;
			}
		}

		// Condición de Derrota (Jefe Final) (Req 9)
		if (this.jefeFinal != null) {
			double frenteJefeX = this.jefeFinal.getX() - (this.jefeFinal.getAncho() / 2.0);
			if (frenteJefeX < limiteRegalosX) {
				this.juegoPerdido = true;
			}
		}
	}

	// -----------------------------------------------------------------
	// 6. MÉTODOS DE DIBUJO (Llamados desde tick())
	// -----------------------------------------------------------------

	/**
	 * Dibuja la Interfaz de Usuario (HUD) en la parte superior.
	 * Dibuja el panel de fondo, las cartas de plantas (con su recarga),
	 * las estadísticas (Nivel, Kills, Tiempo) y las cartas de zombies.
	 * (Usa tildes y 'ñ' en los textos para el usuario).
	 */
	private void dibujarHUD() {

		// 1. Dibuja el panel de fondo del HUD
		if (this.imgPanelStats != null) {
			entorno.dibujarImagen(this.imgPanelStats, entorno.ancho() / 2, ALTO_HUD / 2, 0, 1.0);
		} else {
			// Fallback (barra gris si no carga la imagen)
			entorno.dibujarRectangulo(entorno.ancho() / 2, ALTO_HUD / 2, entorno.ancho(), ALTO_HUD, 0,
					new Color(50, 50, 50));
		}

		String nombreFuente = "Impact";
		long tiempoActual = System.currentTimeMillis();

		// 2. Dibuja las cartas de PLANTAS (Izquierda)
		for (int i = 0; i < this.cartasHUD.length; i++) {
			if (this.cartasHUD[i] != null) {
				this.cartasHUD[i].dibujar(entorno, tiempoActual);
			}
		}

		// 3. Dibuja las ESTADÍSTICAS (Centro, sobre el cartel)
		try {
			entorno.cambiarFont(nombreFuente, 18, Color.WHITE);
		} catch (Exception e) {
			entorno.cambiarFont("Arial", 16, Color.WHITE); // Fuente de respaldo
		}

		// Calcular los valores a mostrar
		long tiempoTranscurridoSeg = (tiempoActual - this.tiempoInicioJuego) / 1000;
		int zombiesRestantes = ZOMBIES_TOTALES_A_ELIMINAR - this.zombiesEliminados;
		if (zombiesRestantes < 0)
			zombiesRestantes = 0;

		// Posición X (horizontal) de los textos
		int centroStatsX = 350;

		// Escribir los textos (¡con tildes!)
		entorno.escribirTexto("NIVEL: " + this.nivelActual, centroStatsX, 30);
		entorno.escribirTexto("ELIMINADOS: " + this.zombiesEliminados, centroStatsX, 48);
		entorno.escribirTexto("RESTANTES: " + zombiesRestantes, centroStatsX, 66);
		entorno.escribirTexto("TIEMPO: " + tiempoTranscurridoSeg, centroStatsX, 84);

		// 4. Dibuja las CARTAS DE ZOMBIES (Derecha)
		double panelZombiesY = 50; // Posición Y (vertical)
		double xCartaNormal = 550;
		double xCartaTanque = 650;
		double xCartaColosal = 750;

		if (this.imgCartaZombieNormal != null) {
			entorno.dibujarImagen(this.imgCartaZombieNormal, xCartaNormal, panelZombiesY, 0, this.escalaZombieNormal);
		}
		if (this.imgCartaZombieTanque != null) {
			entorno.dibujarImagen(this.imgCartaZombieTanque, xCartaTanque, panelZombiesY, 0, this.escalaZombieTanque);
		}
		if (this.imgCartaZombieColosal != null) {
			entorno.dibujarImagen(this.imgCartaZombieColosal, xCartaColosal, panelZombiesY, 0, this.escalaZombieColosal);
		}
	}

	/**
	 * Método principal de dibujo. Se llama en CADA 'tick'.
	 * Dibuja todos los componentes del juego en orden,
	 * como si fueran capas (de atrás para adelante).
	 */
	private void dibujarEscena() {
		// Capa 1: Fondo
		entorno.dibujarImagen(imgFondo, entorno.ancho() / 2, entorno.alto() / 2, 0, 1.0);

		// Capa 2: HUD (Barra superior)
		dibujarHUD();

		// Capa 3: Tablero (El pasto)
		dibujarTablero();

		// Capa 4: Regalos (Primera columna)
		dibujarRegalos();

		// Capa 5: Entidades (Plantas, Zombies, Tumbas, Proyectiles)
		dibujarPlantas();
		dibujarZombies();
		dibujarProyectiles();
		dibujarBolasDeNieve(); // (Opcional 1)

		// Capa 6: Jefe (Opcional 6)
		if (this.jefeFinal != null) {
			this.jefeFinal.dibujar(entorno);
		}

		// Capa 7: Items (Opcional 5)
		dibujarItems();

		// Capa 8: Input Visual (Mouse)
		// Dibuja la "imagen fantasma" de la planta que estamos arrastrando
		if (this.plantaArrastrando != null) {
			double escalaFantasma = 0.2; // La imagen se achica al arrastrar
			entorno.dibujarImagen(
					this.plantaArrastrando.getImagen(),
					entorno.mouseX(),
					entorno.mouseY(),
					0,
					escalaFantasma);
		}

		// Capa 9: Efectos Visuales (Explosión)
		dibujarEfectoExplosion(); // (Opcional 3)
	}

	/**
	 * Dibuja los regalos.
	 */
	private void dibujarRegalos() {
		for (int f = 0; f < this.regalos.length; f++) {
			if (this.regalos[f] != null) {
				this.regalos[f].dibujar();
			}
		}
	}

	/**
	 * Dibuja las casillas de pasto.
	 */
	private void dibujarTablero() {
		for (int f = 0; f < FILAS; f++) {
			for (int c = 0; c < COLUMNAS; c++) {
				if (this.tablero[f][c] != null) {
					this.tablero[f][c].dibujar(entorno);
				}
			}
		}
	}

	/**
	 * Dibuja todas las plantas (y tumbas) que están en el tablero.
	 * También dibuja un círculo amarillo de "selección" si
	 * 'plantaSeleccionadaParaMover' es esta planta.
	 */
	private void dibujarPlantas() {
		for (int f = 0; f < FILAS; f++) {
			for (int c = 0; c < COLUMNAS; c++) {
				Planta p = this.tablero[f][c].ocupante;
				if (p != null) {
					// Dibujar resplandor si está seleccionada (Req 3)
					if (p == this.plantaSeleccionadaParaMover) {
						if (p instanceof WallNut) {
							entorno.dibujarCirculo(p.getX(), p.getY(), 45, Color.YELLOW);
						} else if (p instanceof RoseBlade) {
							entorno.dibujarCirculo(p.getX(), p.getY(), 40, Color.YELLOW);
						} else if (p instanceof PlantaExplosiva) {
							entorno.dibujarCirculo(p.getX(), p.getY(), 40, Color.YELLOW);
						}
					}
					// Dibuja la planta (o tumba)
					p.dibujar(entorno);
				}
			}
		}
	}

	/**
	 * Dibuja todos los zombies.
	 */
	private void dibujarZombies() {
		for (int i = 0; i < this.zombies.length; i++) {
			if (this.zombies[i] != null) {
				this.zombies[i].dibujar(entorno);
			}
		}
	}

	/**
	 * Dibuja todos los proyectiles (bolas de fuego).
	 */
	private void dibujarProyectiles() {
		for (int i = 0; i < this.proyectiles.length; i++) {
			if (this.proyectiles[i] != null) {
				this.proyectiles[i].dibujar(entorno);
			}
		}
	}

	/**
	 * Dibuja todas las bolas de nieve (Opcional 1).
	 */
	private void dibujarBolasDeNieve() {
		for (int i = 0; i < this.bolasDeNieve.length; i++) {
			if (this.bolasDeNieve[i] != null) {
				this.bolasDeNieve[i].dibujar(entorno);
			}
		}
	}

	/**
	 * Dibuja todos los items en el suelo (Opcional 5).
	 */
	private void dibujarItems() {
		for (int i = 0; i < this.items.length; i++) {
			if (this.items[i] != null) {
				this.items[i].dibujar(entorno);
			}
		}
	}

	/**
	 * Dibuja la imagen de explosión (Opcional 3)
	 * si 'duracionExplosion' es mayor a cero.
	 */
	private void dibujarEfectoExplosion() {
		if (this.duracionExplosion > 0 && this.imgExplosion != null) {

			double diametroDeseado = this.ANCHO_CELDA * 3; // Explosión de 3x3 celdas
			int anchoOriginal = this.imgExplosion.getWidth(null);

			if (anchoOriginal > 0) {
				double escala = diametroDeseado / anchoOriginal;
				entorno.dibujarImagen(this.imgExplosion, this.explosionX, this.explosionY, 0, escala);
			}
		}
	}

	/**
	 * Dibuja el texto de "¡GANASTE!" o "¡PERDISTE!"
	 * Se llama desde 'tick()' solo cuando el juego ha terminado.
	 */
	private void dibujarFinDeJuego() {
		entorno.cambiarFont("Arial", 50, Color.BLACK);
		if (this.juegoGanado) {
			entorno.escribirTexto("¡GANASTE!", 270, 290); // Sombra
			entorno.cambiarFont("Arial", 50, Color.GREEN);
			entorno.escribirTexto("¡GANASTE!", 272, 292); // Texto
		}
		if (this.juegoPerdido) {
			entorno.escribirTexto("¡PERDISTE!", 270, 290); // Sombra
			entorno.cambiarFont("Arial", 50, Color.RED);
			entorno.escribirTexto("¡PERDISTE!", 272, 292); // Texto
		}
	}

	// -----------------------------------------------------------------
	// 7. MÉTODOS AUXILIARES (Utilidades)
	// -----------------------------------------------------------------
	// Esta sección contiene "herramientas" matemáticas para que
	// el resto del código sea más limpio y legible.
	// -----------------------------------------------------------------

	/**
	 * Método auxiliar para calcular la escala correcta de una imagen
	 * para que quepa en un tamaño deseado sin deformarse.
	 *
	 * @param img           La imagen (Image) a escalar.
	 * @param anchoDeseado  El ancho máximo (double) que debe tener.
	 * @param altoDeseado   El alto máximo (double) que debe tener.
	 * @return La escala (double) que se debe usar en 'entorno.dibujarImagen()'.
	 */
	private double calcularEscalaImagen(Image img, double anchoDeseado, double altoDeseado) {
		if (img == null) {
			return 1.0;
		}
		int anchoOriginal = img.getWidth(null);
		int altoOriginal = img.getHeight(null);
		if (anchoOriginal <= 0 || altoOriginal <= 0) {
			return 1.0;
		}
		double escalaAncho = anchoDeseado / anchoOriginal;
		double escalaAlto = altoDeseado / altoOriginal;

		// Usa la escala más pequeña para que la imagen quepa sin deformarse
		return Math.min(escalaAncho, escalaAlto);
	}

	/**
	 * Convierte coordenadas de píxeles (double x, double y) a coordenadas de
	 * celda (int[] {fila, columna}).
	 * Esencial para saber dónde hizo clic el mouse.
	 *
	 * @param x Coordenada X del píxel (ej. entorno.mouseX()).
	 * @param y Coordenada Y del píxel (ej. entorno.mouseY()).
	 * @return Un arreglo de int {fila, columna}. Devuelve {-1, -1} si está fuera
	 * del tablero.
	 */
	private int[] pixelACelda(double x, double y) {
		// Chequear si está fuera de los límites verticales
		if (y < OFFSET_Y_TABLERO || y >= OFFSET_Y_TABLERO + FILAS * ALTO_CELDA) {
			return new int[] { -1, -1 };
		}
		// Chequear si está fuera de los límites horizontales
		if (x < OFFSET_X_TABLERO || x >= OFFSET_X_TABLERO + COLUMNAS * ANCHO_CELDA) {
			return new int[] { -1, -1 };
		}
		// Calcular la celda
		int f = (int) ((y - OFFSET_Y_TABLERO) / ALTO_CELDA);
		int c = (int) ((x - OFFSET_X_TABLERO) / ANCHO_CELDA);
		return new int[] { f, c };
	}

	/**
	 * Convierte una columna (int col) a la coordenada X del *centro* de esa celda.
	 * * @param col La columna (0 a 8).
	 * @return La coordenada X (double) en píxeles.
	 */
	private double celdaAPixelX(int col) {
		return OFFSET_X_TABLERO + (col * ANCHO_CELDA) + (ANCHO_CELDA / 2.0);
	}

	/**
	 * Convierte una fila (int fila) a la coordenada Y del *centro* de esa celda.
	 * * @param fila La fila (0 a 4).
	 * @return La coordenada Y (double) en píxeles.
	 */
	private double celdaAPixelY(int fila) {
		return OFFSET_Y_TABLERO + (fila * ALTO_CELDA) + (ALTO_CELDA / 2.0);
	}

	/**
	 * Verifica si una celda (fila, columna) es un lugar válido para plantar.
	 * No se puede plantar en:
	 * - Fuera del tablero.
	 * - Columna 0 (donde están los regalos).
	 * - Celdas ya ocupadas por otra planta o tumba.
	 *
	 * @param f La fila a chequear.
	 * @param c La columna a chequear.
	 * @return 'true' si es válido, 'false' si no.
	 */
	private boolean esCeldaValidaParaPlantar(int f, int c) {
		// Fuera de límites
		if (f < 0 || f >= FILAS || c < 0 || c >= COLUMNAS) {
			return false;
		}
		// Columna de regalos (Req 9)
		if (c == 0) {
			return false;
		}
		// Casilla ocupada (Req 2)
		if (this.tablero[f][c].ocupante != null) {
			return false;
		}
		// Si pasa todo, es válida
		return true;
	}

	// --- Métodos de Detección de Colisión ---
	// Usan una lógica simple de "distancia entre centros".
	// Devuelven 'true' si la distancia es menor a un umbral.

	/**
	 * Chequea colisión entre Proyectil (bola de fuego) y Zombie.
	 */
	private boolean colisionan(Proyectil p, ZombieGrinch z) {
		if (p == null || z == null) {
			return false;
		}
		double distancia = Math.sqrt(
				Math.pow(p.getX() - z.getX(), 2) +
						Math.pow(p.getY() - z.getY(), 2));
		return distancia < 40; // Umbral de 40 píxeles
	}

	/**
	 * Chequea colisión entre Zombie y Planta (o Tumba).
	 */
	private boolean colisionan(ZombieGrinch z, Planta p) {
		if (z == null || p == null) {
			return false;
		}
		double distancia = Math.sqrt(
				Math.pow(z.getX() - p.getX(), 2) +
						Math.pow(z.getY() - p.getY(), 2));
		return distancia < 45; // Umbral más grande (son más anchos)
	}

	/**
	 * Chequea colisión entre el Jefe Colosal (rectángulo) y una Planta (punto).
	 * (Colisión Rectángulo-Punto)
	 */
	private boolean colisionan(ZombieColosal z, Planta p) {
		if (z == null || p == null) {
			return false;
		}
		// Chequea si el (x,y) de la planta está "dentro" del rectángulo del jefe
		return Math.abs(p.getX() - z.getX()) < (z.getAncho() / 2 + ANCHO_CELDA / 2) &&
				Math.abs(p.getY() - z.getY()) < (500 / 2 + ALTO_CELDA / 2); // 500 = alto del jefe
	}

	/**
	 * Chequea colisión entre BolaDeNieve y Planta.
	 */
	private boolean colisionan(BolaDeNieve b, Planta p) {
		if (b == null || p == null) {
			return false;
		}
		double distancia = Math.sqrt(
				Math.pow(b.getX() - p.getX(), 2) +
						Math.pow(b.getY() - p.getY(), 2));
		return distancia < 35; // Umbral pequeño
	}

	/**
	 * Chequea colisión entre Proyectil (bola de fuego) y Planta (para Tumbas).
	 */
	private boolean colisionan(Proyectil p, Planta pl) {
		if (p == null || pl == null) {
			return false;
		}
		double distancia = Math.sqrt(
				Math.pow(p.getX() - pl.getX(), 2) +
						Math.pow(p.getY() - pl.getY(), 2));
		return distancia < 35; // Umbral pequeño
	}

	/**
	 * Chequea colisión entre Proyectil (punto) y Jefe Colosal (rectángulo).
	 */
	private boolean colisionan(Proyectil p, ZombieColosal z) {
		if (p == null || z == null) {
			return false;
		}
		// Chequea si el (x,y) del proyectil está "dentro" del rectángulo del jefe
		return Math.abs(p.getX() - z.getX()) < z.getAncho() / 2 &&
				Math.abs(p.getY() - z.getY()) < 500 / 2; // 500 = alto del jefe
	}

	// -----------------------------------------------------------------
	// MÉTODO MAIN (Punto de entrada)
	// -----------------------------------------------------------------

	/**
	 * Método main (Punto de entrada de la aplicación).
	 * Lo único que hace es crear una nueva instancia de 'Juego()',
	 * lo cual dispara el Constructor y arranca todo.
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Juego juego = new Juego();
	}

} // --- FIN DE LA CLASE JUEGO ---