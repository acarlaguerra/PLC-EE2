// import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.Executor;


class AbelhaRainha {
    // criação das listas de Tarefas Designadas usadas para atribuir a algum operário
    private Queue<Tarefa> queueTarefas;
    private Map<Integer, Tarefa> tarefasConcluidas;
    private ExecutorService executorService;
    private int qtdOperarios;

    public AbelhaRainha(List<Tarefa> listaTarefas, int qtdOperarios){
        // a lista passada anteriormente será uma fila agora
        this.queueTarefas = new LinkedBlockingQueue<>(listaTarefas);
        this.tarefasConcluidas = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(qtdOperarios);
    }

    public void atribuiTarefa() {
        while (!queueTarefas.isEmpty()){
            Tarefa tarefa = queueTarefas.poll();
            if (dependenciasConcluidas(tarefa)) {
                executorService.execute(new Operario(tarefa, tarefasConcluidas));
            } else {
                queueTarefas.offer(tarefa);
            }
        }
    }
    public Queue<Tarefa> getQueueTarefas() {
        return queueTarefas;
    }

    public Map<Integer, Tarefa> getTarefasConcluidas() {
        return tarefasConcluidas;
    }

    private boolean dependenciasConcluidas(Tarefa tarefa){
        List<Integer> dependencias = tarefa.getListaDependencias();

        if(dependencias.isEmpty()){
            return true;
        }

        for (Integer dependenciaId : dependencias) {
            if (!verificarTarefaConcluida(dependenciaId)) {
                return false;
            }    
        }
        // Se todas as dependências foram concluídas, retorna verdadeiro.
        return true;
    }

    private boolean verificarTarefaConcluida(int tarefaId) {
        Tarefa tarefa = tarefasConcluidas.get(tarefaId);
        return tarefa != null && tarefa.isConcluida();
    }
}

class Operario implements Runnable{
    private Tarefa tarefa;
    private Map<Integer, Tarefa> tarefasConcluidas;

    public Operario(Tarefa tarefa, Map<Integer, Tarefa> tarefasConcluidas) {
        this.tarefa = tarefa;
        this.tarefasConcluidas = tarefasConcluidas;
    }

    // public Operario(AbelhaRainha rainha) {
    // }

    @Override
    public void run() {
        // Executa a tarefa, a Rainha já fez essa verificação.
        executarTarefa();

        // Marca a tarefa como concluída após a execução.
        tarefa.setConcluida();

        // Adiciona a tarefa à lista de tarefas concluídas
        tarefasConcluidas.put(tarefa.getId(), tarefa);

        System.out.println("Tarefa " + tarefa.getId() + " feita");
    }

    // Método para executar da tarefa
    private void executarTarefa() {
        System.out.println("Executando a tarefa " + tarefa.getId());
        try {
            Thread.sleep(tarefa.getTempoConclusao());
        } catch (InterruptedException e) {
        }
    }
}

class Tarefa{
    //criação das Variáveis Associadas: id, tempo e lista de dependências
    private int id;
    private long tempoConclusao;
    private List<Integer> listaDependencias;
    private boolean concluida;

    public Tarefa(int id, long tempoConclusao, List<Integer> listaDependencias) {
        this.id = id;
        this.tempoConclusao = tempoConclusao;
        this.listaDependencias = listaDependencias;
        this.concluida = false;
    }

    // definindo get e set
    public int getId() {
        return id;
    }

    public long getTempoConclusao() {
        return tempoConclusao;
    }

    public List<Integer> getListaDependencias() {
        return listaDependencias;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida() {
        this.concluida = true;
    }

}

public class Colmain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // lê a quantidade de Operários e Tarefas designadas
        int qtdOperarios = scanner.nextInt();
        int qtdTarefas = scanner.nextInt();
        List<Tarefa> listaTarefas = new ArrayList<>();

        // cria a lista de Tarefas com suas respectivas variáveis id, tempo e dep
        for (int i = 0; i < qtdTarefas; i++) {
            int idTarefa = scanner.nextInt();
            long tempoConclusao = scanner.nextLong();
            List<Integer> dependencias = new ArrayList<>();

            int idDependencia;
            while ((idDependencia = scanner.nextInt()) >= 0) {
                dependencias.add(idDependencia);
            }

            Tarefa tarefa = new Tarefa(idTarefa, tempoConclusao, dependencias);
            listaTarefas.add(tarefa);
        }

        // Criação da ThreadPool atribuindo aos operários
        ExecutorService threadp = Executors.newFixedThreadPool(qtdOperarios);

        AbelhaRainha rainha = new AbelhaRainha(listaTarefas, qtdOperarios);

        // Loop para atribuir Operários a ThreadPool
        for (int k = 0; k < qtdOperarios; k++) {
            rainha.atribuiTarefa();
        }

        // Encerra a ThreadPool
        threadp.shutdown();

        // Aguarda a finalização da ThreadPool
        while (!threadp.isTerminated()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
    }
}
