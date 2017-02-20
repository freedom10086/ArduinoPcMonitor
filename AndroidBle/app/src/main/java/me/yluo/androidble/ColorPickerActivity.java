package me.yluo.androidble;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import me.yluo.androidble.color.ColorPicker;
import me.yluo.androidble.color.ArgbBar;

public class ColorPickerActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        ColorPicker picker = (ColorPicker) findViewById(R.id.picker);
        ArgbBar opacityBar = (ArgbBar) findViewById(R.id.opacitybar);
        picker.addArgbBar(opacityBar);

        GridView gridView = (GridView) findViewById(R.id.commons_colors);
        //Context context, List<? extends Map<String, ?>> data,
        //@LayoutRes int resource, String[] from, @IdRes int[] to
        //gridView.setAdapter(new SimpleAdapter());


    }




}
