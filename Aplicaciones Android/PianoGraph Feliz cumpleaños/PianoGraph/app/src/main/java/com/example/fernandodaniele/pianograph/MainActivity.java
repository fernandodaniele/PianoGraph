package com.example.fernandodaniele.pianograph;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {

    Button felizCumple;
    Button melodia2;
    TextView txtString;
    Handler bluetoothIn;
    SeekBar brightness;
    TextView lent;

    final int handlerState = 0;        				 //usado para identicar un handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();


    private ConnectedThread mConnectedThread;

    // SPP UUID service - funciona para la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String para la direccion MAC
    private static String address = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //enlaza los botones y textos a sus respectivas vistas
        felizCumple = (Button) findViewById(R.id.buttonOn);
        melodia2= (Button) findViewById(R.id.melodia2);
        brightness = (SeekBar)findViewById(R.id.seekBar);
        txtString = (TextView) findViewById(R.id.txtString);
        lent = (TextView) findViewById(R.id.lentitud);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {						//if message is what we want
                    String readMessage = (String) msg.obj;          // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      		//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        txtString.setText("Datos recibidos = " + dataInPrint);
                        //int dataLength = dataInPrint.length();							//get length of data received
                        //txtStringLength.setText("Tamaño del String = " + String.valueOf(dataLength));

                       /* if (recDataString.charAt(0) == '#')								//if it starts with # we know it is what we are looking for
                        {
                            String sensor0 = recDataString.charAt(1);             //get sensor value from string between indices 1-5
                            //String sensor1 = recDataString.substring(6, 10);            //same again...
                            //String sensor2 = recDataString.substring(11, 15);
                            //String sensor3 = recDataString.substring(16, 20);

                            if(sensor0.equals("1.00"))
                            sensorView0.setText("Encendido");	//update the textviews with sensor values
                            else
                                sensorView0.setText("Apagado");	//update the textviews with sensor values
                           // sensorView1.setText(sensor1);
                           // sensorView2.setText(sensor2);
                            //sensorView3.setText(sensor3);
                            //sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");
                        }
                        */
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                        // strIncom =" ";
                        //dataInPrint = " ";

                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       //obtiene el adaptador Bluetooth
        checkBTState();

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser==true)
                {
                    lent.setText("Lentitud = " + String.valueOf(progress));
                    mConnectedThread.writenote(0xFE);
                    mConnectedThread.writenote(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Configura los onClick listeners para los botones de las melodías
        felizCumple.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Handler handler = new Handler();

                //mConnectedThread.writenote(0xFF);
                notaOn(62);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       notaOff(62);
                    }
                }, 400); //retraso desde tocado el boton

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(62);
                    }
                }, 500);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(62);
                    }
                }, 900);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(64);
                    }
                }, 1000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(64);
                    }
                }, 1900);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(62);
                    }
                }, 2000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(62);
                    }
                }, 2700);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(67);
                    }
                }, 2800);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(67);
                    }
                }, 3700);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(66);
                    }
                }, 3800);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(66);
                    }
                }, 5700);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(62);
                    }
                }, 5800);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(62);
                    }
                }, 6200);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(62);
                    }
                }, 6300);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(62);
                    }
                }, 6700);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(64);
                    }
                }, 6800);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(64);
                    }
                }, 7700);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(62);
                    }
                }, 7800);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(62);
                    }
                }, 8700);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(69);
                    }
                }, 8800);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(69);
                    }
                }, 9700);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOn(67);
                    }
                }, 9800);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(67);
                    }
                }, 10700);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mConnectedThread.writenote(0xFF);
                    }
                }, 10901);
            }


            //Toast.makeText(getBaseContext(), "Encender el LED", Toast.LENGTH_SHORT).show();

        });
        melodia2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Handler handler = new Handler();

                //mConnectedThread.writenote(0xFF);
                notaOn(62);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notaOff(62);
                    }
                }, 3000); //retraso desde tocado el boton


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mConnectedThread.writenote(0xFF);
                    }
                }, 3200);
            }


            //Toast.makeText(getBaseContext(), "Encender el LED", Toast.LENGTH_SHORT).show();

        });



    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //crea una conexion segura con un dispositivo BT usando UUID
    }

    public void notaOn(int nota)
    {
        mConnectedThread.writenote(144);
        mConnectedThread.writenote(nota);
        mConnectedThread.writenote(127);
    }
    public void notaOff(int nota)
    {
        mConnectedThread.writenote(128);
        mConnectedThread.writenote(nota);
        mConnectedThread.writenote(0);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Obtiene la dirección MAC desde DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Error al crear Socket", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }


    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Error de conexión", Toast.LENGTH_LONG).show();
                finish();

            }
        }
        public void writenote(int input) {
            // byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(input);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Error de conexión", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}

