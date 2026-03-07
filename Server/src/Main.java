import controllers.ApiController;
import services.tcp.TcpServerService;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        ApiController controller = new ApiController();
        TcpServerService tcpServerService = new TcpServerService(1234, controller);

        try {
            tcpServerService.start();
        } catch (IOException e) {
            System.err.println("Failed to start TCP server: " + e.getMessage());
        }
    }
}
