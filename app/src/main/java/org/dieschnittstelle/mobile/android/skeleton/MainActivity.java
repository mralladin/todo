package org.dieschnittstelle.mobile.android.skeleton;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ViewGroup listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        listView=findViewById(R.id.listview);
        Arrays.asList("Test","test2","test3").forEach(item -> {
            TextView currentItemView = (TextView) getLayoutInflater().inflate(R.layout.activity_overview_simple_list_item_view, null);
            currentItemView.setText(item);
            listView.addView(currentItemView);
            currentItemView.setOnClickListener(view -> showDetailviewForItem(view.toString()));
        });

    }

        protected void showDetailviewForItem(String item){

            Intent callDetailviewIntent = new Intent(this, DetailviewActivity.class);
            callDetailviewIntent.putExtra("item",item);
            startActivity(callDetailviewIntent);
        }

   private void showMessage(String message){
         Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
