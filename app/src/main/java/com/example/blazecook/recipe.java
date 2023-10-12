package com.example.blazecook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

public class recipe extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        TextView name = findViewById(R.id.name);
        TextView ingridients = findViewById(R.id.ingredients);
        TextView time = findViewById(R.id.time);
        TextView instructions = findViewById(R.id.instructions);
        ImageView image = findViewById(R.id.image);
        MaterialButton shareButton = findViewById(R.id.share_button);




        int id = getIntent().getIntExtra("id",-1);
        AtomicReference<JSONArray> list = MainActivity.recipeArray;




        try {
            JSONObject recipe = list.get().getJSONObject(id);
            name.setText(recipe.getString("name"));
            time.setText("Time: "+recipe.getString("time")+" minutes");
            ingridients.setText(recipe.getString("ingredients"));
            instructions.setText(recipe.getString("instructions"));
            String imageUrl = recipe.getString("image_url");
            Picasso.get().load(imageUrl).into(image);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    try {
                        intent.putExtra(Intent.EXTRA_SUBJECT, recipe.getString("name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        intent.putExtra(Intent.EXTRA_TEXT, "Here's the recipe for " + recipe.getString("name") + ":\n\n" +
                                "Ingredients:\n" +  recipe.getString("ingredients")+ "\n\n" +
                                "Instructions:\n" + recipe.getString("instructions"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(Intent.createChooser(intent, "Share via"));
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}