package citygraph.algorithms;

import citygraph.graph.GrafoTransporte;
import citygraph.model.CriterioOptimizacion;
import citygraph.model.Ruta;

import java.util.*;

public class Dfs {

    /**
     * Clase interna auxiliar para guardar:
     * - el camino encontrado
     * - el peso total de ese camino
     *
     * Se declara dentro de Dfs porque solo se usa aquí.
     */
    private static class CaminoDFS {
        private final List<String> camino;
        private final double peso;

        public CaminoDFS(List<String> camino, double peso) {
            this.camino = camino;
            this.peso = peso;
        }

        public List<String> getCamino() {
            return camino;
        }

        public double getPeso() {
            return peso;
        }
    }


    /**
     * Busca caminos alternativos entre origen y destino usando DFS.
     * Luego los ordena por peso y devuelve solo los mejores.
     *
     * @param origenId ID de la parada de origen
     * @param destinoId ID de la parada de destino
     * @param criterio criterio de optimización
     * @param maxAlternativos cantidad máxima de caminos a devolver
     * @return lista de caminos alternativos ordenados por peso
     */
    public static List<List<String>> calcularCaminosDFS(GrafoTransporte grafo,
                                                        String origenId,
                                                        String destinoId,
                                                        CriterioOptimizacion criterio,
                                                        int maxAlternativos) {

        // Lista de candidatos encontrados con su peso
        List<CaminoDFS> candidatos = new ArrayList<>();

        // NUEVO: evita caminos repetidos
        Set<List<String>> caminosUnicos = new HashSet<>();

        // Camino actual en construcción
        List<String> caminoActual = new ArrayList<>();
        caminoActual.add(origenId);

        // Conjunto para evitar repetir nodos
        Set<String> visitados = new HashSet<>();
        visitados.add(origenId);

        // Inicia la búsqueda DFS
        dfsBuscarCaminos(grafo,origenId, destinoId, criterio, visitados, caminoActual,candidatos, caminosUnicos);

        // Ordena los caminos del menor peso al mayor
        candidatos.sort(Comparator.comparingDouble(CaminoDFS::getPeso));

        // Lista final con solo los caminos que se devolverán
        List<List<String>> resultado = new ArrayList<>();

        for (CaminoDFS c : candidatos) {
            if (resultado.size() >= maxAlternativos) {
                break;
            }
            resultado.add(c.getCamino());
        }

        return resultado;
    }

    /**
     * Método recursivo que explora todos los caminos posibles
     * sin repetir nodos.
     *
     * @param actual nodo actual
     * @param destino nodo destino
     * @param criterio criterio de optimización
     * @param visitados nodos ya visitados en esta rama
     * @param caminoActual camino que se está construyendo
     */
    private static void dfsBuscarCaminos(GrafoTransporte grafo,
                                         String actual,
                                         String destino,
                                         CriterioOptimizacion criterio,
                                         Set<String> visitados,
                                         List<String> caminoActual,
                                         List<CaminoDFS> resultados,
                                         Set<List<String>> caminosUnicos) {


        // Caso base: si llegamos al destino, guardamos el camino
        if (actual.equals(destino)) {
            double peso = calcularPesoDFS(grafo,caminoActual, criterio);

            // Copia del camino actual
            List<String> nuevoCamino = new ArrayList<>(caminoActual);

            // NUEVO: solo se agrega si no existe todavía
            if (caminosUnicos.add(nuevoCamino)) {
                resultados.add(new CaminoDFS(nuevoCamino, peso));
            }

            return;
        }

        // Explora cada vecino del nodo actual
        for (Ruta ruta : grafo.vecinosDe(actual)) {
            String siguiente = ruta.getDestinoId();

            // Si ya fue visitado, se omite para evitar ciclos
            if (visitados.contains(siguiente)) {
                continue;
            }

            // Se agrega el nodo al camino actual
            visitados.add(siguiente);
            caminoActual.add(siguiente);

            // Llamada recursiva
            dfsBuscarCaminos(grafo,siguiente, destino, criterio, visitados, caminoActual,resultados,caminosUnicos);

            // Backtracking: deshacer cambios para probar otra rama
            caminoActual.remove(caminoActual.size() - 1);
            visitados.remove(siguiente);
        }
    }

    /**
     * Calcula el peso total de un camino según el criterio elegido.
     *
     * @param camino lista de IDs que forman el camino
     * @param criterio criterio usado para calcular el peso
     * @return peso total del camino
     */
    private static double calcularPesoDFS(GrafoTransporte grafo, List<String> camino, CriterioOptimizacion criterio) {
        double total = 0.0;

        for (int i = 0; i < camino.size() - 1; i++) {
            Ruta ruta = grafo.obtenerRuta(camino.get(i), camino.get(i + 1));

            total += switch (criterio) {
                case TIEMPO -> ruta.getTiempoMin();
                case DISTANCIA -> ruta.getDistanciaKm();
                case COSTO -> ruta.getCosto();
                case TRANSBORDOS -> 1.0;
            };
        }

        return total;
    }
}