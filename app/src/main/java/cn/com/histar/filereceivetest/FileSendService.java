package cn.com.histar.filereceivetest;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FileSendService extends IntentService {

    private static final String TAG = "FileSendService";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SEND = "cn.com.histar.filereceivetest.action.SEND";

    // TODO: Rename parameters
    private static final String EXTRA_PORT = "cn.com.histar.filereceivetest.extra.PORT";
    private static final String EXTRA_IP = "cn.com.histar.filereceivetest.extra.IP";
    private static final String EXTRA_NAME = "cn.com.histar.filereceivetest.extra.NAME";
    private static final String EXTRA_SRC_PATH = "cn.com.histar.filereceivetest.extra.SRC.PATH";
    private static final String EXTRA_DST_PATH = "cn.com.histar.filereceivetest.extra.DST.PATH";


    private Socket socket;
    private OutputStream outputStream;
    private FileInputStream fileInputStream;
    private byte[] buf;
    private BufferedReader bufferedReader;


    public FileSendService() {
        super("FileSendService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSend(Context context, String port, String ip, String name, String srcPath, String dstPath) {
        Intent intent = new Intent(context, FileSendService.class);
        intent.setAction(ACTION_SEND);
        intent.putExtra(EXTRA_PORT, port);
        intent.putExtra(EXTRA_IP, ip);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_SRC_PATH, srcPath);
        intent.putExtra(EXTRA_DST_PATH, dstPath);

        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND.equals(action)) {
                final String port = intent.getStringExtra(EXTRA_PORT);
                final String ip = intent.getStringExtra(EXTRA_IP);
                final String name = intent.getStringExtra(EXTRA_NAME);
                final String srcPath = intent.getStringExtra(EXTRA_SRC_PATH);
                final String dstPath = intent.getStringExtra(EXTRA_DST_PATH);
                handleActionSend(port, ip, name, srcPath, dstPath);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSend(String port, String ip, String name, String srcPath, String dstPath) {
        int portInt = Integer.parseInt(port);
        try {
            socket = new Socket();
            socket.bind(null);
            socket.connect(new InetSocketAddress(ip, portInt), 20000);
            outputStream = socket.getOutputStream();
            outputStream.write((name + "*" + dstPath + "*").getBytes());
            fileInputStream = new FileInputStream(new File(srcPath));
            buf = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            socket.shutdownOutput();
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String serverBack = bufferedReader.readLine();

            Log.e(TAG, "handleActionSend: " + "backMsg = " + serverBack);
            clean();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clean() {

        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
                fileInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
                bufferedReader = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clean();
    }
}
