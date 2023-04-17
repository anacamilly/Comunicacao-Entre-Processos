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
            saida.println(linha);
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
