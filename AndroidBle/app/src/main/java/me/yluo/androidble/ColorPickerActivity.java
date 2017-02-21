package me.yluo.androidble;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import me.yluo.androidble.color.CircleView;
import me.yluo.androidble.color.ColorPicker;
import me.yluo.androidble.color.RgbBar;

public class ColorPickerActivity extends Activity implements ColorPicker.ColorChangeListener, AdapterView.OnItemClickListener {
    public static final int COLOR_REQUEST = 8793;
    public static final int COLOR_RESULT = 8794;
    public static final String COLOR_KEY = "color_picker_value";

    private int[] colors = new int[]{
            0xd22222, 0xf44836, 0xf2821e, 0x7bb736, 0x16c24b, 0x16a8c2,
            0x2b86e3, 0x3f51b5, 0x5538e9, 0x9c27b0, 0xcc268f, 0x39c5bb
    };
    private String[] names = new String[]{
            "红色", "橘红", "橘黄", "草绿", "翠绿", "青色",
            "天蓝", "蓝色", "青紫", "紫色", "紫红", "初音"
    };

    private EditText colorHex, colorRgb;
    private ColorPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        picker = (ColorPicker) findViewById(R.id.picker);
        RgbBar opacityBar = (RgbBar) findViewById(R.id.opacitybar);
        colorHex = (EditText) findViewById(R.id.et_color_hex);
        colorRgb = (EditText) findViewById(R.id.et_color_rgb);
        picker.addArgbBar(opacityBar);
        GridView gridView = (GridView) findViewById(R.id.commons_colors);
        gridView.setAdapter(new ColorAdapter());
        picker.setColorChangeListener(this);
        gridView.setOnItemClickListener(this);

        getActionBar().setTitle("信仰灯");
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onColorChange(int color) {
        colorHex.setText("#" + Integer.toHexString(color & 0x00ffffff).toUpperCase());
        colorRgb.setText(Color.red(color) + "," + Color.green(color) + "," + Color.blue(color));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        picker.setColor(colors[position]);
    }

    class ColorAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return colors.length;
        }

        @Override
        public Object getItem(int position) {
            return colors[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.item_color, null);
            CircleView circleView = (CircleView) convertView.findViewById(R.id.color);
            circleView.setColor(colors[position]);
            ((TextView) convertView.findViewById(R.id.name)).setText(names[position]);
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.color_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_ok:
                Intent i = new Intent();
                i.putExtra(COLOR_KEY, picker.getColor());
                setResult(COLOR_RESULT, i);
                finish();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
