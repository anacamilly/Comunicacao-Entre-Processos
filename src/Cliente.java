import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente {

    private String host;
    private int porta;
    private String nome;
    private long totalBytesEnviados;
    private long tempoTotalEnvio;

    public Cliente(String host, int porta, String nome) {
        this.host = host;
        this.porta = porta;
        this.nome = nome;
    }

    public void iniciar() throws IOException {
        Socket socket = new Socket(host, porta);

        BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter saida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        Thread threadRecepcao = new Thread(new RecepcaoMensagens(entrada));
        threadRecepcao.start();

        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
        String linha;

        saida.println(nome);

        while ((linha = teclado.readLine()) != null && !"/exit".equals(linha)) {
            long inicioEnvio = System.currentTimeMillis();
            saida.println(linha);
            long fimEnvio = System.currentTimeMillis();

            // Calcula a taxa de transferência em bytes/segundo
            long tempoEnvio = fimEnvio - inicioEnvio;
            long bytesEnviados = linha.getBytes().length;
            totalBytesEnviados += bytesEnviados;
            tempoTotalEnvio += tempoEnvio;
            double taxaTransferencia = (double) totalBytesEnviados / tempoTotalEnvio * 1000;
            int tamanho = linha.getBytes().length;
            System.out.println("Tamanho da mensagem: " + tamanho + " bytes");


            System.out.println("Taxa de transferência: " + taxaTransferencia + " bytes/s");
        }

        socket.close();
        System.exit(0);
    }

    class RecepcaoMensagens implements Runnable {

        private BufferedReader entrada;

        public RecepcaoMensagens(BufferedReader entrada) {
            this.entrada = entrada;
        }

        public void run() {
            try {
                String linha;
                while ((linha = entrada.readLine()) != null) {
                    System.out.println(linha);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Informe o IP do servidor, a porta e o nome do usuário como argumentos.");
            System.exit(1);
        }

        String host = args[0];
        int porta = Integer.parseInt(args[1]);
        String nome = args[2];

        Cliente cliente = new Cliente(host, porta, nome);
        cliente.iniciar();
    }

}
