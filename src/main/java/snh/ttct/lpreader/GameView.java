package snh.ttct.lpreader;

/**
 * Created by ADMIN on 10/17/2018.
 */
import android.content.*;
import android.view.*;
import android.graphics.*;

public class GameView extends SurfaceView {

    private SurfaceHolder holder;
    private Bitmap bmp;

    public GameView(Context context) {
        super(context);
        this.bmp = BitmapFactory.decodeResource(getResources(), R.drawable.launcher_icon);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    draw(canvas);
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

        });
    }

    public void draw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(this.bmp, 25, 25, null);
    }

}