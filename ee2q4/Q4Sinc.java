// Q4: Implementação síncrona em Java (pingpong)
package ee2q4;

public class Q4Sinc {

    public static final Object lock = new Object(); // Bloqueio para garantir a sincronização das threads
    public static boolean liberada = true; // Variável que controla a interação entre as threads
    public static int n = 50; // Número de mensagens enviadas

    public static void main(String[] args) {

        class EnviaMsgSinc extends Thread { // Criando uma thread para o envio de mensagens
            @Override
            public void run() {
                try {
                    for (int i = 0; i < n; i++) {
                        synchronized (lock) { // Garantindo que esse código só será executado por uma thread por vez
                            while (!liberada) {
                                lock.wait();
                            }
                            System.out.println("Mensagem " + i + ": Taylor");
                            liberada = false;
                            lock.notify();
                        }
                    }
                } catch (InterruptedException exc) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        class RespondeMsgSinc extends Thread {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < n; i++) {
                        synchronized (lock) {
                            while (liberada) {
                                lock.wait();
                            }
                            System.out.println("Resposta " + i + ": Swift");
                            liberada = true;
                            lock.notify();
                        }
                    }
                } catch (InterruptedException exc) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        EnviaMsgSinc envia = new EnviaMsgSinc();
        RespondeMsgSinc responde = new RespondeMsgSinc();

        envia.start();
        responde.start();    
    }
}
