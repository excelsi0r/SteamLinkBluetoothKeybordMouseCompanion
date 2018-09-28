package nuno.steamlinkcontroller;

import android.annotation.SuppressLint;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import static android.view.MotionEvent.*;

public class TestMousepadKeyboard extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mousepad_keyboard);

        final ImageView img = findViewById(R.id.imageView);


        img.setOnTouchListener(new View.OnTouchListener()
        {
            long lastTap = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction())
                {
                    case (ACTION_UP):
                        Log.d("EventMotion", "Action up");
                        if(System.currentTimeMillis() - lastTap < 200)
                        {
                            Log.d("EventMotion", "Double Tap");
                            doubleTap();
                        }
                        lastTap = System.currentTimeMillis();
                        break;
                    case (ACTION_DOWN):
                        Log.d("EventMotion", "Action down");
                        break;
                    case (ACTION_MOVE):
                        Log.d("COORDINATES: ", "X: " + motionEvent.getX() +", Y: " + motionEvent.getY());
                        break;
                }

                return true;
            }
        });

    }

    public void doubleTap()
    {
        Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show();
    }
}
