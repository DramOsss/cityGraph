//package citygraph;
//
//import citygraph.algorithms.Dijkstra;
//import citygraph.graph.GrafoTransporte;
//import citygraph.model.CriterioOptimizacion;
//import citygraph.model.Parada;
//import citygraph.model.ResultadoRuta;
//import citygraph.model.Ruta;
//
//public class MainTest {
//
//    public static void main(String[] args) {
//
//        GrafoTransporte grafo = new GrafoTransporte();
//
//        // 1) Paradas
//        grafo.agregarParada(new Parada("A", "Centro"));
//        grafo.agregarParada(new Parada("B", "Hospital"));
//        grafo.agregarParada(new Parada("C", "Universidad"));
//        grafo.agregarParada(new Parada("D", "Aeropuerto"));
//
//        // 2) Rutas
//        grafo.agregarRuta(new Ruta("A", "B", 5, 2.0, 35, 0));
//        grafo.agregarRuta(new Ruta("A", "C", 3, 1.5, 25, 0));
//        grafo.agregarRuta(new Ruta("C", "B", 2, 1.0, 15, 0));
//        grafo.agregarRuta(new Ruta("B", "D", 6, 3.0, 40, 1));
//        grafo.agregarRuta(new Ruta("C", "D", 10, 6.0, 50, 1));
//
//
//        System.out.println("Número de paradas: " + grafo.numeroParadas());
//        System.out.println("Número de rutas: " + grafo.numeroRutas());
//
//
//        ResultadoRuta porTiempo = Dijkstra.calcular(grafo, "A", "D", CriterioOptimizacion.TIEMPO);
//        System.out.println("\nDijkstra (TIEMPO) A->D: " + porTiempo);
//
//        ResultadoRuta porDistancia = Dijkstra.calcular(grafo, "A", "D", CriterioOptimizacion.DISTANCIA);
//        System.out.println("Dijkstra (DISTANCIA) A->D: " + porDistancia);
//
//        ResultadoRuta porCosto = Dijkstra.calcular(grafo, "A", "D", CriterioOptimizacion.COSTO);
//        System.out.println("Dijkstra (COSTO) A->D: " + porCosto);
//
//        ResultadoRuta porTransbordos = Dijkstra.calcular(grafo, "A", "D", CriterioOptimizacion.TRANSBORDOS);
//        System.out.println("Dijkstra (TRANSBORDOS) A->D: " + porTransbordos);
//
//        // Ruta inexistente (por ejemplo: D -> A no existe en tu grafo)
//        ResultadoRuta inexistente = Dijkstra.calcular(grafo, "D", "A", CriterioOptimizacion.TIEMPO);
//        System.out.println("\nDijkstra (TIEMPO) D->A (inexistente): " + inexistente);
//
//        // 5) Vecinos
//        System.out.println("\nVecinos de A:");
//        grafo.vecinosDe("A").forEach(System.out::println);
//
//        // 6) Existe arista
//        System.out.println("\n¿Existe ruta A -> B? " + grafo.existeArista("A", "B"));
//        System.out.println("¿Existe ruta B -> C? " + grafo.existeArista("B", "C"));
//
//        System.out.println("\nModificando parada B (nuevo nombre: 'Hospital Central')...");
//        grafo.modificarParada("B", "Hospital Central", null, null);
//        System.out.println("Parada B: " + grafo.obtenerParada("B"));
//
//        System.out.println("\nModificando ruta B -> D (tiempo 6 -> 2, costo 40 -> 10)...");
//        grafo.modificarRuta("B", "D", 2.0, null, 10.0, null);
//
//        ResultadoRuta tiempoNuevo = Dijkstra.calcular(grafo, "A", "D", CriterioOptimizacion.TIEMPO);
//        System.out.println("Dijkstra (TIEMPO) A->D después de modificar B->D: " + tiempoNuevo);
//
//        // 7) Eliminar ruta
//        grafo.eliminarRuta("A", "B");
//        System.out.println("\nDespués de eliminar A -> B:");
//        grafo.vecinosDe("A").forEach(System.out::println);
//
//        System.out.println("\nEliminando parada C...");
//        grafo.eliminarParada("C");
//
//        System.out.println("Número de paradas: " + grafo.numeroParadas());
//        System.out.println("Número de rutas: " + grafo.numeroRutas());
//
//        System.out.println("\nVecinos de A (después de eliminar C):");
//        grafo.vecinosDe("A").forEach(System.out::println);
//
//
//        System.out.println("\n¿Existe ruta A -> C? " + grafo.existeArista("A", "C"));
//
//    }
//}