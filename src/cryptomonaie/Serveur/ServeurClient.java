package cryptomonaie.serveur;

import cryptomonaie.Blockchaine;
import cryptomonaie.Jonction;
import cryptomonaie.TransactionRequest;
import cryptomonaie.TransactionResponse;
import cryptomonaie.Util;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 *
 * Serveur client: Wrapper de la connexion entre le serveur et le mineur. Le
 * mineur communique avec le mineur un utilisant cette classe.
 */
public class ServeurClient {

    private Socket socket;

    public ServeurClient(Socket socket) {
        this.socket = socket;
    }

    public void close() {

        try {
            socket.close();
        } catch (Exception ex) {
        }
    }

    TransactionRequest readTransactionRequest() throws SocketException, IOException, ClassNotFoundException {
        // timeout 1 seconde
        // si le mineur ne renvois rien dans une seconde ignore lui pour ne pas bloquer les auters requetes
        socket.setSoTimeout(1000);
        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
        return (TransactionRequest) is.readObject();

    }

    ObjectOutputStream os = null;

    boolean trySendResponse(TransactionResponse response) {

        boolean sent = false;
        try {
            if (os == null) {
                os = new ObjectOutputStream(socket.getOutputStream());
            }
            os.writeObject(response);
            os.flush();
        sent = true;
        } catch (SocketException ex) {
            Util.debug(this, ex, "Un mineur s'est déconnecté du canal multicast ");
        } catch (IOException ex) {
            Util.debug(this, ex);
        }
        return sent;
    }

    boolean trySendChaine(Blockchaine chaine) {
        boolean sent = false;
        try (ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream())) {
            os.writeObject(chaine);
            os.close();
            sent = true;
        } catch (IOException ex) {
            Util.debug(this, ex);
        } finally {
            this.close();
        }
        return sent;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    /**
     *
     * Try to send a message to the client
     *
     * @param message
     */
    private void trySend(String message) {

        try {
            OutputStreamWriter os = new OutputStreamWriter(socket.getOutputStream());
            os.write(message + "\n");
            os.flush();
            os.close();
        } catch (Exception ex) {

        }
    }

    void tryNotValid() {
        trySend("NOT_VALID");
        this.close();
    }

    void tryValid() {
        trySend("VALID");
        this.close();
    }

}
