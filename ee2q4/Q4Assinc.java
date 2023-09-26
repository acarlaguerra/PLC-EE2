// Q4: Implementação assíncrona em Java (pingping)

package ee2q4;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Q4Assinc {

    public static void main(String[] args) {
        int n = 50; // Número total de mensagens a serem enviadas
        BlockingQueue<Integer> mensagens = new LinkedBlockingQueue<>(n); // Fila de tamanho n para as mensagens que estiverem aguardando uma resposta
        

        class EnviaMsgAssinc extends Thread { // Criando uma thread para o envio de mensagens
            @Override
            public void run() {
                try {
                    for (int i = 0; i < n; i++) {
                        System.out.println("Mensagem " + i + " enviada!");
                        mensagens.put(i);
                        Thread.sleep(100); // Atrasa o envio de mensagens pausando a execução da thread para que as respostas cheguem em ordem (não é uma garantia)
                    }
                } catch (InterruptedException exc) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        class RespondeMsgAssinc extends Thread {
            @Override
            public void run() {
                try {
                    while (true) {
                        int valorMensagem = mensagens.take();
                        System.out.println("Mensagem " + valorMensagem + " recebida!");
                    }
                } catch (InterruptedException exc) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        EnviaMsgAssinc envio = new EnviaMsgAssinc();
        RespondeMsgAssinc resposta = new RespondeMsgAssinc();

        envio.start();
        resposta.start();
    }
}