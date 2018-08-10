package cn.com.histar.filereceivetest;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FileReceiveService extends IntentService {

    private static final String TAG = "FileReceiveService";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RECEIVE = "cn.com.histar.filereceivetest.action.RECEIVE";
    private static final int PORT = 4563;


    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private OutputStream backMsg;
    private byte[] nameBuf;
    private byte[] pathBuf;
    private byte[] fileBuf;
    private int content;
    private String fileName;
    private String filePath;


    public FileReceiveService() {
        super("FileReceiveService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionReceive(Context context) {
        Log.e(TAG, "startActionReceive: ");
        Intent intent = new Intent(context, FileReceiveService.class);
        intent.setAction(ACTION_RECEIVE);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RECEIVE.equals(action)) {
                Log.e(TAG, "onHandleIntent: ");
                handleActionReceive();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionReceive() {

        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(PORT));
            socket = serverSocket.accept();
            Log.e(TAG, "handleActionReceive: " + "client ip = " + socket.getInetAddress().getHostAddress());
            inputStream = socket.getInputStream();
            nameBuf = new byte[1024];
            for (int i = 0; (content = inputStream.read()) != -1; i++) {
                if (content == '*') {
                    break;
                }
                nameBuf[i] = (byte) content;
            }
            fileName = new String(nameBuf, "utf-8").trim();
            Log.e(TAG, "handleActionReceive: " + "fileName = " + fileName);
            pathBuf = new byte[1024];
            for (int i = 0; (content = inputStream.read()) != -1; i++) {
                if (content == '*') {
                    break;
                }
                pathBuf[i] = (byte) content;
            }
            filePath = new String(pathBuf, "utf-8").trim();
            Log.e(TAG, "handleActionReceive: " + "filePath = " + filePath);

            File fileDir = new File(Environment.getExternalStorageDirectory().toString() + filePath);
            Log.e(TAG, "handleActionReceive: " + Environment.getExternalStorageDirectory().toString() + filePath);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            File file = new File(fileDir.getPath() + "/" + fileName);
            if (file.exists()) {
                file.delete();
            }
            outputStream = new FileOutputStream(file);
            fileBuf = new byte[1024];
            int len;
            while ((len = inputStream.read(fileBuf)) != -1) {
                outputStream.write(fileBuf, 0, len);
            }
            Log.e(TAG, "handleActionReceive: " + "receive file done!");
            backMsg = socket.getOutputStream();
            backMsg.write(("receive file " + fileName + "  succeed!").getBytes());

            clean();
        } catch (IOException e) {
            e.printStackTrace();
        }


        startActionReceive(this);
    }

    private void clean() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
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

        if (backMsg != null) {
            try {
                backMsg.flush();
                backMsg.close();
                backMsg = null;
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
