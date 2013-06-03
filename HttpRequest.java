import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

public final class HttpRequest implements Runnable {

    private final static String CRLF = "\r\n";
    private final static String HTTP_VERSION = "HTTP/1.0 "; // versio HTTP
    private final static String WEB_SERVER = "Servidor D"; // nom del servidor
    private final static String ERR_404 = "404.html"; // pagina per defecte de error
    private final static String DEFAULT_INDEX = "index.html"; // pagina d'index
    private Socket socket;

    public HttpRequest(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
        }
    }

    private void processRequest() throws IOException, Exception {
        DataOutputStream os = new DataOutputStream(socket.getOutputStream()); // obtenim els fluxes de sortida
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); // obtenim els fluxes d'entrada

        System.out.println();

        String requestLine = br.readLine(); // l'arxiu que volem obtenir

        System.out.println(requestLine);

        String headerLine;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine); // altres capçaleres enviades pel navegador
        }


        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); // agafam el nom de l'arxiu
        String fileName = "." + tokens.nextToken();
        if (fileName.equals("./")) { // si no hi hem posat cap arxiu agafarem per defecte index.html (extra, comú a molts servidors)
            fileName = DEFAULT_INDEX; // default index
        }
        FileInputStream fis;
        File f = new File(fileName);
        boolean exists = f.exists(); // marcam la variable exists en funcio de l'existencia del fitxer
        try {
            fis = new FileInputStream(f); // si existeix el fitxer l'obrim
        } catch (IOException e) {
            fis = new FileInputStream(ERR_404); // si no existeix obrim el fitxer d'error 404
        }

        String statusLine = HTTP_VERSION; // marcam la versio http
        String contentTypeLine;
        if (exists) {
            statusLine += "200 OK" + CRLF; // marcam l'estat 200 si existeix, 404 si no existeix

        } else {
            statusLine += "404 Not Found" + CRLF;

        }
        statusLine += "Connection: close" + CRLF;//we can't handle persistent connections
        statusLine += "Server: " + WEB_SERVER + CRLF; //server name

        contentTypeLine = "Content-type: " + contentType(fileName); // tipus de contingut que ha demanat el client

        os.writeBytes(statusLine); // enviam l'estat

        os.writeBytes(contentTypeLine); // enviam el contingut MIME

        os.writeBytes(CRLF); // salt de linia

        sendBytes(fis, os); // enviam l'arxiu

        fis.close();
        os.close();
        br.close();
        socket.close();

    }

    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes;
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String fileName) {
        fileName = fileName.toLowerCase();
        String mime;
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) { // HTML
            mime = "text/html";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {  // jpeg
            mime = "image/jpeg";
        } else if (fileName.endsWith(".gif")) { // gif
            mime = "image/gif";
        }else if(fileName.endsWith(".png")){ // png
            mime = "image/png";
        } else {
            mime = "application/octet-stream"; // altres
        }
        return mime;
    }
}
