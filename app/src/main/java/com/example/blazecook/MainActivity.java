package com.example.blazecook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    public static AtomicReference<JSONArray> recipeArray = new AtomicReference<>(new JSONArray());
    private AutoCompleteTextView input;
    private ArrayList<String> suggestionsList;
    private String searchHistory;
    private LinearLayout layoutOvalList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        searchHistory = "";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button clear = findViewById(R.id.clearBtn);
        Button find = findViewById(R.id.findBtn);
        input = findViewById(R.id.input);
        ListView listView = findViewById(R.id.list);
        ToggleButton containAllToggle = findViewById(R.id.containAllToggle);
        loadSuggestions();

        layoutOvalList = findViewById(R.id.layoutOvalList);
        clear.setOnClickListener(view -> {
            if(!searchHistory.equals("")) {
                searchHistory = "";
                layoutOvalList.removeAllViews();
            }
        });

        ArrayAdapter<String> adapterSuggestions = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, suggestionsList);


        input.setAdapter(adapterSuggestions);


        input.setThreshold(1);


//        input.setOnItemClickListener((parent, view, position, id) -> {
//            String selectedSuggestion = (String) parent.getItemAtPosition(position);
//            input.setText(selectedSuggestion);
//        });
        input.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSuggestion = (String) parent.getItemAtPosition(position);
            searchHistory = searchHistory +selectedSuggestion.trim() +",";
            input.setText("");

            TextView ovalTextView = new TextView(MainActivity.this);
            ovalTextView.setBackground(getResources().getDrawable(R.drawable.oval_shape));
            ovalTextView.setText(selectedSuggestion);
            ovalTextView.setPadding(20, 20, 20, 20);


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 0, 0);
            layoutOvalList.addView(ovalTextView, params);
        });



        find.setOnClickListener(view -> {

            String ingredients = searchHistory.toString().toLowerCase();
            Toast.makeText(this, "Finding "+searchHistory.toLowerCase(), Toast.LENGTH_SHORT).show();
            JSONArray matchedRecipes = new JSONArray();
            try {
                //JSONArray recipeList = new JSONArray(new JSONTokener(new FileReader("res/raw/output.json")));
                //JSONArray recipeList = new JSONArray(new JSONTokener("res/raw/output.json"));

                JSONArray recipeList = new JSONArray();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.output)));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    recipeList = new JSONArray(sb.toString());
                    // Do something with the recipeList JSONArray object
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

                //recipeArray.set(recipeList);
                matchedRecipes = new JSONArray();

                for (int i = 0; i < recipeList.length(); i++) {
                    JSONObject recipe = recipeList.getJSONObject(i);
                    String recipeIngredients = recipe.getString("raw_ingredients").toLowerCase();

                    boolean matchAll = containAllToggle.isChecked();
                    if (matchAll) {
                        if (containsAllIngredientsV2(ingredients, recipeIngredients)) {
                            System.out.println("Found!");
                            matchedRecipes.put(recipe);
                        }
                    } else {
                        if (containsAllIngredientsV1(ingredients, recipeIngredients)) {
                            System.out.println("Found!");
                            matchedRecipes.put(recipe);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            recipeArray.set(matchedRecipes);
            ArrayList<String> imagesCon = new ArrayList<>();
            ArrayList<String> dishNameCon = new ArrayList<>();
            ArrayList<String> dishTimeCon = new ArrayList<>();




            for (int i = 0; i <matchedRecipes.length(); i++) {
                JSONObject recipe = new JSONObject();
                String recipeName = "";
                String recipeTime = "";
                String recipeImage = "";
                try {
                    recipe = matchedRecipes.getJSONObject(i);
                    recipeName = recipe.getString("name");
                    recipeTime = recipe.getString("time");
                    recipeImage = recipe.getString("image_url");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                imagesCon.add(recipeImage);
                dishNameCon.add(recipeName);
                dishTimeCon.add(recipeTime);

            }

            customAdapter adapter = new customAdapter(this,imagesCon,dishNameCon,dishTimeCon);
            listView.setAdapter(adapter);

        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
//            try {
//                JSONObject object = recipeArray.get().getJSONObject(i);
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
            Intent intent = new Intent(this,recipe.class);
            intent.putExtra("id",i);
            startActivity(intent);

        });


    }

    private void loadSuggestions() {
        suggestionsList = new ArrayList<>();
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.suggestions);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                suggestionsList.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static boolean containsAllIngredientsV1(String search, String ingredients) {
        String[] searchList = search.split(",");
        String[] ingredientList = ingredients.split(",");
        for (String s : searchList) {
            boolean found = false;
            for (String i : ingredientList) {
                String[] words = i.toLowerCase().split(" ");
                for (String w : words) {
                    if (w.contains(s.toLowerCase().trim())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsAllIngredientsV2(String search, String ingredients) {
        String[] searchList = search.split(",");
        String[] ingredientList = ingredients.split(",");
        int foundIngredientsCount = 0;
        for (String s : searchList) {
            boolean found = false;
            String searchWord = s.toLowerCase().trim().split(" ")[0];
            for (String i : ingredientList) {
                String ingredientWord = i.toLowerCase().trim().split(" ")[0];
                if (ingredientWord.equals(searchWord)) {
                    found = true;
                    foundIngredientsCount++;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return foundIngredientsCount >= ingredientList.length * 0.6;
    }


}

class customAdapter extends ArrayAdapter<String>{
     Context context;
     ArrayList<String> images,name,time;

    public customAdapter(@NonNull Context context, ArrayList<String> images, ArrayList<String> name, ArrayList<String> time) {
        super(context, R.layout.listitem,R.id.dish_name,name);
        this.context = context;
        this.images = images;
        this.name = name;
        this.time = time;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.listitem,parent,false);
        TextView nameText = view.findViewById(R.id.dish_name);
        nameText.setText(name.get(position));
        TextView timeText = view.findViewById(R.id.dish_time);
        timeText.setText("Time to Prepare: "+time.get(position));
        ImageView imageData = view.findViewById(R.id.dish_image);
        Picasso.get().load(images.get(position)).into(imageData);
        return view;
    }
}