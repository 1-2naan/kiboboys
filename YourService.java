package jp.jaxa.iss.kibo.rpc.sampleapk;


import android.graphics.Bitmap;
import android.util.Log;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;

import com.google.zxing.FormatException;

import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.common.HybridBinarizer;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import java.lang.String;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    @Override
    protected void runPlan1(String args[]){
        // astrobee is undocked and the mission starts
        api.startMission();

        // move to Point A
        moveToWrapper(11.21, -9.8, 4.79, 0, 0, -0.707, 0.707);

        api.flashlightControlFront(0.025f);
        //Read QR code from point-A
        Bitmap image = api.getBitmapNavCam();
        Bitmap cropped_image = Bitmap.createBitmap(image,0,0,960,960);
        api.flashlightControlFront(0);
        String content = readQr(cropped_image);
        String[] stringArray1= newString[4];
        int element = 0;
        for (int i = 1; i < content.length();++i)
        {
            if (content.charAt(i)==':')
            {
                int condition= 0;
                int initial= i+1;

                for (int j=i;condition<1;++j)
                {
                    if (content.charAt(j)==',')
                    {
                        stringArray1[element]= content.substring(initial,j-1);
                        condition=1;
                        element++;

                    }
                }

            }
        }


        api.sendDiscoveredQR(content);
        // astrobee is undocked and the mission starts
        String pattern = stringArray1[0];
        double pattern1 = Double.parseDouble(pattern);
        double x_Adash = Double.parseDouble(stringArray1[1]);
        double y_Adash = Double.parseDouble(stringArray1[2]);
        double z_Adash = Double.parseDouble(stringArray1[3]);

        if (pattern1==1.0 || pattern1==2.0 || pattern1==8.0)
        {
            moveToWrapper(x_Adash,y_Adash,z_Adash, 0.0, 0.0, -0.707, 0.707);

        }


        if (pattern1==3.0 || pattern1==4.0)
        {
            moveToWrapper(x_Adash,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_Adash,-9.8,z_Adash, 0.0, 0.0, -0.707, 0.707);
        }

        if (pattern1==5.0 || pattern1==6.0)
        {
            double x_KOZL = x_Adash - 0.22;
            moveToWrapper(x_KOZL,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_KOZL,-9.8,z_Adash, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_Adash,-9.8,z_Adash, 0.0, 0.0, -0.707, 0.707);



        }
        if (pattern1==7.0)
        {
            double x_KOZR = x_Adash;
            moveToWrapper(x_KOZR,-9.8,4.79, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_KOZR,-9.8,z_Adash, 0.0, 0.0, -0.707, 0.707);
            moveToWrapper(x_Adash,-9.8,z_Adash, 0.0, 0.0, -0.707, 0.707);
        }




        // Send mission completion
        api.reportMissionCompletion();
    }

    @Override
    protected void runPlan2(){
        // write here your plan 2
    }

    @Override
    protected void runPlan3(){
        // write here your plan 3
    }

    // You can add your method
    private void moveToWrapper(double pos_x, double pos_y, double pos_z,
                               double qua_x, double qua_y, double qua_z,
                               double qua_w){

        final int LOOP_MAX = 3;
        final Point point = new Point(pos_x, pos_y, pos_z);
        final Quaternion quaternion = new Quaternion((float)qua_x, (float)qua_y,
                (float)qua_z, (float)qua_w);

        Result result = api.moveTo(point, quaternion, true);

        int loopCounter = 0;
        while(!result.hasSucceeded() || loopCounter < LOOP_MAX){
            result = api.moveTo(point, quaternion, true);
            ++loopCounter;
        }
    }

}