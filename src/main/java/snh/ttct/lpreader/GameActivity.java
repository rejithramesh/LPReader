package snh.ttct.lpreader;

/**
 * Created by ADMIN on 10/17/2018.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GameActivity extends Activity implements SurfaceHolder.Callback {




        TextView testView;

        LinearLayout capture;

        Camera camera;

        SurfaceView surfaceView,transparentView;

        SurfaceHolder surfaceHolder,holderTransparent;
    private float RectLeft, RectTop,RectRight,RectBottom ;

    int  deviceHeight,deviceWidth;


        Camera.PictureCallback rawCallback;

        Camera.ShutterCallback shutterCallback;

        Camera.PictureCallback jpegCallback;



        /** Called when the activity is first created. */

        @Override

        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);



            setContentView(R.layout.cam_layout);



            surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

            surfaceHolder = surfaceView.getHolder();

            transparentView = (SurfaceView)findViewById(R.id.surfaceView2);



            holderTransparent = transparentView.getHolder();

            capture=(LinearLayout) findViewById(R.id.capture);

            // Install a SurfaceHolder.Callback so we get notified when the

            // underlying surface is created and destroyed.

            surfaceHolder.addCallback(this);

            holderTransparent.addCallback(this);

            holderTransparent.setFormat(PixelFormat.TRANSLUCENT);

            transparentView.setZOrderMediaOverlay(true);

            // deprecated setting, but required on Android versions prior to 3.0

            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            deviceWidth=getScreenWidth();

            deviceHeight=getScreenHeight();


            jpegCallback = new Camera.PictureCallback() {

                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Intent intent = new Intent(getBaseContext(),MainActivity.class);
                    int x= (bitmap.getWidth()/5);
                    int y= (bitmap.getHeight()/3);
                    int w=bitmap.getWidth()-x;
                    int z=bitmap.getHeight()-y-y;
                    Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, w, z);
                    createImageFromBitmap(croppedBitmap);
                    startActivity(intent);
                }

            };
            capture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        ProgressWheel wheel = new ProgressWheel(getBaseContext());
                        wheel.setBarColor(Color.BLUE);
                        wheel.spin();
                        captureImage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }



        public void captureImage() throws IOException {

            camera.takePicture(null, null, jpegCallback);

        }



        public void refreshCamera() {

            if (surfaceHolder.getSurface() == null) {

                // preview surface does not exist

                return;

            }



            // stop preview before making changes

            try {

                camera.stopPreview();

            } catch (Exception e) {

                // ignore: tried to stop a non-existent preview

            }


            try {

                camera.setPreviewDisplay(surfaceHolder);

                camera.startPreview();

            } catch (Exception e) {



            }

        }



        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

            // Now that the size is known, set up the camera parameters and begin

            // the preview.

            refreshCamera();

        }



        public void surfaceCreated(SurfaceHolder holder) {

            try {

                // open the camera
              synchronized (holder)

                {Draw();}

                camera = Camera.open();
                camera.setDisplayOrientation(90);

            } catch (RuntimeException e) {

                // check for exceptions

                System.err.println(e);

                return;

            }

            Camera.Parameters param;

            param = camera.getParameters();

            param.setPreviewSize(352, 288);

            camera.setParameters(param);

            try {
                camera.setPreviewDisplay(surfaceHolder);

                camera.startPreview();
                camera.setDisplayOrientation(90);

            } catch (Exception e) {


                System.err.println(e);

                return;

            }

        }



        public void surfaceDestroyed(SurfaceHolder holder) {

            // stop preview and release camera
            try {
                camera.stopPreview();

                camera.release();

                camera = null;
            }catch(Exception e)
            {
                camera = null;
            }

        }


    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "myImage";//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    private void Draw()

    {

        Canvas canvas = holderTransparent.lockCanvas(null);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.STROKE);

        paint.setColor(Color.GREEN);

        paint.setStrokeWidth(3);

        int width =  deviceWidth/3;
        int height =  deviceHeight/5;

        RectLeft = width;

        RectTop = height;

        RectRight = deviceWidth-width;

        RectBottom =deviceHeight-height;

        Rect rec=new Rect((int) RectLeft,(int)RectTop,(int)RectRight,(int)RectBottom);

        canvas.drawRect(rec,paint);

        holderTransparent.unlockCanvasAndPost(canvas);



    }

    public static int getScreenWidth() {

        return Resources.getSystem().getDisplayMetrics().widthPixels;

    }



    public static int getScreenHeight() {

        return Resources.getSystem().getDisplayMetrics().heightPixels;

    }



}