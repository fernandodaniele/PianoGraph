package com.poi.explorer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;

import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.UUID;

public class MainActivity extends Activity {

    Button felizCumple;
    Button melodia2;
    Button explorador;
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



    public static final int HEADER_SIZE = 14;
    public static final byte[] IDENTIFIER = {'M', 'T', 'h', 'd'};
    public static final byte[] IDENTIFIER2 = {'M', 'T', 'r', 'k'};
    public static final int NOTE_OFF = 0x8;
    public static final int NOTE_ON = 0x9;
    public static final int NOTE_AFTERTOUCH = 0xA;
    public static final int CONTROLLER = 0xB;
    public static final int PROGRAM_CHANGE = 0xC;
    public static final int CHANNEL_AFTERTOUCH = 0xD;
    public static final int PITCH_BEND = 0xE;
    private static final String HEX = "0123456789ABCDEF";
    public static final int SEQUENCE_NUMBER = 0;
    public static final int TEXT_EVENT = 1;
    public static final int COPYRIGHT_NOTICE = 2;
    public static final int TRACK_NAME = 3;
    public static final int INSTRUMENT_NAME = 4;
    public static final int LYRICS = 5;
    public static final int MARKER = 6;
    public static final int CUE_POINT = 7;
    public static final int MIDI_CHANNEL_PREFIX = 0x20;
    public static final int END_OF_TRACK = 0x2F;
    public static final int TEMPO = 0x51;
    public static final int SMPTE_OFFSET = 0x54;
    public static final int TIME_SIGNATURE = 0x58;
    public static final int KEY_SIGNATURE = 0x59;
    public static final int SEQUENCER_SPECIFIC = 0x7F;
    private int mType;
    //private ArrayList<MidiTrack> mTracks;

    private int mTrackCount;

    private int mResolution;
    public static final int DEFAULT_RESOLUTION = 480;
    protected PowerManager.WakeLock wakelock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        felizCumple = (Button) findViewById(R.id.buttonOn);
        melodia2= (Button) findViewById(R.id.melodia2);
        explorador= (Button) findViewById(R.id.explorador);
        brightness = (SeekBar)findViewById(R.id.seekBar);
        txtString = (TextView) findViewById(R.id.txtString);
        lent = (TextView) findViewById(R.id.lentitud);

        final PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        this.wakelock=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "etiqueta");
        wakelock.acquire();

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {						//if message is what we want
                    String readMessage = (String) msg.obj;          // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      		//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        txtString.setText("Datos recibidos = " + dataInPrint);
                        recDataString.delete(0, recDataString.length()); 					//clear all string data

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

        felizCumple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectedThread.writenote(0xFF);

            }
        });
        // Configura los onClick listeners para los botones de las melodías

        explorador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ListaFiles = 1;
                Intent i = new Intent(MainActivity.this,PoIExplorer.class);

                startActivityForResult(i,ListaFiles);
               // startActivity(i);
            }



        });



        melodia2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                MidiFile midi = null;
                File ruta_sd = getExternalFilesDir(null);
                File input = new File(ruta_sd.getAbsolutePath(), "ode.mid");

                try {
                    midi = new MidiFile(input);
                } catch (IOException e) {
                    e.printStackTrace();
                }

// Create a new MidiProcessor:
                MidiProcessor processor = new MidiProcessor(midi);

// Register for the events you're interested in:
                EventPrinter ep2 = new EventPrinter("Listener For All");

                processor.registerEventListener(ep2, NoteOn.class);

            }


            //Toast.makeText(getBaseContext(), "Encender el LED", Toast.LENGTH_SHORT).show();

        });
    }

    protected void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                // Do something with the contact here (bigger example below)
                String archivo= data.getStringExtra("midi");
                MidiFile midi = null;
                File input = new File(archivo);

                try {
                    midi = new MidiFile(input);
                } catch (IOException e) {
                    e.printStackTrace();
                }

// Create a new MidiProcessor:
                MidiProcessor processor = new MidiProcessor(midi);

