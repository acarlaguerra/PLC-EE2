import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class Aviao implements Comparable<Aviao> {

    private Aeroporto aeroporto;
    private int id;
    private long horaEsperada;
    private long horaExecutada;
    private boolean ehDecolagem;

    public Aviao(Aeroporto aeroporto, int id, long horaEsperada, boolean ehDecolagem) {
        this.aeroporto = aeroporto;
        this.id = id;
        this.horaEsperada = horaEsperada;
        this.ehDecolagem = ehDecolagem;
    }

    public void run() {
        try {
            aeroporto.programarVoo(this);

            long espera = getHoraEsperada() - System.currentTimeMillis();
            if (espera > 0) {
                TimeUnit.MILLISECONDS.sleep(espera);
            }

            aeroporto.acessarPista(this);
        } catch (InterruptedException e) {
            System.out.format("Avião id %d foi interrompido enquanto aguardava!%n", id);
        }
    }

    public void vooExecutado(long horaExecutada) {
    setHoraExecutada(horaExecutada);
    
    long atraso = horaExecutada - horaEsperada;
    
    String tipo = tipoVoo();
    String sHoraPrev = String.format("%tR", horaEsperada); // Exibe "HH:MM"
    String sHoraExec = String.format("%tR", horaExecutada); // Exibe "HH:MM"
    String sAtraso = String.format("%tR", Math.abs(atraso)); // Exibe "HH:MM"

    System.out.format("%s do Avião id %d feita!%n" +
                    "  Previsão: %d - %s%n" +
                    "  Voo Cumprido: %d - %s%n" +
                    "  Tempo de Atraso: %d - %s%n", 
                    tipo, id, horaEsperada, sHoraPrev, horaExecutada, sHoraExec, atraso, sAtraso);

}


    public String toString() {
        return String.format("Avião %d - %s >> %d - %s", id, tipoVoo(), horaEsperada,
                String.format("%1$tH:%1$tM:%1$tS.%1$tL", horaEsperada));
    }

    public String tipoVoo() {
        return ehDecolagem ? "Decolagem" : "Aterrissagem";
    }

    public int compareTo(Aviao aviao) {
        return Long.compare(this.horaEsperada, aviao.getHoraEsperada());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getHoraEsperada() {
        return horaEsperada;
    }

    public void sethoraEsperada(long horaEsperada) {
        this.horaEsperada = horaEsperada;
    }

    public long getHoraExecutada() {
        return horaExecutada;
    }

    public void setHoraExecutada(long horaExecutada) {
        this.horaExecutada = horaExecutada;
    }

    public boolean ehDecolagem() {
        return ehDecolagem;
    }

    public void setPartida(boolean ehDecolagem) {
        this.ehDecolagem = ehDecolagem;
    }
}

class Aeroporto {

    private int pistas;
    private PriorityQueue<Aviao> fila;
    private long tempoOcupado = 500;
    private ReentrantLock lock = new ReentrantLock();
    private Condition pistaLivre = lock.newCondition();

    public Aeroporto(int pistas) {
        this.pistas = pistas;
        this.fila = new PriorityQueue<>();
    }

    public void programarVoo(Aviao aviao) {
        lock.lock();
        try {
            fila.offer(aviao); // Usamos offer para adicionar e manter a fila ordenada automaticamente
            System.out.println("Programação: " + aviao);
        } finally {
            lock.unlock();
        }
    }

    public void acessarPista(Aviao aviao) {
        lock.lock();
        try {
            while (pistas == 0 || !fila.peek().equals(aviao)) {
                System.out.println("\n...aguardando liberação da pista...\n");
                pistaLivre.await();
            }

            pistas--;
            fila.poll().vooExecutado(System.currentTimeMillis());
            Thread.sleep(tempoOcupado);
            pistas++;
            pistaLivre.signalAll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura a interrupção
        } finally {
            lock.unlock();
        }
    }
}


public class PistaDeControle {
    public static void main(String[] args) throws InterruptedException {
        int avioesPartida;
        int avioesChegada;
        int pistas;
        long timeSpan = 20000; // intervalo para distribuição dos voos em milissegundos
        long offset = 2000; // offset para início dos voos

        avioesPartida = readIntFromUser();

        avioesChegada = readIntFromUser();

        pistas = readIntFromUser();

        long inicio = System.currentTimeMillis();
        System.out.println("\nFoi dada a largada!\nHorário: " + getTimeAsString(inicio) + "\n");

        Aeroporto aeroporto = new Aeroporto(pistas);
        List<Thread> threads = new ArrayList<>();
        Random random = new Random();

        for (int p = 0; p < avioesPartida; p++) {
            final int partidaIndex = p; // Crie uma cópia final da variável p
            long hora = System.currentTimeMillis() + offset + (Math.abs(random.nextLong() % timeSpan));
            threads.add(new Thread(() -> {
                Aviao aviao = new Aviao(aeroporto, partidaIndex, hora, true);
                aviao.run();
            }));
        }

        for (int c = 0; c < avioesChegada; c++) {
            final int chegadaIndex = c; // Crie uma cópia final da variável c
            long hora = System.currentTimeMillis() + offset + (Math.abs(random.nextLong() % timeSpan));
            threads.add(new Thread(() -> {
                Aviao aviao = new Aviao(aeroporto, chegadaIndex + avioesPartida, hora, false);
                aviao.run();
            }));
        }

        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        long fim = System.currentTimeMillis();
        System.out.println("\nDesligando as asas!\nHorário: " + getTimeAsString(fim));
        System.out.println("Tempo de execução total: " + (fim - inicio) + " ms");
    }

    private static int readIntFromUser() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }

    private static String getTimeAsString(long timeInMillis) {
        return String.format("%tT.%tL", timeInMillis, timeInMillis);
    }
}
