package com.example.chuchun.ball_bubble;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by chuchun on 2016/6/3.
 */
/*public class SurfaceViewActivity {

}*/
public class SurfaceViewActivity extends Activity {
    MyView mAnimView = null;
    private SensorManager mSensorManager;   //體感(Sensor)使用管理
    private Sensor mSensor;                 //體感(Sensor)類別
    private float mLastX;                    //x軸體感(Sensor)偏移
    private float mLastY;                    //y軸體感(Sensor)偏移
    private float mLastZ;                    //z軸體感(Sensor)偏移
    private double mSpeed;                 //甩動力道數度
    private long mLastUpdateTime;           //觸發時間
    boolean ball=true;
    int time=0;


    //甩動力道數度設定值 (數值越大需甩動越大力，數值越小輕輕甩動即會觸發)
    private static final int SPEED_SHRESHOLD = 3000;
    //觸發間隔時間
    private static final int UPTATE_INTERVAL_TIME = 70;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //System.out.println("Enter SurfaceViewActivity");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //System.out.println("SurfaceView onCreate!");

        mAnimView = new MyView(this);
        setContentView(mAnimView);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(SensorListener, mSensor,SensorManager.SENSOR_DELAY_GAME);
    }
    public class MyView extends SurfaceView implements SurfaceHolder.Callback,Runnable ,SensorEventListener {
        public static final int TIME_IN_FRAME = 50;
        Paint mPaint = null;
        Paint mTextPaint = null;
        SurfaceHolder mSurfaceHolder = null;
        Canvas mCanvas = null;
        Sensor mSensor = null;
        boolean mIsRunning = false;
        private SensorManager mSensorMgr = null;
        int mScreenWidth = 0, mScreenHeight = 0;
        private int mScreenBallWidth = 0, mScreenBallHeight = 0;
        /**
         * 小球資源檔
         **/
        private Bitmap mbitmapBall;
        private Bitmap mbitmapBubble;
        /**
         * 遊戲背景
         **/
        private Bitmap mbitmapBg;
        /**
         * 小球的座標位置
         **/
        private float mPosX = 200;
        private float mPosY = 0;
        /**
         * 重力感應X軸 Y軸 Z軸的重力值
         **/
        private float mGX = 0;
        private float mGY = 0;
        private float mGZ = 0;

        public MyView(Context context) {
            super(context);
            System.out.println("SurfaceView MyView!");
            /** 設置當前View擁有控制焦點 **/
            this.setFocusable(true);
            /** 設置當前View擁有觸摸事件 **/
            this.setFocusableInTouchMode(true);
            /** 拿到SurfaceHolder物件 **/
            mSurfaceHolder = this.getHolder();
            /** 將mSurfaceHolder添加到Callback回呼函數中 **/
            mSurfaceHolder.addCallback(this);
            /** 創建畫布 **/
            mCanvas = new Canvas();
            /** 創建曲線畫筆 **/
            mPaint = new Paint();
            mPaint.setColor(Color.WHITE);
            /**載入小球資源**/
            mbitmapBall = BitmapFactory.decodeResource(this.getResources(), R.drawable.ball);
            /**載入遊戲背景**/
            mbitmapBg = BitmapFactory.decodeResource(this.getResources(), R.drawable.bg);
            mbitmapBubble = BitmapFactory.decodeResource(this.getResources(), R.drawable.bubble);
/**得到SensorManager物件**/
            mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
            mSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // 註冊listener，第三個參數是檢測的精確度
            //SENSOR_DELAY_FASTEST 最靈敏 因為太快了沒必要使用
            //SENSOR_DELAY_GAME 遊戲開發中使用
            //SENSOR_DELAY_NORMAL 正常速度
            //SENSOR_DELAY_UI 最慢的速度
            mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        }
        private void Draw() {
            System.out.println("Draw!!!!!");
            /**繪製遊戲背景**/
            mCanvas.drawBitmap(mbitmapBg,0,0, mPaint);
            /**繪製小球**/
            if(ball) {
                mCanvas.drawBitmap(mbitmapBall, mPosX, mPosY, mPaint);
            }
            else{
                mCanvas.drawBitmap(mbitmapBubble, mPosX, mPosY, mPaint);
            }
            /**X軸 Y軸 Z軸的重力值**/
            mCanvas.drawText("X軸重力值 ：" + mGX, 0, 20, mPaint);
            mCanvas.drawText("Y軸重力值 ：" + mGY, 0, 40, mPaint);
            mCanvas.drawText("Z軸重力值 ：" + mGZ, 0, 60, mPaint);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {    }
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            /**開始遊戲主迴圈執行緒**/
            mIsRunning = true;
            new Thread(this).start();
            /**得到當前螢幕寬高**/
            mScreenWidth = this.getWidth();
            mScreenHeight = this.getHeight();
            /**得到小球越界區域**/
            mScreenBallWidth = mScreenWidth - mbitmapBall.getWidth();
            mScreenBallHeight = mScreenHeight - mbitmapBall.getHeight();
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsRunning = false;
        }
        @Override
        public void run() {
            while (mIsRunning) {
                System.out.println("Run!");
                /** 取得更新遊戲之前的時間 **/
                long startTime = System.currentTimeMillis();
                /** 在這裡加上執行緒安全鎖 **/
                synchronized (mSurfaceHolder) {
                    /** 拿到當前畫布 然後鎖定 **/
                    mCanvas = mSurfaceHolder.lockCanvas();
                    Draw();
                    /** 繪製結束後解鎖顯示在螢幕上 **/
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
                /** 取得更新遊戲結束的時間 **/
                long endTime = System.currentTimeMillis();
                /** 計算出遊戲一次更新的毫秒數 **/
                int diffTime = (int) (endTime - startTime);
                /** 確保每次更新時間為50幀 **/
                while (diffTime <= TIME_IN_FRAME) {
                    diffTime = (int) (System.currentTimeMillis() - startTime);
                    /** 執行緒等待 **/
                    Thread.yield();
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stubF
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            mGX = event.values[0];
            mGY= event.values[1];
            mGZ = event.values[2];
            //這裡乘以2是為了讓小球移動的更快
            if(ball) {
                mPosY += mGX * 3;
                mPosX += mGY * 3;
            }
            else{
                mPosY -= mGX * 3;
                mPosX -= mGY * 3;
            }
            //檢測小球是否超出邊界
            if (mPosX < 0) {
                mPosX = 0;
            } else if (mPosX > mScreenBallWidth) {
                mPosX = mScreenBallWidth;
            }
            if (mPosY < 0) {
                mPosY = 0;
            } else if (mPosY > mScreenBallHeight) {
                mPosY = mScreenBallHeight;
            }
        }
    }
    private SensorEventListener SensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent mSensorEvent) {
// 當前觸發時間
            long mCurrentUpdateTime = System.currentTimeMillis();
// 觸發間隔時間 = 當前觸發時間 - 上次觸發時間
            long mTimeInterval = mCurrentUpdateTime - mLastUpdateTime;
// 若觸發間隔時間< 70 則return;
            if (mTimeInterval < UPTATE_INTERVAL_TIME)
                return;

            mLastUpdateTime = mCurrentUpdateTime;
// 取得xyz體感(Sensor)偏移
            float x = mSensorEvent.values[0];
            float y = mSensorEvent.values[1];
            float z = mSensorEvent.values[2];
// 甩動偏移速度 = xyz體感(Sensor)偏移 - 上次xyz體感(Sensor)偏移
            float mDeltaX = x - mLastX;
            float mDeltaY = y - mLastY;
            float mDeltaZ = z - mLastZ;
            mLastX = x;
            mLastY = y;
            mLastZ = z;
// 體感(Sensor)甩動力道速度公式
            mSpeed = Math.sqrt(mDeltaX * mDeltaX + mDeltaY * mDeltaY + mDeltaZ
                    * mDeltaZ)
                    / mTimeInterval * 10000;
// 若體感(Sensor)甩動速度大於等於甩動設定值則進入 (達到甩動力道及速度)
            if (mSpeed >= SPEED_SHRESHOLD) {
// 達到搖一搖甩動後要做的事情
                time++;
            }
            else
            {
                if (time > 10) {
                    ball=!ball;
                    time=0;
                }
            }

        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}

