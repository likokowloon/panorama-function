package com.dermandar.panoramal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class PreviewActivity extends Activity {

    public static final int RESULT_OK = 201;
    public static final int RESULT_CANCEL = 202;

    public static final String EXTRA_IMAGE_PATH = "image_path";

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        ImageViewTouch ivt = (ImageViewTouch)findViewById(R.id.imageViewTouch);
        //ivt.canScroll(ImageViewTouch.SCROLL_AXIS_HORIZONTAL);
        //ivt.canScroll(ImageViewTouch.SCROLL_AXIS_VERTICAL);
        ivt.setDisplayType(ImageViewTouchBase.DisplayType.FIT_WIDTH);
        ivt.setDoubleTapEnabled(true);
        ivt.setQuickScaleEnabled(true);
        ivt.setScrollEnabled(true);
        ivt.setScaleEnabled(true);
        Intent i = getIntent();
        String equiPath = null;
        boolean hideOk=false;

        if(i != null && i.getExtras() != null)
            equiPath = i.getExtras().getString(EXTRA_IMAGE_PATH);
        if(equiPath != null)
            ivt.setImageBitmap(rotateImage(BitmapFactory.decodeFile(equiPath), 90));
        else {
            Toast.makeText(this, "no image found", Toast.LENGTH_SHORT).show();
            hideOk = true;
        }
        Button btnOk = (Button)findViewById(R.id.btnOK);
        if(hideOk) btnOk.setVisibility(View.INVISIBLE);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewActivity.this.setResult(RESULT_OK);
                finish();
            }
        });

        Button btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewActivity.this.setResult(RESULT_CANCEL);
                finish();
            }
        });
    }
}
