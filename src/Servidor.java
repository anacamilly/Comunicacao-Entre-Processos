import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {

    private int porta;
    private List<ClienteHandler> clientes;

    public Servidor(int porta) {
        this.porta = porta;
        this.clientes = new ArrayList<>();
    }

    public void iniciar() throws IOException {
        ServerSocket serverSocket = new ServerSocket(this.porta);
        System.out.println("Servidor iniciado na porta " + this.porta);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Novo cliente conectado: " + socket.getInetAddress().getHostAddress());
            ClienteHandler clienteHandler = new ClienteHandler(socket, this);
            clientes.add(clienteHandler);
            clienteHandler.start();
        }
    }

    public void removerCliente(ClienteHandler clienteHandler) {
        this.clientes.remove(clienteHandler);
    }

    public void broadcast(String mensagem, ClienteHandler remetente) {
        for (ClienteHandler cliente : clientes) {
            if (cliente != remetente) {
                cliente.enviarMensagem(mensagem);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Informe o número da porta como argumento.");
            System.exit(1);
        }

        int porta = Integer.parseInt(args[0]);
        Servidor servidor = new Servidor(porta);
        servidor.iniciar();
    }
}

class ClienteHandler extends Thread {

    private Socket socket;
    private Servidor servidor;
    private BufferedReader entrada;
    private PrintWriter saida;
    private String nome;

    public ClienteHandler(Socket socket, Servidor servidor) throws IOException {
        this.socket = socket;
        this.servidor = servidor;
        this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.saida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    public void run() {
        try {
            this.saida.println("Digite o seu nome:");
            this.nome = entrada.readLine();
            String mensagem = this.nome + " acabou de entrar.";
            servidor.broadcast(mensagem, this);

            String linha;
            while ((linha = entrada.readLine()) != null) {
                if ("/exit".equals(linha)) {
                    break;
                }

                mensagem = this.nome + ": " + linha;
                servidor.broadcast(mensagem, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                servidor.removerCliente(this);
                String mensagem = this.nome + " acabou de sair.";
                servidor.broadcast(mensagem, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void enviarMensagem(String mensagem) {
        this.saida.println(mensagem);
    }

}