// Register for the events you're interested in:
                EventPrinter ep2 = new EventPrinter("Listener For All");

                processor.registerEventListener(ep2, NoteOn.class);

                //processor.start();
            }
        }
    }

    private void initFromBuffer(byte[] buffer)

    {
        if (!bytesEqual(buffer, IDENTIFIER, 0, 4))

        {
            System.out.println("File identifier not MThd. Exiting");

            mType = 0;

            mTrackCount = 0;

            mResolution = DEFAULT_RESOLUTION;

            return;

        }


        mType = bytesToInt(buffer, 8, 2);

        mTrackCount = bytesToInt(buffer, 10, 2);

        mResolution = bytesToInt(buffer, 12, 2);

    }

    public static boolean bytesEqual(byte[] buf1, byte[] buf2, int off, int len) {
        for (int i = off; i < off + len; i++) {
            if (i >= buf1.length || i >= buf2.length) {
                return false;
            }
            if (buf1[i] != buf2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int bytesToInt(byte[] buff, int off, int len) {
        int num = 0;

        int shift = 0;
        for (int i = off + len - 1; i >= off; i--) {

            num += (buff[i] & 0xFF) << shift;
            shift += 8;
        }

        return num;
    }

    public class MidiTrack

    {

        private static final boolean VERBOSE = false;
        private int mSize;
        private boolean mSizeNeedsRecalculating;
        private boolean mClosed;
        private long mEndOfTrackDelta;
        private TreeSet<MidiEvent> mEvents;

      /*  public static MidiTrack createTempoTrack()
        {
            MidiTrack T = new MidiTrack();
            T.insertEvent(new TimeSignature());
            T.insertEvent(new Tempo());
            return T;
        }
        */

        public MidiTrack()

        {
            mEvents = new TreeSet<MidiEvent>();
            mSize = 0;
            mSizeNeedsRecalculating = false;
            mClosed = false;
            mEndOfTrackDelta = 0;
        }

        public MidiTrack(InputStream in) throws IOException {
            this();
            byte[] buffer = new byte[4];
            in.read(buffer);
            if (!bytesEqual(buffer, IDENTIFIER2, 0, 4)) {
                System.err.println("Track identifier did not match MTrk!");
                return;
            }

            in.read(buffer);
            mSize = bytesToInt(buffer, 0, 4);
            buffer = new byte[mSize];
            in.read(buffer);
            this.readTrackData(buffer);
        }

        private void readTrackData(byte[] data) throws IOException {
            InputStream in = new ByteArrayInputStream(data);
            long totalTicks = 0;
            while (in.available() > 0) {
                VariableLengthInt delta = new VariableLengthInt(in);
                totalTicks += delta.getValue();
                MidiEvent E = parseEvent(totalTicks, delta.getValue(), in);
                // Log.d("notaOn?", String.valueOf(E));
                if (E == null) {
                    System.out.println("Event skipped!");
                    continue;
                }
                if (VERBOSE) {
                    System.out.println(E);
                }
                // Not adding the EndOfTrack event here allows the track to be
                // edited
                // after being read in from file.
                if (E.getClass().equals(EndOfTrack.class)) {
                    mEndOfTrackDelta = E.getDelta();
                    break;
                }
                mEvents.add(E);
            }
        }

        public TreeSet<MidiEvent> getEvents() {

            return mEvents;
        }

        public int getEventCount() {
            return mEvents.size();
        }

        public int getSize() {
            if (mSizeNeedsRecalculating) {
                recalculateSize();
            }
            return mSize;
        }

        public long getLengthInTicks() {
            if (mEvents.size() == 0) {
                return 0;
            }
            MidiEvent E = mEvents.last();
            return E.getTick();
        }

        public long getEndOfTrackDelta() {
            return mEndOfTrackDelta;
        }

        public void setEndOfTrackDelta(long delta) {
            mEndOfTrackDelta = delta;
        }

        /*
        public void insertNote(int channel, int pitch, int velocity, long tick, long duration)
        {
            insertEvent(new NoteOn(tick, channel, pitch, velocity));
            insertEvent(new NoteOff(tick + duration, channel, pitch, 0));
        }
        */

        @SuppressWarnings({"rawtypes", "unchecked"})
        public void insertEvent(MidiEvent newEvent) {
            if (newEvent == null) {
                return;
            }
            if (mClosed) {
                System.err.println("Error: Cannot add an event to a closed track.");
                return;
            }
            MidiEvent prev = null, next = null;
            // floor() and ceiling() are not supported on Android before API Level 9
            // (Gingerbread)
            try {
                Class treeSet = Class.forName("java.util.TreeSet");
                Method floor = treeSet.getMethod("floor", Object.class);
                Method ceiling = treeSet.getMethod("ceiling", Object.class);
                prev = (MidiEvent) floor.invoke(mEvents, newEvent);
                next = (MidiEvent) ceiling.invoke(mEvents, newEvent);
            } catch (Exception e) {
                // methods are not supported - must perform linear search
                Iterator<MidiEvent> it = mEvents.iterator();
                while (it.hasNext()) {
                    next = it.next();
                    if (next.getTick() > newEvent.getTick()) {
                        break;
                    }
                    prev = next;
                    next = null;
                }
            }
            mEvents.add(newEvent);
            mSizeNeedsRecalculating = true;
            // Set its delta time based on the previous event (or itself if no
            // previous event exists)
            if (prev != null) {
                newEvent.setDelta(newEvent.getTick() - prev.getTick());
            } else {
                newEvent.setDelta(newEvent.getTick());
            }
            // Update the next event's delta time relative to the new event.
            if (next != null) {
                next.setDelta(next.getTick() - newEvent.getTick());
            }
            mSize += newEvent.getSize();
            if (newEvent.getClass().equals(EndOfTrack.class)) {
                if (next != null) {
                    throw new IllegalArgumentException("Attempting to insert EndOfTrack before an existing event. Use closeTrack() when finished with MidiTrack.");
                }
                mClosed = true;
            }
        }

        public boolean removeEvent(MidiEvent E) {
            Iterator<MidiEvent> it = mEvents.iterator();
            MidiEvent prev = null, curr = null, next = null;
            while (it.hasNext()) {
                next = it.next();
                if (E.equals(curr)) {
                    break;
                }
                prev = curr;
                curr = next;
                next = null;
            }

            if (next == null) {
                // Either the event was not found in the track,
                // or this is the last event in the track.
                // Either way, we won't need to update any delta times
                return mEvents.remove(curr);
            }

            if (!mEvents.remove(curr)) {
                return false;
            }

            if (prev != null) {
                next.setDelta(next.getTick() - prev.getTick());
            } else {
                next.setDelta(next.getTick());
            }

            return true;
        }

        public void closeTrack() {
            long lastTick = 0;
            if (mEvents.size() > 0) {
                MidiEvent last = mEvents.last();
                lastTick = last.getTick();
            }
            EndOfTrack eot = new EndOfTrack(lastTick + mEndOfTrackDelta, 0);
            insertEvent(eot);
        }

        public void dumpEvents() {
            Iterator<MidiEvent> it = mEvents.iterator();
            while (it.hasNext()) {
                System.out.println(it.next());
            }
        }

        private void recalculateSize() {
            mSize = 0;
            Iterator<MidiEvent> it = mEvents.iterator();
            MidiEvent last = null;
            while (it.hasNext()) {
                MidiEvent E = it.next();
                mSize += E.getSize();
                // If an event is of the same type as the previous event,
                // no status byte is written.
                if (last != null && !E.requiresStatusByte(last)) {
                    mSize--;
                }
                last = E;
            }
            mSizeNeedsRecalculating = false;
        }
    }

    public abstract class MidiEvent implements Comparable<MidiEvent> {
        protected long mTick;
        protected VariableLengthInt mDelta;

        public MidiEvent(long tick, long delta) {
            mTick = tick;
            mDelta = new VariableLengthInt((int) delta);
        }

        public long getTick() {
            return mTick;
        }

        public long getDelta() {
            return mDelta.getValue();
        }

        public void setDelta(long d) {
            mDelta.setValue((int) d);
        }

        protected abstract int getEventSize();

        public int getSize()

        {
            return getEventSize() + mDelta.getByteCount();
        }

        public boolean requiresStatusByte(MidiEvent prevEvent) {
            if (prevEvent == null) {
                return true;
            }
            if (this instanceof MetaEvent) {
                return true;
            }
            if (this.getClass().equals(prevEvent.getClass())) {
                return false;
            }
            return true;
        }

        public void writeToFile(OutputStream out, boolean writeType) throws IOException {
            out.write(mDelta.getBytes());
        }

       /* private static int sId = -1;
        private static int sType = -1;
        private static int sChannel = -1;*/


        @Override
        public String toString() {
            return "" + mTick + " (" + mDelta.getValue() + "): " + this.getClass().getSimpleName();
        }
    }

    private int sId = -1;
    private int sType = -1;
    private int sChannel = -1;

    //private static boolean verifyIdentifier(int id)
    private boolean verifyIdentifier(int id) {
        sId = id;
        int type = id >> 4;
        int channel = id & 0x0F;
        if (type >= 0x8 && type <= 0xE) {
            sId = id;
            sType = type;
            sChannel = channel;
        } else if (id == 0xFF) {
            sId = id;
            sType = -1;
            sChannel = -1;
        } else if (type == 0xF) {
            sId = id;
            sType = type;
            sChannel = -1;
        } else {
            return false;
        }
        return true;
    }

    //estas tres variables mas parseEvent estaban dentro de MidiEvent
    //public static final MidiEvent parseEvent(long tick, long delta, InputStream in) throws IOException
    public MidiEvent parseEvent(long tick, long delta, InputStream in) throws IOException {

        in.mark(1);
        boolean reset = false;
        int id = in.read();
        if (!verifyIdentifier(id)) {
            in.reset();
            reset = true;
        }

        if (sType >= 0x8 && sType <= 0xE) {
            return parseChannelEvent(tick, delta, sType, sChannel, in);
        } else if (sId == 0xFF) {
            return parseMetaEvent(tick, delta, in);
        } else if (sId == 0xF0 || sId == 0xF7) {
            VariableLengthInt size = new VariableLengthInt(in);
            byte[] data = new byte[size.getValue()];
            in.read(data);
            return new SystemExclusiveEvent(sId, tick, delta, data);
        } else {
            System.out.println("Unable to handle status byte, skipping: " + sId);
            if (reset) {
                in.read();
            }
        }
        return null;
    }

    public class VariableLengthInt {
        private int mValue;
        private byte[] mBytes;
        private int mSizeInBytes;

        public VariableLengthInt(int value) {
            setValue(value);
        }

        public VariableLengthInt(InputStream in) throws IOException {
            parseBytes(in);
        }

        public void setValue(int value) {
            mValue = value;
            buildBytes();
        }

        public int getValue() {
            return mValue;
        }

        public int getByteCount() {
            return mSizeInBytes;
        }

        public byte[] getBytes() {
            return mBytes;
        }

        private void parseBytes(InputStream in) throws IOException {
            int[] ints = new int[4];

            mSizeInBytes = 0;
            mValue = 0;
            int shift = 0;

            int b = in.read();
            while (mSizeInBytes < 4) {
                mSizeInBytes++;

                boolean variable = (b & 0x80) > 0;
                if (!variable) {
                    ints[mSizeInBytes - 1] = (b & 0x7F);
                    break;
                }
                ints[mSizeInBytes - 1] = (b & 0x7F);

                b = in.read();
            }

            for (int i = 1; i < mSizeInBytes; i++) {
                shift += 7;
            }

            mBytes = new byte[mSizeInBytes];
            for (int i = 0; i < mSizeInBytes; i++) {
                mBytes[i] = (byte) ints[i];

                mValue += ints[i] << shift;
                shift -= 7;
            }
        }

        private void buildBytes() {
            if (mValue == 0) {
                mBytes = new byte[1];
                mBytes[0] = 0x00;
                mSizeInBytes = 1;
                return;
            }

            mSizeInBytes = 0;
            int[] vals = new int[4];
            int tmpVal = mValue;

            while (mSizeInBytes < 4 && tmpVal > 0) {
                vals[mSizeInBytes] = tmpVal & 0x7F;

                mSizeInBytes++;
                tmpVal = tmpVal >> 7;
            }

            for (int i = 1; i < mSizeInBytes; i++) {
                vals[i] |= 0x80;
            }

            mBytes = new byte[mSizeInBytes];
            for (int i = 0; i < mSizeInBytes; i++) {
                mBytes[i] = (byte) vals[mSizeInBytes - i - 1];
            }
        }

        @Override
        public String toString() {
            return bytesToHex(mBytes) + " (" + mValue + ")";
        }
    }

    public class EndOfTrack extends MetaEvent {
        public EndOfTrack(long tick, long delta) {
            super(tick, delta, END_OF_TRACK, new VariableLengthInt(0));
        }

        @Override
        protected int getEventSize() {
            return 3;
        }

        @Override
        public void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out);

            out.write(0);
        }

        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.getDelta()) {
                return mDelta.getValue() < other.getDelta() ? 1 : -1;
            }

            if (!(other instanceof EndOfTrack)) {
                return 1;
            }
            return 0;
        }
    }

    public abstract class MetaEvent extends MidiEvent {
        protected int mType;
        protected VariableLengthInt mLength;

        protected MetaEvent(long tick, long delta, int type, VariableLengthInt length) {
            super(tick, delta);

            mType = type & 0xFF;
            mLength = length;
        }

        protected abstract int getEventSize();

        @Override
        public void writeToFile(OutputStream out, boolean writeType) throws IOException {
            writeToFile(out);
        }

        protected void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out, true);
            out.write(0xFF);
            out.write(mType);
        }


    }


    //las 2 siguiente estaba dentro de metaEvent
    //protected static class MetaEventData
    protected class MetaEventData {
        public final int type;
        public final VariableLengthInt length;
        public final byte[] data;

        public MetaEventData(InputStream in) throws IOException {
            type = in.read();
            length = new VariableLengthInt(in);
            data = new byte[length.getValue()];
            if (length.getValue() > 0) {
                in.read(data);
            }
        }
    }

    //public static MetaEvent parseMetaEvent(long tick, long delta, InputStream in) throws IOException
    public MetaEvent parseMetaEvent(long tick, long delta, InputStream in) throws IOException {
        MetaEventData eventData = new MetaEventData(in);

        boolean isText = false;
        switch (eventData.type) {
            case SEQUENCE_NUMBER:
            case MIDI_CHANNEL_PREFIX:
            case END_OF_TRACK:
            case TEMPO:
            case SMPTE_OFFSET:
            case TIME_SIGNATURE:
            case KEY_SIGNATURE:
                break;
            case TEXT_EVENT:
            case COPYRIGHT_NOTICE:
            case TRACK_NAME:
            case INSTRUMENT_NAME:
            case LYRICS:
            case MARKER:
            case CUE_POINT:
            case SEQUENCER_SPECIFIC: // Not technically text, but follows same
                // structure
            default: // Also not technically text, but it should follow
                isText = true;
                break;
        }

        if (isText) {
            String text = new String(eventData.data);

            switch (eventData.type) {
                case TEXT_EVENT:
                    return new Text(tick, delta, text);
                case COPYRIGHT_NOTICE:
                    return new CopyrightNotice(tick, delta, text);
                case TRACK_NAME:
                    return new TrackName(tick, delta, text);
                case INSTRUMENT_NAME:
                    return new InstrumentName(tick, delta, text);
                case LYRICS:
                    return new Lyrics(tick, delta, text);
                case MARKER:
                    return new Marker(tick, delta, text);
                case CUE_POINT:
                    return new CuePoint(tick, delta, text);
                case SEQUENCER_SPECIFIC:
                    return new SequencerSpecificEvent(tick, delta, eventData.data);
                default:
                    return new GenericMetaEvent(tick, delta, eventData);
            }
        }

        switch (eventData.type) {
            case SEQUENCE_NUMBER:
                return parseSequenceNumber(tick, delta, eventData);
            case MIDI_CHANNEL_PREFIX:
                return parseMidiChannelPrefix(tick, delta, eventData);
            case END_OF_TRACK:
                return new EndOfTrack(tick, delta);
            case TEMPO:
                return parseTempo(tick, delta, eventData);
            case SMPTE_OFFSET:
                return parseSmpteOffset(tick, delta, eventData);
            case TIME_SIGNATURE:
                return parseTimeSignature(tick, delta, eventData);
            case KEY_SIGNATURE:
                return parseKeySignature(tick, delta, eventData);
        }
        System.out.println("Completely broken in MetaEvent.parseMetaEvent()");
        return null;
    }

    public static String byteToHex(byte b) {
        int high = (b & 0xF0) >> 4;
        int low = (b & 0x0F);

        return "" + HEX.charAt(high) + HEX.charAt(low);
    }

    public static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            sb.append(byteToHex(b[i])).append(" ");
        }
        return sb.toString();
    }

    public class ChannelEvent extends MidiEvent {
        protected int mType;
        protected int mChannel;
        public int mValue1;
        protected int mValue2;


        //private static HashMap<Integer, Integer> mOrderMap;
        private HashMap<Integer, Integer> mOrderMap;

        protected ChannelEvent(long tick, int type, int channel, int param1, int param2) {
            this(tick, 0, type, channel, param1, param2);
        }

        protected ChannelEvent(long tick, long delta, int type, int channel, int param1, int param2) {
            super(tick, delta);

            mType = type & 0x0F;
            mChannel = channel & 0x0F;
            mValue1 = param1 & 0xFF;
            mValue2 = param2 & 0xFF;
        }

        public int getType() {
            return mType;
        }

        public void setChannel(int c) {
            if (c < 0) {
                c = 0;
            } else if (c > 15) {
                c = 15;
            }
            mChannel = c;
        }

        public int getChannel() {
            return mChannel;
        }

        protected int getEventSize() {
            switch (mType) {
                case PROGRAM_CHANGE:
                case CHANNEL_AFTERTOUCH:
                    return 2;
                default:
                    return 3;
            }
        }

        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.mDelta.getValue()) {
                return mDelta.getValue() < other.mDelta.getValue() ? 1 : -1;
            }

            if (!(other instanceof ChannelEvent)) {
                return 1;
            }

            ChannelEvent o = (ChannelEvent) other;
            if (mType != o.getType()) {
                if (mOrderMap == null) {
                    buildOrderMap();
                }

                int order1 = mOrderMap.get(mType);
                int order2 = mOrderMap.get(o.getType());

                return order1 < order2 ? -1 : 1;
            }
            if (mValue1 != o.mValue1) {
                return mValue1 < o.mValue1 ? -1 : 1;
            }
            if (mValue2 != o.mValue2) {
                return mValue2 < o.mValue2 ? -1 : 1;
            }
            if (mChannel != o.getChannel()) {
                return mChannel < o.getChannel() ? -1 : 1;
            }
            return 0;
        }

        @Override
        public boolean requiresStatusByte(MidiEvent prevEvent) {
            if (prevEvent == null) {
                return true;
            }
            if (!(prevEvent instanceof ChannelEvent)) {
                return true;
            }

            ChannelEvent ce = (ChannelEvent) prevEvent;
            return !(mType == ce.getType() && mChannel == ce.getChannel());
        }

        @Override
        public void writeToFile(OutputStream out, boolean writeType) throws IOException {
            super.writeToFile(out, writeType);

            if (writeType) {
                int typeChannel = (mType << 4) + mChannel;
                out.write(typeChannel);
            }

            out.write(mValue1);
            if (mType != PROGRAM_CHANGE && mType != CHANNEL_AFTERTOUCH) {
                out.write(mValue2);
            }
        }


        //private static void buildOrderMap()
        private void buildOrderMap() {
            mOrderMap = new HashMap<Integer, Integer>();
            mOrderMap.put(PROGRAM_CHANGE, 0);
            mOrderMap.put(CONTROLLER, 1);
            mOrderMap.put(NOTE_ON, 2);
            mOrderMap.put(NOTE_OFF, 3);
            mOrderMap.put(NOTE_AFTERTOUCH, 4);
            mOrderMap.put(CHANNEL_AFTERTOUCH, 5);
            mOrderMap.put(PITCH_BEND, 6);
        }


    }

    //LA siguiente estaba dentro de chanelEvent
    public ChannelEvent parseChannelEvent(long tick, long delta, int type, int channel, InputStream in) throws IOException {
        final Handler handler = new Handler();
        final int tipo = type;
        int val1 = in.read();
        final int valor1 = val1;
        int val2 = 0;
        if (type != PROGRAM_CHANGE && type != CHANNEL_AFTERTOUCH) {

            val2 = in.read();

        }
        final int valor2 = val2;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                System.out.println(tipo);
                System.out.println("Nota?= " + valor1);
                System.out.println("Velocidad?= " + valor2);
                if(tipo==NOTE_ON) {
                    mConnectedThread.writenote(144);
                    mConnectedThread.writenote(valor1);
                    mConnectedThread.writenote(valor2);
                }
                else if(tipo==NOTE_OFF) {
                    mConnectedThread.writenote(128);
                    mConnectedThread.writenote(valor1);
                    mConnectedThread.writenote(valor2);
                }
            }
        }, tick);

        switch (type) {
            case NOTE_OFF:
                return new NoteOff(tick, delta, channel, val1, val2);
            case NOTE_ON:
                return new NoteOn(tick, delta, channel, val1, val2);
            case NOTE_AFTERTOUCH:
                return new NoteAftertouch(tick, delta, channel, val1, val2);
            case CONTROLLER:
                return new Controller(tick, delta, channel, val1, val2);
            case PROGRAM_CHANGE:
                return new ProgramChange(tick, delta, channel, val1);
            case CHANNEL_AFTERTOUCH:
                return new ChannelAftertouch(tick, delta, channel, val1);
            case PITCH_BEND:
                return new PitchBend(tick, delta, channel, val1, val2);
            default:
                return new ChannelEvent(tick, delta, type, channel, val1, val2);
        }
    }


    public class SystemExclusiveEvent extends MidiEvent {
        private int mType;
        private VariableLengthInt mLength;
        private byte[] mData;

        public SystemExclusiveEvent(int type, long tick, byte[] data) {
            this(type, tick, 0, data);
        }

        public SystemExclusiveEvent(int type, long tick, long delta, byte[] data) {
            super(tick, delta);
            mType = type & 0xFF;
            if (mType != 0xF0 && mType != 0xF7) {
                mType = 0xF0;
            }


            mLength = new VariableLengthInt(data.length);

            mData = data;

        }


        public byte[] getData()

        {

            return mData;

        }


        public void setData(byte[] data)

        {

            mLength.setValue(data.length);

            mData = data;

        }


        @Override

        public boolean requiresStatusByte(MidiEvent prevEvent)

        {

            return true;

        }


        @Override

        public void writeToFile(OutputStream out, boolean writeType) throws IOException

        {

            super.writeToFile(out, writeType);


            out.write(mType);

            out.write(mLength.getBytes());

            out.write(mData);

        }


        @Override

        public int compareTo(MidiEvent other)

        {

            if (this.mTick < other.mTick)

            {

                return -1;

            }

            if (this.mTick > other.mTick)

            {

                return 1;

            }


            if (this.mDelta.getValue() > other.mDelta.getValue())

            {

                return -1;

            }

            if (this.mDelta.getValue() < other.mDelta.getValue())

            {

                return 1;

            }


            if (other instanceof SystemExclusiveEvent)

            {

                String curr = new String(mData);

                String comp = new String(((SystemExclusiveEvent) other).mData);

                return curr.compareTo(comp);

            }


            return 1;

        }


        @Override

        protected int getEventSize()

        {

            return 1 + mLength.getByteCount() + mData.length;

        }


    }

    public class Text extends TextualMetaEvent {
        public Text(long tick, long delta, String text) {
            super(tick, delta, TEXT_EVENT, text);
        }

        public void setText(String t) {
            super.setText(t);
        }

        public String getText() {
            return super.getText();
        }
    }

    public class CopyrightNotice extends TextualMetaEvent {
        public CopyrightNotice(long tick, long delta, String text) {
            super(tick, delta, COPYRIGHT_NOTICE, text);
        }

        public void setNotice(String t) {
            setText(t);
        }

        public String getNotice() {
            return getText();
        }
    }


    public class TrackName extends TextualMetaEvent {
        public TrackName(long tick, long delta, String name) {
            super(tick, delta, TRACK_NAME, name);
        }

        public void setName(String name) {
            setText(name);
        }

        public String getTrackName() {
            return getText();
        }
    }

    public class InstrumentName extends TextualMetaEvent {
        public InstrumentName(long tick, long delta, String name) {
            super(tick, delta, INSTRUMENT_NAME, name);
        }

        public void setName(String name) {
            setText(name);
        }

        public String getName() {
            return getText();
        }
    }

    public class Lyrics extends TextualMetaEvent {
        public Lyrics(long tick, long delta, String lyric) {
            super(tick, delta, LYRICS, lyric);
        }

        public void setLyric(String t) {
            setText(t);
        }

        public String getLyric() {
            return getText();
        }
    }


    public class Marker extends TextualMetaEvent {
        public Marker(long tick, long delta, String marker) {
            super(tick, delta, MARKER, marker);
        }

        public void setMarkerName(String name) {
            setText(name);
        }

        public String getMarkerName() {
            return getText();
        }
    }

    public class CuePoint extends TextualMetaEvent {
        public CuePoint(long tick, long delta, String marker) {
            super(tick, delta, CUE_POINT, marker);
        }

        public void setCue(String name) {
            setText(name);
        }

        public String getCue() {
            return getText();
        }
    }

    public class SequencerSpecificEvent extends MetaEvent {
        private byte[] mData;

        public SequencerSpecificEvent(long tick, long delta, byte[] data) {
            super(tick, delta, SEQUENCER_SPECIFIC, new VariableLengthInt(data.length));

            mData = data;
        }

        public void setData(byte[] data) {
            mData = data;
            mLength.setValue(mData.length);
        }

        public byte[] getData() {
            return mData;
        }

        protected int getEventSize() {
            return 1 + 1 + mLength.getByteCount() + mData.length;
        }

        @Override
        public void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out);

            out.write(mLength.getBytes());
            out.write(mData);
        }

        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.getDelta()) {
                return mDelta.getValue() < other.getDelta() ? 1 : -1;
            }

            if (!(other instanceof SequencerSpecificEvent)) {
                return 1;
            }

            SequencerSpecificEvent o = (SequencerSpecificEvent) other;

            if (bytesEqual(mData, o.mData, 0, mData.length)) {
                return 0;
            }
            return 1;
        }
    }

    public class GenericMetaEvent extends MetaEvent {
        private byte[] mData;

        protected GenericMetaEvent(long tick, long delta, MetaEventData info) {
            super(tick, delta, info.type, info.length);

            mData = info.data;

            System.out.println("Warning: GenericMetaEvent used because type (" + info.type + ") wasn't recognized or unexpected data length (" + info.length.getValue() + ") for type.");
        }

        @Override
        protected int getEventSize() {
            return 1 + 1 + mLength.getByteCount() + mLength.getValue();
        }

        @Override
        protected void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out);
            out.write(mLength.getBytes());
            out.write(mData);
        }

        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.getDelta()) {
                return mDelta.getValue() < other.getDelta() ? 1 : -1;
            }

            return 1;
        }

    }

    public class SequenceNumber extends MetaEvent {
        private int mNumber;

        public SequenceNumber(long tick, long delta, int number) {
            super(tick, delta, SEQUENCE_NUMBER, new VariableLengthInt(2));

            mNumber = number;
        }

        public int getMostSignificantBits() {
            return mNumber >> 8;
        }

        public int getLeastSignificantBits() {
            return mNumber & 0xFF;
        }

        public int getSequenceNumber() {
            return mNumber;
        }

        @Override
        public void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out);

            out.write(2);
            out.write(getMostSignificantBits());
            out.write(getLeastSignificantBits());
        }


        @Override
        protected int getEventSize() {
            return 5;
        }

        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.getDelta()) {
                return mDelta.getValue() < other.getDelta() ? 1 : -1;
            }

            if (!(other instanceof SequenceNumber)) {
                return 1;
            }

            SequenceNumber o = (SequenceNumber) other;

            if (mNumber != o.mNumber) {
                return mNumber < o.mNumber ? -1 : 1;
            }
            return 0;
        }
    }

    //estaba dentro de sequenceNumber
    //public static MetaEvent parseSequenceNumber(long tick, long delta, MetaEventData info)
    public MetaEvent parseSequenceNumber(long tick, long delta, MetaEventData info) {
        if (info.length.getValue() != 2) {
            return new GenericMetaEvent(tick, delta, info);
        }

        int msb = info.data[0];
        int lsb = info.data[1];
        int number = (msb << 8) + lsb;

        return new SequenceNumber(tick, delta, number);
    }

    public class MidiChannelPrefix extends MetaEvent {
        private int mChannel;

        public MidiChannelPrefix(long tick, long delta, int channel) {
            super(tick, delta, MIDI_CHANNEL_PREFIX, new VariableLengthInt(4));

            mChannel = channel;
        }

        public void setChannel(int c) {
            mChannel = c;
        }

        public int getChannel() {
            return mChannel;
        }

        @Override
        protected int getEventSize() {
            return 4;
        }

        @Override
        public void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out);

            out.write(1);
            out.write(mChannel);
        }


        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.getDelta()) {
                return mDelta.getValue() < other.getDelta() ? 1 : -1;
            }

            if (!(other instanceof MidiChannelPrefix)) {
                return 1;
            }

            MidiChannelPrefix o = (MidiChannelPrefix) other;

            if (mChannel != o.mChannel) {
                return mChannel < o.mChannel ? -1 : 1;
            }
            return 0;
        }
    }

    //la siguiente estaba dentro de MidiChannelPrefix
    //public static MetaEvent parseMidiChannelPrefix(long tick, long delta, MetaEventData info)
    public MetaEvent parseMidiChannelPrefix(long tick, long delta, MetaEventData info) {
        if (info.length.getValue() != 1) {
            return new GenericMetaEvent(tick, delta, info);
        }

        int channel = info.data[0];

        return new MidiChannelPrefix(tick, delta, channel);
    }

    public class Tempo extends MetaEvent {
        public static final float DEFAULT_BPM = 120.0f;
        public static final int DEFAULT_MPQN = (int) (60000000 / DEFAULT_BPM);

        private int mMPQN;
        private float mBPM;

        public Tempo() {
            this(0, 0, DEFAULT_MPQN);
        }

        public Tempo(long tick, long delta, int mpqn) {
            super(tick, delta, TEMPO, new VariableLengthInt(3));

            setMpqn(mpqn);
        }

        public int getMpqn() {
            return mMPQN;
        }

        public float getBpm() {
            return mBPM;
        }

        public void setMpqn(int m) {
            mMPQN = m;
            mBPM = 60000000.0f / mMPQN;
        }

        public void setBpm(float b) {
            mBPM = b;
            mMPQN = (int) (60000000 / mBPM);
        }

        @Override
        protected int getEventSize() {
            return 6;
        }

        @Override
        public void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out);

            out.write(3);
            out.write(intToBytes(mMPQN, 3));
        }


        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.getDelta()) {
                return mDelta.getValue() < other.getDelta() ? 1 : -1;
            }

            if (!(other instanceof Tempo)) {
                return 1;
            }

            Tempo o = (Tempo) other;

            if (mMPQN != o.mMPQN) {
                return mMPQN < o.mMPQN ? -1 : 1;
            }
            return 0;
        }
    }

    //la siguientes estaba dentro de tempo
    //public static MetaEvent parseTempo(long tick, long delta, MetaEventData info)
    public MetaEvent parseTempo(long tick, long delta, MetaEventData info) {
        if (info.length.getValue() != 3) {
            return new GenericMetaEvent(tick, delta, info);
        }

        int mpqn = bytesToInt(info.data, 0, 3);

        return new Tempo(tick, delta, mpqn);
    }

    public class SmpteOffset extends MetaEvent {
        public static final int FRAME_RATE_24 = 0;
        public static final int FRAME_RATE_25 = 1;
        public static final int FRAME_RATE_30_DROP = 2;
        public static final int FRAME_RATE_30 = 3;

        private FrameRate mFrameRate;
        private int mHours;
        private int mMinutes;
        private int mSeconds;
        private int mFrames;
        private int mSubFrames;

        public SmpteOffset(long tick, long delta, FrameRate fps, int hour, int min, int sec, int fr, int subfr) {
            super(tick, delta, SMPTE_OFFSET, new VariableLengthInt(5));

            mFrameRate = fps;
            mHours = hour;
            mMinutes = min;
            mSeconds = sec;
            mFrames = fr;
            mSubFrames = subfr;
        }

        public void setFrameRate(FrameRate fps) {
            mFrameRate = fps;
        }

        public FrameRate getFrameRate() {
            return mFrameRate;
        }

        public void setHours(int h) {
            mHours = h;
        }

        public int getHours() {
            return mHours;
        }

        public void setMinutes(int m) {
            mMinutes = m;
        }

        public int getMinutes() {
            return mMinutes;
        }

        public void setSeconds(int s) {
            mSeconds = s;
        }

        public int getSeconds() {
            return mSeconds;
        }

        public void setFrames(int f) {
            mFrames = f;
        }

        public int getFrames() {
            return mFrames;
        }

        public void setSubFrames(int s) {
            mSubFrames = s;
        }

        public int getSubFrames() {
            return mSubFrames;
        }

        @Override
        protected int getEventSize() {
            return 8;
        }

        @Override
        public void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out);

            out.write(5);
            out.write(mHours);
            out.write(mMinutes);
            out.write(mSeconds);
            out.write(mFrames);
            out.write(mSubFrames);
        }


        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.getDelta()) {
                return mDelta.getValue() < other.getDelta() ? 1 : -1;
            }

            if (!(other instanceof SmpteOffset)) {
                return 1;
            }

            return 0;
        }
    }

    //estaba dentro de SmpteOffset
    public enum FrameRate {
        FRAME_RATE_24(0x00), FRAME_RATE_25(0x01), FRAME_RATE_30_DROP(0x02), FRAME_RATE_30(0x03);

        public final int value;

        private FrameRate(int v) {
            value = v;
        }

        public static FrameRate fromInt(int val) {
            switch (val) {
                case 0:
                    return FRAME_RATE_24;
                case 1:
                    return FRAME_RATE_25;
                case 2:
                    return FRAME_RATE_30_DROP;
                case 3:
                    return FRAME_RATE_30;
            }
            return null;
        }
    }

    //        public static MetaEvent parseSmpteOffset(long tick, long delta, MetaEventData info)
    public MetaEvent parseSmpteOffset(long tick, long delta, MetaEventData info) {
        if (info.length.getValue() != 5) {
            return new GenericMetaEvent(tick, delta, info);
        }

        int rrHours = info.data[0];

        int rr = rrHours >> 5;
        FrameRate fps = FrameRate.fromInt(rr);
        int hour = rrHours & 0x1F;

        int min = info.data[1];
        int sec = info.data[2];
        int frm = info.data[3];
        int sub = info.data[4];

        return new SmpteOffset(tick, delta, fps, hour, min, sec, frm, sub);
    }

    public class TimeSignature extends MetaEvent {
        public static final int METER_EIGHTH = 12;
        public static final int METER_QUARTER = 24;
        public static final int METER_HALF = 48;
        public static final int METER_WHOLE = 96;

        public static final int DEFAULT_METER = METER_QUARTER;
        public static final int DEFAULT_DIVISION = 8;

        private int mNumerator;
        private int mDenominator;
        private int mMeter;
        private int mDivision;

        public TimeSignature() {
            this(0, 0, 4, 4, DEFAULT_METER, DEFAULT_DIVISION);
        }

        public TimeSignature(long tick, long delta, int num, int den, int meter, int div) {
            super(tick, delta, TIME_SIGNATURE, new VariableLengthInt(4));

            setTimeSignature(num, den, meter, div);
        }

        public void setTimeSignature(int num, int den, int meter, int div) {
            mNumerator = num;
            mDenominator = log2(den);
            mMeter = meter;
            mDivision = div;
        }

        public int getNumerator() {
            return mNumerator;
        }

        public int getDenominatorValue() {
            return mDenominator;
        }

        public int getRealDenominator() {
            return (int) Math.pow(2, mDenominator);
        }

        public int getMeter() {
            return mMeter;
        }

        public int getDivision() {
            return mDivision;
        }

        @Override
        protected int getEventSize() {
            return 7;
        }

        @Override
        public void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out);

            out.write(4);
            out.write(mNumerator);
            out.write(mDenominator);
            out.write(mMeter);
            out.write(mDivision);
        }

        private int log2(int den) {
            switch (den) {
                case 2:
                    return 1;
                case 4:
                    return 2;
                case 8:
                    return 3;
                case 16:
                    return 4;
                case 32:
                    return 5;
            }
            return 0;
        }

        @Override
        public String toString() {
            return super.toString() + " " + mNumerator + "/" + getRealDenominator();
        }

        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.getDelta()) {
                return mDelta.getValue() < other.getDelta() ? 1 : -1;
            }

            if (!(other instanceof TimeSignature)) {
                return 1;
            }

            TimeSignature o = (TimeSignature) other;

            if (mNumerator != o.mNumerator) {
                return mNumerator < o.mNumerator ? -1 : 1;
            }
            if (mDenominator != o.mDenominator) {
                return mDenominator < o.mDenominator ? -1 : 1;
            }
            return 0;
        }
    }

    //estaba dentro de TimeSignature
    //public static MetaEvent parseTimeSignature(long tick, long delta, MetaEventData info)
    public MetaEvent parseTimeSignature(long tick, long delta, MetaEventData info) {
        if (info.length.getValue() != 4) {
            return new GenericMetaEvent(tick, delta, info);
        }

        int num = info.data[0];
        int den = info.data[1];
        int met = info.data[2];
        int fps = info.data[3];

        den = (int) Math.pow(2, den);

        return new TimeSignature(tick, delta, num, den, met, fps);
    }

    public class KeySignature extends MetaEvent {
        public static final int SCALE_MAJOR = 0;
        public static final int SCALE_MINOR = 1;

        private int mKey;
        private int mScale;

        public KeySignature(long tick, long delta, int key, int scale) {
            super(tick, delta, KEY_SIGNATURE, new VariableLengthInt(2));

            this.setKey(key);
            mScale = scale;
        }

        public void setKey(int key) {
            mKey = (byte) key;

            if (mKey < -7)
                mKey = -7;
            else if (mKey > 7)
                mKey = 7;
        }

        public int getKey() {
            return mKey;
        }

        public void setScale(int scale) {
            mScale = scale;
        }

        public int getScale() {
            return mScale;
        }

        @Override
        protected int getEventSize() {
            return 5;
        }

        @Override
        public void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out);

            out.write(2);
            out.write(mKey);
            out.write(mScale);
        }


        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.getDelta()) {
                return mDelta.getValue() < other.getDelta() ? 1 : -1;
            }

            if (!(other instanceof KeySignature)) {
                return 1;
            }

            KeySignature o = (KeySignature) other;
            if (mKey != o.mKey) {
                return mKey < o.mKey ? -1 : 1;
            }

            if (mScale != o.mScale) {
                return mKey < o.mScale ? -1 : 1;
            }

            return 0;
        }
    }

    //estaba dentro de KeySignature
    //public static MetaEvent parseKeySignature(long tick, long delta, MetaEventData info)
    public MetaEvent parseKeySignature(long tick, long delta, MetaEventData info) {
        if (info.length.getValue() != 2) {
            return new GenericMetaEvent(tick, delta, info);
        }

        int key = info.data[0];
        int scale = info.data[1];

        return new KeySignature(tick, delta, key, scale);
    }

    public abstract class TextualMetaEvent extends MetaEvent {
        protected String mText;

        protected TextualMetaEvent(long tick, long delta, int type, String text) {
            super(tick, delta, type, new VariableLengthInt(text.length()));

            setText(text);
        }

        protected void setText(String t) {
            mText = t;
            mLength.setValue(t.getBytes().length);
        }

        protected String getText() {
            return mText;
        }

        @Override
        protected int getEventSize() {
            return 1 + 1 + mLength.getByteCount() + mLength.getValue();
        }

        @Override
        public void writeToFile(OutputStream out) throws IOException {
            super.writeToFile(out);

            out.write(mLength.getBytes());
            out.write(mText.getBytes());
        }

        @Override
        public int compareTo(MidiEvent other) {
            if (mTick != other.getTick()) {
                return mTick < other.getTick() ? -1 : 1;
            }
            if (mDelta.getValue() != other.getDelta()) {
                return mDelta.getValue() < other.getDelta() ? 1 : -1;
            }

            if (!(other instanceof TextualMetaEvent)) {
                return 1;
            }

            TextualMetaEvent o = (TextualMetaEvent) other;

            return mText.compareTo(o.mText);
        }

        @Override
        public String toString() {
            return super.toString() + ": " + mText;
        }
    }

    public static byte[] intToBytes(int val, int byteCount) {
        byte[] buffer = new byte[byteCount];

        int[] ints = new int[byteCount];

        for (int i = 0; i < byteCount; i++) {
            ints[i] = val & 0xFF;
            buffer[byteCount - i - 1] = (byte) ints[i];

            val = val >> 8;

            if (val == 0) {
                break;
            }
        }

        return buffer;
    }


    public class NoteOn extends ChannelEvent {
        public NoteOn(long tick, int channel, int note, int velocity) {
            super(tick, NOTE_ON, channel, note, velocity);
        }

        public NoteOn(long tick, long delta, int channel, int note, int velocity) {
            super(tick, delta, NOTE_ON, channel, note, velocity);

        }


        public int getNoteValue() {

            return mValue1;
        }

        public int getVelocity() {
            return mValue2;
        }

        public void setNoteValue(int p) {
            mValue1 = p;
        }

        public void setVelocity(int v) {
            mValue2 = v;
        }
    }

    public class NoteOff extends ChannelEvent {
        public NoteOff(long tick, int channel, int note, int velocity) {
            super(tick, NOTE_OFF, channel, note, velocity);
        }

        public NoteOff(long tick, long delta, int channel, int note, int velocity) {
            super(tick, delta, NOTE_OFF, channel, note, velocity);
        }

        public int getNoteValue() {
            return mValue1;
        }

        public int getVelocity() {
            return mValue2;
        }

        public void setNoteValue(int p) {
            mValue1 = p;
        }

        public void setVelocity(int v) {
            mValue2 = v;
        }
    }

    public class PitchBend extends ChannelEvent {
        public PitchBend(long tick, int channel, int lsb, int msb) {
            super(tick, PITCH_BEND, channel, lsb, msb);
        }

        public PitchBend(long tick, long delta, int channel, int lsb, int msb) {
            super(tick, delta, PITCH_BEND, channel, lsb, msb);
        }

        public int getLeastSignificantBits() {
            return mValue1;
        }

        public int getMostSignificantBits() {
            return mValue2;
        }

        public int getBendAmount() {
            int y = (mValue2 & 0x7F) << 7;
            int x = (mValue1);

            return y + x;
        }

        public void setLeastSignificantBits(int p) {
            mValue1 = p & 0x7F;
        }

        public void setMostSignificantBits(int p) {
            mValue2 = p & 0x7F;
        }

        public void setBendAmount(int amount) {
            amount = amount & 0x3FFF;
            mValue1 = (amount & 0x7F);
            mValue2 = amount >> 7;
        }
    }

    public class NoteAftertouch extends ChannelEvent {
        public NoteAftertouch(long tick, int channel, int note, int amount) {
            super(tick, NOTE_AFTERTOUCH, channel, note, amount);
        }

        public NoteAftertouch(long tick, long delta, int channel, int note, int amount) {
            super(tick, delta, NOTE_AFTERTOUCH, channel, note, amount);
        }

        public int getNoteValue() {
            return mValue1;
        }

        public int getAmount() {
            return mValue2;
        }

        public void setNoteValue(int p) {
            mValue1 = p;
        }

        public void setAmount(int a) {
            mValue2 = a;
        }
    }

    public class Controller extends ChannelEvent {
        public Controller(long tick, int channel, int controllerType, int value) {
            super(tick, CONTROLLER, channel, controllerType, value);
        }

        public Controller(long tick, long delta, int channel, int controllerType, int value) {
            super(tick, delta, CONTROLLER, channel, controllerType, value);
        }

        public int getControllerType() {
            return mValue1;
        }

        public int getValue() {
            return mValue2;
        }

        public void setControllerType(int t) {
            mValue1 = t;
        }

        public void setValue(int v) {
            mValue2 = v;
        }
    }

    public class ProgramChange extends ChannelEvent {
        public ProgramChange(long tick, int channel, int program) {
            super(tick, PROGRAM_CHANGE, channel, program, 0);
        }

        public ProgramChange(long tick, long delta, int channel, int program) {
            super(tick, delta, PROGRAM_CHANGE, channel, program, 0);
        }

        public int getProgramNumber() {
            return mValue1;
        }

        public void setProgramNumber(int p) {
            mValue1 = p;
        }

       /* public enum MidiProgram
        {
            ACOUSTIC_GRAND_PIANO, BRIGHT_ACOUSTIC_PIANO, ELECTRIC_GRAND_PIANO, HONKYTONK_PIANO, ELECTRIC_PIANO_1, ELECTRIC_PIANO_2, HARPSICHORD, CLAVINET, CELESTA, GLOCKENSPIEL, MUSIC_BOX, VIBRAPHONE, MARIMBA, XYLOPHONE, TUBULAR_BELLS, DULCIMER, DRAWBAR_ORGAN, PERCUSSIVE_ORGAN, ROCK_ORGAN, CHURCH_ORGAN, REED_ORGAN, ACCORDION, HARMONICA, TANGO_ACCORDION, ACOUSTIC_GUITAR_NYLON, ACOUSTIC_GUITAR_STEEL, ELECTRIC_GUITAR_JAZZ, ELECTRIC_GUITAR_CLEAN, ELECTRIC_GUITAR_MUTED, OVERDRIVEN_GUITAR, DISTORTION_GUITAR, GUITAR_HARMONICS, ACOUSTIC_BASS, ELECTRIC_BASS_FINGER, ELECTRIC_BASS_PICK, FRETLESS_BASS, SLAP_BASS_1, SLAP_BASS_2, SYNTH_BASS_1, SYNTH_BASS_2, VIOLIN, VIOLA, CELLO, CONTRABASS, TREMOLO_STRINGS, PIZZICATO_STRINGS, ORCHESTRAL_HARP, TIMPANI, STRING_ENSEMBLE_1, STRING_ENSEMBLE_2, SYNTH_STRINGS_1, SYNTH_STRINGS_2, CHOIR_AAHS, VOICE_OOHS, SYNTH_CHOIR, ORCHESTRA_HIT, TRUMPET, TROMBONE, TUBA, MUTED_TRUMPET, FRENCH_HORN, BRASS_SECTION, SYNTH_BRASS_1, SYNTH_BRASS_2, SOPRANO_SAX, ALTO_SAX, TENOR_SAX, BARITONE_SAX, OBOE, ENGLISH_HORN, BASSOON, CLARINET, PICCOLO, FLUTE, RECORDER, PAN_FLUTE, BLOWN_BOTTLE, SHAKUHACHI, WHISTLE, OCARINA, LEAD_1_SQUARE, LEAD_2_SAWTOOTH, LEAD_3_CALLIOPE, LEAD_4_CHIFF, LEAD_5_CHARANG, LEAD_6_VOICE, LEAD_7_FIFTHS, LEAD_8_BASS_AND_LEAD, PAD_1_NEW_AGE, PAD_2_WARM, PAD_3_POLYSYNTH, PAD_4_CHOIR, PAD_5_BOWED, PAD_6_METALLIC, PAD_7_HALO, PAD_8_SWEEP, FX_1_RAIN, FX_2_SOUNDTRACK, FX_3_CRYSTAL, FX_4_ATMOSPHERE, FX_5_BRIGHTNESS, FX_6_GOBLINS, FX_7_ECHOES, FX_8_SCIFI, SITAR, BANJO, SHAMISEN, KOTO, KALIMBA, BAGPIPE, FIDDLE, SHANAI, TINKLE_BELL, AGOGO, STEEL_DRUMS, WOODBLOCK, TAIKO_DRUM, MELODIC_TOM, SYNTH_DRUM, REVERSE_CYMBAL, GUITAR_FRET_NOISE, BREATH_NOISE, SEASHORE, BIRD_TWEET, TELEPHONE_RING, HELICOPTER, APPLAUSE, GUNSHOT;

            public int programNumber()
            {
                return this.ordinal();
            }
        }
        */
    }

    public class ChannelAftertouch extends ChannelEvent {
        public ChannelAftertouch(long tick, int channel, int amount) {
            super(tick, CHANNEL_AFTERTOUCH, channel, amount, 0);
        }

        public ChannelAftertouch(long tick, long delta, int channel, int amount) {
            super(tick, delta, CHANNEL_AFTERTOUCH, channel, amount, 0);
        }

        public int getAmount() {
            return mValue1;
        }

        public void setAmount(int p) {
            mValue1 = p;
        }
    }

    class EventPrinter implements MidiEventListener {
        private String mLabel;


        public EventPrinter(String label) {

            mLabel = label;
        }


        public void onStart(boolean fromBeginning) {
            if (fromBeginning) {
                System.out.println(mLabel + " Started!");
            } else {
                System.out.println(mLabel + " resumed");
            }
        }


        public void onEvent(MidiEvent event, long ms) {

            //long tick= Long.parseLong(null);

            //int canal= Integer.parseInt(null);
            //int nota= Integer.parseInt(null);
            //int velocidad= Integer.parseInt(null);
            //NoteOn notaOn = new NoteOn( tick,canal,nota,velocidad);
            System.out.println(mLabel + " received event: " + event);

            //Log.d(mLabel, String.valueOf(NoteOn.getNoteValue()));

        }


        public void onStop(boolean finished) {
            if (finished) {
                System.out.println(mLabel + " Finished!");
            } else {
                System.out.println(mLabel + " paused");
            }
        }
    }

    public interface MidiEventListener

    {

        public void onStart(boolean fromBeginning);



        public void onEvent(MidiEvent event, long ms);



        public void onStop(boolean finished);

    }


    public class MidiFile

    {

        public static final int DEFAULT_RESOLUTION = 480;


        private int mType;

        private int mTrackCount;

        private int mResolution;


        private ArrayList<MidiTrack> mTracks;


        public MidiFile()

        {

            this(DEFAULT_RESOLUTION);

        }


        public MidiFile(int resolution)

        {

            this(resolution, new ArrayList<MidiTrack>());

        }


        public MidiFile(int resolution, ArrayList<MidiTrack> tracks)

        {

            mResolution = resolution >= 0 ? resolution : DEFAULT_RESOLUTION;


            mTracks = tracks != null ? tracks : new ArrayList<MidiTrack>();

            mTrackCount = tracks.size();

            mType = mTrackCount > 1 ? 1 : 0;

        }


        public MidiFile(File fileIn) throws FileNotFoundException, IOException

        {

            this(new FileInputStream(fileIn));

        }


        public MidiFile(InputStream rawIn) throws IOException

        {

            BufferedInputStream in = new BufferedInputStream(rawIn);


            byte[] buffer = new byte[HEADER_SIZE];

            in.read(buffer);


            initFromBuffer(buffer);


            mTracks = new ArrayList<MidiTrack>();

            for (int i = 0; i < mTrackCount; i++)

            {

                mTracks.add(new MidiTrack(in));

            }

        }


        public void setType(int type)

        {

            if (type < 0)

            {

                type = 0;

            } else if (type > 2)

            {

                type = 1;

            } else if (type == 0 && mTrackCount > 1)

            {

                type = 1;

            }

            mType = type;

        }


        public int getType()

        {

            return mType;

        }


        public int getTrackCount()

        {

            return mTrackCount;

        }


        public void setResolution(int res)

        {

            if (res >= 0)

            {

                mResolution = res;

            }

        }


        public int getResolution()

        {

            return mResolution;

        }


        public long getLengthInTicks()

        {

            long length = 0;

            for (MidiTrack T : mTracks)

            {

                long l = T.getLengthInTicks();

                if (l > length)

                {

                    length = l;

                }

            }

            return length;

        }


        public ArrayList<MidiTrack> getTracks()

        {

            return mTracks;

        }


        public void addTrack(MidiTrack T)

        {

            addTrack(T, mTracks.size());

        }


        public void addTrack(MidiTrack T, int pos)

        {


            if (pos > mTracks.size())

            {

                pos = mTracks.size();

            } else if (pos < 0)

            {

                pos = 0;

            }


            mTracks.add(pos, T);

            mTrackCount = mTracks.size();

            mType = mTrackCount > 1 ? 1 : 0;

        }


        public void removeTrack(int pos)

        {

            if (pos < 0 || pos >= mTracks.size())

            {

                return;

            }

            mTracks.remove(pos);

            mTrackCount = mTracks.size();

            mType = mTrackCount > 1 ? 1 : 0;

        }


        public void writeToFile(File outFile) throws FileNotFoundException, IOException

        {

            FileOutputStream fout = new FileOutputStream(outFile);


            fout.write(IDENTIFIER);

            fout.write(intToBytes(6, 4));

            fout.write(intToBytes(mType, 2));

            fout.write(intToBytes(mTrackCount, 2));

            fout.write(intToBytes(mResolution, 2));


            for (MidiTrack T : mTracks)

            {

               // T.writeToFile(fout);

            }


            fout.flush();

            fout.close();

        }


        private void initFromBuffer(byte[] buffer)

        {

            if (!bytesEqual(buffer, IDENTIFIER, 0, 4))

            {

                System.out.println("File identifier not MThd. Exiting");

                mType = 0;

                mTrackCount = 0;

                mResolution = DEFAULT_RESOLUTION;

                return;

            }


            mType = bytesToInt(buffer, 8, 2);

            mTrackCount = bytesToInt(buffer, 10, 2);

            mResolution = bytesToInt(buffer, 12, 2);

        }

    }

    public class MidiProcessor

    {

        private static final int PROCESS_RATE_MS = 8;
        private HashMap<Class<? extends MidiEvent>, ArrayList<MidiEventListener>> mEventsToListeners;
        private HashMap<MidiEventListener, ArrayList<Class<? extends MidiEvent>>> mListenersToEvents;
        private MidiFile mMidiFile;
        private boolean mRunning;
        private double mTicksElapsed;
        private long mMsElapsed;
        private int mMPQN;
        private int mPPQ;
        private MetronomeTick mMetronome;
        private MidiTrackEventQueue[] mEventQueues;

        public MidiProcessor(MidiFile input)
        {
            mMidiFile = input;
            mMPQN = Tempo.DEFAULT_MPQN;
            mPPQ = mMidiFile.getResolution();
            mEventsToListeners = new HashMap<Class<? extends MidiEvent>, ArrayList<MidiEventListener>>();
            mListenersToEvents = new HashMap<MidiEventListener, ArrayList<Class<? extends MidiEvent>>>();
            mMetronome = new MetronomeTick(new TimeSignature(), mPPQ);
            this.reset();
        }

        public synchronized void start()
        {
            if(mRunning)
                return;
            mRunning = true;
            new Thread(new Runnable()
            {
                public void run()
                {
                    process();
                }
            }).start();
        }

        public void stop()
        {
            mRunning = false;
        }

        public void reset()
        {
            mRunning = false;
            mTicksElapsed = 0;
            mMsElapsed = 0;
            mMetronome.setTimeSignature(new TimeSignature());
            ArrayList<MidiTrack> tracks = mMidiFile.getTracks();
            if(mEventQueues == null)
            {
                mEventQueues = new MidiTrackEventQueue[tracks.size()];
            }

            for(int i = 0; i < tracks.size(); i++)
            {
                mEventQueues[i] = new MidiTrackEventQueue(tracks.get(i));
            }
        }

        public boolean isStarted()
        {
            return mTicksElapsed > 0;
        }

        public boolean isRunning()
        {
            return mRunning;
        }

        protected void onStart(boolean fromBeginning)
        {
            Iterator<MidiEventListener> it = mListenersToEvents.keySet().iterator();
            while(it.hasNext())
            {
                MidiEventListener mel = it.next();
                mel.onStart(fromBeginning);
            }
        }

        protected void onStop(boolean finished)
        {
            Iterator<MidiEventListener> it = mListenersToEvents.keySet().iterator();
            while(it.hasNext())
            {
                MidiEventListener mel = it.next();
                mel.onStop(finished);
            }
        }

        public void registerEventListener(MidiEventListener mel, Class<? extends MidiEvent> event)
        {
            ArrayList<MidiEventListener> listeners = mEventsToListeners.get(event);
            if(listeners == null)
            {
                listeners = new ArrayList<MidiEventListener>();
                listeners.add(mel);
                mEventsToListeners.put(event, listeners);
            }
            else
            {
                listeners.add(mel);
            }

            ArrayList<Class<? extends MidiEvent>> events = mListenersToEvents.get(mel);

            if(events == null)
            {
                events = new ArrayList<Class<? extends MidiEvent>>();
                events.add(event);
                mListenersToEvents.put(mel, events);
            }
            else
            {
                events.add(event);
            }
        }

        public void unregisterEventListener(MidiEventListener mel)
        {
            ArrayList<Class<? extends MidiEvent>> events = mListenersToEvents.get(mel);

            if(events == null)
            {
                return;
            }
            for(Class<? extends MidiEvent> event : events)
            {
                ArrayList<MidiEventListener> listeners = mEventsToListeners.get(event);
                listeners.remove(mel);
            }
            mListenersToEvents.remove(mel);
        }

        public void unregisterEventListener(MidiEventListener mel, Class<? extends MidiEvent> event)
        {
            ArrayList<MidiEventListener> listeners = mEventsToListeners.get(event);
            if(listeners != null)
            {
                listeners.remove(mel);
            }
            ArrayList<Class<? extends MidiEvent>> events = mListenersToEvents.get(mel);
            if(events != null)
            {
                events.remove(event);
            }
        }

        public void unregisterAllEventListeners()
        {
            mEventsToListeners.clear();
            mListenersToEvents.clear();
        }

        protected void dispatch(MidiEvent event)
        {
            // Tempo and Time Signature events are always needed by the processor
            if(event.getClass().equals(Tempo.class))
            {
                mMPQN = ((Tempo) event).getMpqn();
            }
            else if(event.getClass().equals(TimeSignature.class))
            {
                boolean shouldDispatch = mMetronome.getBeatNumber() != 1;
                mMetronome.setTimeSignature((TimeSignature) event);
                if(shouldDispatch)
                {
                    dispatch(mMetronome);
                }
            }
            this.sendOnEventForClass(event, event.getClass());
            this.sendOnEventForClass(event, MidiEvent.class);
        }

        private void sendOnEventForClass(MidiEvent event, Class<? extends MidiEvent> eventClass)
        {
            ArrayList<MidiEventListener> listeners = mEventsToListeners.get(eventClass);
            if(listeners == null)
            {
                return;
            }
            for(MidiEventListener mel : listeners)
            {
                mel.onEvent(event, mMsElapsed);
            }
        }

        private void process()
        {
            onStart(mTicksElapsed < 1);
            long lastMs = System.currentTimeMillis();
            boolean finished = false;
            while(mRunning)
            {
                long now = System.currentTimeMillis();
                long msElapsed = now - lastMs;
                if(msElapsed < PROCESS_RATE_MS)
                {
                    try
                    {
                        Thread.sleep(PROCESS_RATE_MS - msElapsed);
                    }
                    catch(Exception e)
                    {
                    }
                    continue;
                }
                double ticksElapsed = msToTicks(msElapsed, mMPQN, mPPQ);
                if(ticksElapsed < 1)
                {
                    continue;
                }
                if(mMetronome.update(ticksElapsed))
                {
                    dispatch(mMetronome);
                }
                lastMs = now;
                mMsElapsed += msElapsed;
                mTicksElapsed += ticksElapsed;

                boolean more = false;
                for(int i = 0; i < mEventQueues.length; i++)
                {
                    MidiTrackEventQueue queue = mEventQueues[i];
                    if(!queue.hasMoreEvents())
                    {
                        continue;
                    }
                    ArrayList<MidiEvent> events = queue.getNextEventsUpToTick(mTicksElapsed);
                    for(MidiEvent event : events)
                    {
                        this.dispatch(event);
                    }
                    if(queue.hasMoreEvents())
                    {
                        more = true;
                    }
                }

                if(!more)
                {
                    finished = true;
                    break;
                }
            }

            mRunning = false;
            onStop(finished);
        }

        private class MidiTrackEventQueue
        {
            private MidiTrack mTrack;
            private Iterator<MidiEvent> mIterator;
            private ArrayList<MidiEvent> mEventsToDispatch;
            private MidiEvent mNext;
            public MidiTrackEventQueue(MidiTrack track)
            {
                mTrack = track;
                mIterator = mTrack.getEvents().iterator();
                mEventsToDispatch = new ArrayList<MidiEvent>();
                if(mIterator.hasNext())
                {
                    mNext = mIterator.next();
                }
            }

            public ArrayList<MidiEvent> getNextEventsUpToTick(double tick)
            {
                mEventsToDispatch.clear();
                while(mNext != null)
                {
                    if(mNext.getTick() <= tick)
                    {
                        mEventsToDispatch.add(mNext);
                        if(mIterator.hasNext())
                        {
                            mNext = mIterator.next();
                        }
                        else
                        {
                            mNext = null;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
                return mEventsToDispatch;
            }
            public boolean hasMoreEvents()
            {
                return mNext != null;
            }
        }
    }

    /**
     * An event specifically for MidiProcessor to broadcast metronome ticks so that
     * observers need not rely on time conversions or measure tracking
     */
    public class MetronomeTick extends MidiEvent

    {

        private int mResolution;

        private TimeSignature mSignature;



        private int mCurrentMeasure;

        private int mCurrentBeat;



        private double mMetronomeProgress;

        private int mMetronomeFrequency;



        public MetronomeTick(TimeSignature sig, int resolution)

        {

            super(0, 0);



            mResolution = resolution;



            setTimeSignature(sig);

            mCurrentMeasure = 1;

        }



        public void setTimeSignature(TimeSignature sig)

        {

            mSignature = sig;

            mCurrentBeat = 0;



            setMetronomeFrequency(sig.getMeter());

        }



        public boolean update(double ticksElapsed)

        {

            mMetronomeProgress += ticksElapsed;



            if(mMetronomeProgress >= mMetronomeFrequency)

            {



                mMetronomeProgress %= mMetronomeFrequency;



                mCurrentBeat = (mCurrentBeat + 1) % mSignature.getNumerator();

                if(mCurrentBeat == 0)

                {

                    mCurrentMeasure++;

                }



                return true;

            }

            return false;

        }



        public void setMetronomeFrequency(int meter)

        {

            switch(meter)

            {

                case TimeSignature.METER_EIGHTH:

                    mMetronomeFrequency = mResolution / 2;

                    break;

                case TimeSignature.METER_QUARTER:

                    mMetronomeFrequency = mResolution;

                    break;

                case TimeSignature.METER_HALF:

                    mMetronomeFrequency = mResolution * 2;

                    break;

                case TimeSignature.METER_WHOLE:

                    mMetronomeFrequency = mResolution * 4;

                    break;

            }

        }



        public int getBeatNumber()

        {

            return mCurrentBeat + 1;

        }



        public int getMeasure()

        {

            return mCurrentMeasure;

        }



        @Override

        public String toString()

        {

            return "Metronome: " + mCurrentMeasure + "\t" + getBeatNumber();

        }



        @Override

        public int compareTo(MidiEvent o)

        {

            return 0;

        }



        @Override

        protected int getEventSize()

        {

            return 0;

        }



        @Override

        public int getSize()

        {

            return 0;

        }

    }

    public static double msToTicks(long ms, int mpqn, int ppq)
    {
        return ((ms * 1000.0) * ppq) / mpqn;
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //crea una conexion segura con un dispositivo BT usando UUID
    }

    /*Esto, junto con el onDestroy, hacen que la pantalla siga encendida hasta que la actividad termine*/
    protected void onDestroy(){
        super.onDestroy();

        this.wakelock.release();
    }

    public void onResume() {
        super.onResume();

        wakelock.acquire();
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
        this.wakelock.release();

        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        this.wakelock.release();
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
