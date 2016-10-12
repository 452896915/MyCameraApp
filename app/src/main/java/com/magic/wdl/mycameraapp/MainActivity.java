package com.magic.wdl.mycameraapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private final int MAX_WIDTH = 768;
    private final int MAX_HEIGHT = 1280;

    @Bind(R.id.preview_surface_view)
    SurfaceView surfaceView;

    @Bind(R.id.image_view)
    ImageView imageView;

    @Bind(R.id.take_photo_btn)
    Button takeButton;

    @Bind(R.id.confirm_btn)
    Button confirmButton;

    private Camera camera;

    private SurfaceHolder surfaceHolder;
    private boolean isCapturing = true;

    private byte[] pictureDataBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        initCamera();
    }


    @Override
    protected void onPause() {
        super.onPause();

        releaseCamera();
    }

    @OnClick(R.id.take_photo_btn)
    protected void onTakePhotoClicked(View v) {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                pictureDataBytes = data;
                showPicture();
            }
        });
    }

    @OnClick(R.id.confirm_btn)
    protected void onConfirmClicked(View v) {
        surfaceView.setVisibility(View.VISIBLE);
        takeButton.setVisibility(View.VISIBLE);

        confirmButton.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        surfaceView.setVisibility(View.VISIBLE);
    }

    private void initCamera() {
        if (camera == null) {
            camera = Camera.open();
        }
    }

    private void startPreview() {
        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);

                if (isCapturing) {
                    camera.startPreview();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to open camera.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void showPicture() {
        Bitmap bitmap = BitmapFactory.decodeByteArray(pictureDataBytes, 0, pictureDataBytes.length);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateSampleSize(bitmap.getHeight(), bitmap.getWidth());
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        InputStream inputStream = bitmapToStream(bitmap);

        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream, null, options));

        imageView.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.GONE);

        surfaceView.setVisibility(View.GONE);
        takeButton.setVisibility(View.GONE);

        confirmButton.setVisibility(View.VISIBLE);
    }

    private InputStream bitmapToStream(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());

        return inputStream;
    }

    // 获取采样比例
    public int calculateSampleSize(int outWidth, int outHeight) {
        int scale = 1;
        if (outHeight > MAX_HEIGHT || outWidth > MAX_WIDTH) {
            int maxSize = MAX_WIDTH > MAX_HEIGHT ? MAX_WIDTH : MAX_HEIGHT;
            scale = (int) Math.pow(2, (int) Math.round(Math.log(maxSize /(double) Math.max(outHeight, outWidth)) / Math.log(0.5)));
        }

        return scale;
    }
}
