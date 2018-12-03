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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.TextView;

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

        startAPICall("sm75-57");
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
            // Create JSONArrays.
            JSONArray weaknesses = card.getJSONArray("weaknesses");
            JSONArray resistances = card.getJSONArray("resistances");
            // Display pokemonName.
            TextView pokemonName = findViewById(R.id.pokemonName);
            pokemonName.setText(card.get("name").toString());
            Log.i(TAG, "pokemonName = " + card.get("name").toString());
            // Display weakMultiplier.
            TextView weakMultiplier = findViewById(R.id.weakMultiplier);
            JSONObject tempA = weaknesses.getJSONObject(0);
            weakMultiplier.setText(tempA.get("value").toString());
            Log.i(TAG, "weakMultiplier = " + tempA.get("value").toString());
            // Display resMultiplier.
            TextView resMultiplier = findViewById(R.id.resMultiplier);
            JSONObject tempB = resistances.getJSONObject(0);
            resMultiplier.setText(tempB.get("value").toString());
            Log.i(TAG, "resMultiplier = " + tempB.get("value").toString());
        } catch (JSONException ignored) { }
    }
}
