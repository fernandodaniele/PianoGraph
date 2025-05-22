package com.example.fernandodaniele.almacenamiento;

import com.example.fernandodaniele.almacenamiento.event.MidiEvent;
import com.example.fernandodaniele.almacenamiento.util.MidiEventListener;

public class EventPrinter implements MidiEventListener
{
    private String mLabel;


    public EventPrinter(String label)
    {

        mLabel = label;
    }

    @Override
    public void onStart(boolean fromBeginning)
    {
        if(fromBeginning)
        {
            System.out.println(mLabel + " Started!");
        }
        else
        {
            System.out.println(mLabel + " resumed");
        }
    }

    @Override
    public void onEvent(MidiEvent event, long ms)
    {

        //long tick= Long.parseLong(null);

        //int canal= Integer.parseInt(null);
        //int nota= Integer.parseInt(null);
        //int velocidad= Integer.parseInt(null);
        //NoteOn notaOn = new NoteOn( tick,canal,nota,velocidad);
        System.out.println(mLabel + " received event: " + event);

        //Log.d(mLabel, String.valueOf(NoteOn.getNoteValue()));

    }



    @Override
    public void onStop(boolean finished)
    {
        if(finished)
        {
            System.out.println(mLabel + " Finished!");
        }
        else
        {
            System.out.println(mLabel + " paused");
        }
    }
}
