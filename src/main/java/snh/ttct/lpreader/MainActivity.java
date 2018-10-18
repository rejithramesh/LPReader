package snh.ttct.lpreader;

import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.Utils;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.ml.KNearest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    public static final String TESS_DATA = "/tessdata";
    Bitmap img = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!OpenCVLoader.initDebug())
        {
            System.out.println("Opencv STOP");
        }else
        {
            System.out.println("Opencv GO");
        }
        final ImageView imageView1, imageView2;
        Button btn,cam;
        final TextView result;
        result=(TextView) findViewById(R.id.result);
        prepareTessData();
        final MyTessOCR mTessOCR = new MyTessOCR(this);

        Context context = this;
        try {
            img = BitmapFactory.decodeStream(context.openFileInput("myImage"));
        }catch(Exception e)
        {
            img=BitmapFactory.decodeResource(getResources(),R.drawable.xx);
        }

        imageView1=(ImageView)findViewById(R.id.imageView);
        imageView2=(ImageView)findViewById(R.id.imageView2);
        btn =(Button)findViewById(R.id.button);
        cam =(Button)findViewById(R.id.button2);
        imageView2.setImageBitmap(img);

        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(),GameActivity.class);
                startActivity(i);
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Bitmap img = BitmapFactory.decodeResource(getResources(),R.drawable.xx);
                Mat source = new Mat();
                Mat dest = new Mat();
                Utils.bitmapToMat( img, source);

                //Convert the image to greyscale
                Imgproc.cvtColor(source, dest, Imgproc.COLOR_RGB2GRAY);
                //Apply gaussian blur on the grayscale image
                Imgproc.GaussianBlur(dest,dest,new Size(45,45), 0);


               //Apply thresholding to return black and white.Black background and white shapes
                Imgproc.adaptiveThreshold(dest,dest,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,
                        Imgproc.THRESH_BINARY_INV,75,10);

                //Setting up matrix points for contours
               List<MatOfPoint> contours = new ArrayList<>();
                Mat desti = Mat.zeros(dest.size(), CvType.CV_8UC3);
                Scalar white = new Scalar(255, 255, 255);

                //Finding contorus from processed image
                Imgproc.findContours(dest, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

               // Draw contours in destination Mat
                Imgproc.drawContours(desti, contours, -1, white);
                KNearest knn = KNearest.create();
                //Fille the detected contours with white
                 for (MatOfPoint contour: contours) {
                     Imgproc.fillPoly(desti, Arrays.asList(contour), white);
                 }

                //Surround detected contours with rectangles
                Mat image_roi=null;
                Scalar green = new Scalar(81, 190, 0);
                for (MatOfPoint contour: contours) {

                    Double area=Imgproc.contourArea(contour);
                    if(area>6000) {
                        System.out.println("Area - "+area);
                        Rect p=Imgproc.boundingRect(contour);
                        image_roi= new Mat(source,p);
                        RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
                       drawRotatedRect(desti, rotatedRect, green, 4);
                    }
                }

                //Create bitmap from the processed image and set the imageview
                Bitmap btmp = Bitmap.createBitmap(desti.width(),desti.height(),Bitmap.Config.ARGB_8888); Utils. matToBitmap(desti,btmp);
                imageView1. setImageBitmap (btmp);



                try {

                    String extractedText = mTessOCR.getOCRResult(img);
                    System.out.println("GOT TXT --- > " + extractedText);
                    result.setText(extractedText);

                }catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
        });
    }

    public static void drawRotatedRect(Mat image, RotatedRect rotatedRect, Scalar color, int thickness) {
        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        MatOfPoint points = new MatOfPoint(vertices);
        Imgproc.drawContours(image, Arrays.asList(points), -1, color, thickness);
    }

    private void prepareTessData() {
        try {
            File dir = getExternalFilesDir(TESS_DATA);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                   System.out.println( "The folder " + dir.getPath() + "was not created");
                }
            }
            String fileList[] = getAssets().list("tessdata");

            for (String fileName : fileList) {
                String pathToDataFile = dir + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {
                    InputStream in = getAssets().open("tessdata/" + fileName);
                    OutputStream out = new FileOutputStream(pathToDataFile);
                    byte[] buff = new byte[1024];
                    int len;
                    while ((len = in.read(buff)) > 0) {
                        out.write(buff, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
