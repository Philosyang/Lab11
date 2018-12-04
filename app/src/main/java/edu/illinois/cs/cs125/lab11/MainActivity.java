package edu.illinois.cs.cs125.lab11;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

/**
 * Main class for our UI design lab.
 */
public final class MainActivity extends AppCompatActivity {
    /** Default logging tag for messages from the main activity. */
    private static final String TAG = "Pokemon-TCG:Main";

    /** Request queue for our API requests. */
    private static RequestQueue requestQueue;

    /**
     * Run when this activity comes to the foreground.
     *
     * @param savedInstanceState unused
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the queue for our API requests
        requestQueue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_main);
        // Display card back.
        ImageView temp = findViewById(R.id.pokemonImage);
        Picasso.with(MainActivity.this).
                load("https://crystal-cdn2.crystalcommerce.com/"
                        + "photos/63287/pkm-cardback.png").into(temp);
        // pop up a Toast to show that the app is ready.
        Toast.makeText(MainActivity.this,
                "Click on the button to get started.", Toast.LENGTH_LONG).show();
        Button button = findViewById(R.id.pressForPokemon);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Log.d(TAG, "button clicked");
                // pop up a Toast to provide feedback after pressing the button.
                Toast.makeText(MainActivity.this,
                        "Fetching data...", Toast.LENGTH_SHORT).show();
                // random ID generator.
                String id = "sm75-" + (int) (Math.random() * 70);
                Log.d(TAG, id);
                // start API call.
                startAPICall(id);
            }
        });
    }

    /**
     * Run when this activity is no longer visible.
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Make a call to the PTCG API.
     *
     * @param id one Pokemon's ID.
     */
    void startAPICall(final String id) {
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    "https://api.pokemontcg.io/v1/cards/" + id,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            apiCallDone(response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(final VolleyError error) {
                            Log.e(TAG, error.toString());
                        }
                    });
            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle the response from PTCG API.
     *
     * @param response response from PTCG API.
     */
    void apiCallDone(final JSONObject response) {
        try {
            Log.d(TAG, response.toString(2));
            // Create JSONObject card.
            JSONObject card = response.getJSONObject("card");
            // Display pokemonName.
            TextView pokemonName = findViewById(R.id.pokemonName);
            pokemonName.setText(card.get("name").toString());
            Log.i(TAG, "pokemonName = " + card.get("name").toString());
            // Display weakMultiplier.
            try {
                JSONArray weaknesses = card.getJSONArray("weaknesses");
                TextView weakMultiplier = findViewById(R.id.weakMultiplier);
                JSONObject tempA = weaknesses.getJSONObject(0);
                weakMultiplier.setText(tempA.get("value").toString());
                Log.i(TAG, "weakMultiplier = " + tempA.get("value").toString());
            } catch (Exception e) {
                TextView weakMultiplier = findViewById(R.id.weakMultiplier);
                weakMultiplier.setText("N/A");
            }
            // Display resMultiplier.
            try {
                JSONArray resistances = card.getJSONArray("resistances");
                TextView resMultiplier = findViewById(R.id.resMultiplier);
                JSONObject tempB = resistances.getJSONObject(0);
                resMultiplier.setText(tempB.get("value").toString());
                Log.i(TAG, "resMultiplier = " + tempB.get("value").toString());
            } catch (Exception e) {
                TextView resMultiplier = findViewById(R.id.resMultiplier);
                resMultiplier.setText("N/A");
            }
            // Display retreatCost.
            try {
                JSONArray retreatCost = card.getJSONArray("retreatCost");
                TextView retreatMultiplier = findViewById(R.id.retreatMultiplier);
                int tempC = retreatCost.length();
                retreatMultiplier.setText("×" + tempC); // any workarounds?
                Log.i(TAG, "retreatMultiplier = ×" + tempC);
            } catch (Exception e) {
                TextView retreatMultiplier = findViewById(R.id.retreatMultiplier);
                retreatMultiplier.setText("N/A");
            }
            // Display pokemonImage.
            ImageView tempD = findViewById(R.id.pokemonImage);
            Picasso.with(MainActivity.this).load(card.get("imageUrlHiRes").toString()).into(tempD);
            // Display a success Toast.
            Toast.makeText(MainActivity.this,
                    "Fetch successful.", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.i(TAG, "caught ERROR! " + e.toString());
            // Display a failing Toast.
            Toast.makeText(MainActivity.this,
                    "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }
}
