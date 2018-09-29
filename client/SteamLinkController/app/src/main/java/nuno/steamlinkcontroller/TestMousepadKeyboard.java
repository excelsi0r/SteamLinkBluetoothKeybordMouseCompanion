package nuno.steamlinkcontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import static android.view.MotionEvent.*;

public class TestMousepadKeyboard extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mousepad_keyboard);

        final ImageView img = findViewById(R.id.imageView);
        final Button leftButton = findViewById(R.id.left_mouse);
        final Button rightButton = findViewById(R.id.right_mouse);

        leftButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        //send left mouse button event
                            //BTN_LEFT 1

                        Log.d("EVENTMOUSE", "left mouse down");
                        break;

                    case (ACTION_UP):
                        //send left mouse button event
                            //BTN_LEFT 0

                        Log.d("EVENTMOUSE", "left mouse up");
                        break;
                }

                return true;
            }
        });

        rightButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        //send right mouse button event
                            //BTN_RIGHT 1

                        Log.d("EVENTMOUSE", "right mouse down");
                        break;

                    case (ACTION_UP):
                        //send right mouse button event
                            //BTN_RIGHT 0

                        Log.d("EVENTMOUSE", "right mouse up");
                        break;
                }

                return true;
            }
        });

        img.setOnTouchListener(new View.OnTouchListener()
        {
            long tapDown = 0;
            long tapUp = 1;

            int posX = 0;
            int posY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction() & ACTION_MASK)
                {
                    case (ACTION_DOWN):
                        //send tap down event
                            //BTN_TOOL_MOUSE 1
                            //BTN_TOUCH 1

                        Log.d("EVENTMOUSE", "1 finger down");
                        break;
                    case (ACTION_UP):
                        //send tapUp event
                            //BTN_TOOL_MOUSE 0
                            //BTN_TOUCH 0

                        Log.d("EVENTMOUSE", "1 finger up");
                        break;
                    case (ACTION_MOVE):
                        int diffX = (int) motionEvent.getX() - posX;
                        int diffY = (int) motionEvent.getY() - posY;

                        //send rel event
                            //REL_X diffX
                            //REL_T diffY

                        Log.d("EVENTMOUSE", "movement: " + diffX + ", " + diffY);
                        break;
                    case (ACTION_POINTER_DOWN):
                        //send double finger down event
                            //BTN_TOOL_DOUBLETAP 1
                            //BTN_TOUCH 1

                        Log.d("EVENTMOUSE", "2 finger down");
                        break;
                    case(ACTION_POINTER_UP):
                        //send double finger up event
                            //BTN_TOOL_DOUBLETAP 1
                            //BTN_TOUCH 1

                        Log.d("EVENTMOUSE", "2 finger up");
                        break;
                }

                posX = (int) motionEvent.getX();
                posY = (int) motionEvent.getY();

                return true;
            }
        });
    }

    public void toast()
    {
        Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
    }
}
