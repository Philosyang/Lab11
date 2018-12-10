package edu.illinois.cs.cs125.lab11;

import android.content.Context;
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

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * Main class for our UI design lab.
 */
public final class MainActivity extends AppCompatActivity {
    /** Default logging tag for messages from the main activity. */
    private static final String TAG = "Pokemon-TCG:Main";
    private String lookUpId = "pl4-71";
    private String cardInSet = "Arceus";
    private boolean isSearch = false;
    private int searchDialTotal = 1;
    private JSONArray tempArr;
    private int arrIndex = 0;
    /** specifically set up if search reaches the end / beginning, gives out an alternative Toast. **/
    private int altToast = 0;

    /** Request queue for our API requests. */
    private static RequestQueue requestQueue;
    /** Name variable entered by client **/
    private String readPokemonName;
    private EditText input;
    private Button search;
    /**
     * Run when this activity comes to the foreground.
     *
     * @param savedInstanceState unused
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove actionBar.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // Set up the queue for our API requests
        requestQueue = Volley.newRequestQueue(this);
        // Set up search bar
        input = findViewById(R.id.searchBar);
        search = findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Closes virtual keyboard on button click.
                try {
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    //stub.
                }
                readPokemonName = input.getText().toString();
                Log.d(TAG, "search clicked");
                if (readPokemonName.length() == 0) {
                    // halt user's operation.
                    Toast.makeText(MainActivity.this,
                            "WARNING: Empty input!", Toast.LENGTH_LONG).show();
                } else {
                    // pop up a Toast to provide feedback after pressing the button.
                    Toast.makeText(MainActivity.this,
                            "Fetching data...", Toast.LENGTH_SHORT).show();
                    // start API call.
                    arrIndex = 0;
                    startAPICall("?name=" + readPokemonName);
                }
            }
        });
        // Display card back.
        ImageView temp = findViewById(R.id.pokemonImage);
        Picasso.with(MainActivity.this).
                load("https://crystal-cdn2.crystalcommerce.com/"
                        + "photos/63287/pkm-cardback.png").into(temp);
        // pop up a Toast to show that the app is ready.
        Toast.makeText(MainActivity.this,
                "Ready!", Toast.LENGTH_LONG).show();
        Button button = findViewById(R.id.pressForPokemon);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Log.d(TAG, "button clicked");
                // clears searchDial.
                ((TextView) findViewById(R.id.searchDialCurrent)).setText("N");
                ((TextView) findViewById(R.id.searchDialTotal)).setText("A");
                // pop up a Toast to provide feedback after pressing the button.
                Toast.makeText(MainActivity.this,
                        "Fetching data...", Toast.LENGTH_SHORT).show();
                // random ID generator.
                try {
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            "https://api.pokemontcg.io/v1/sets",
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(final JSONObject response) {
                                    Log.d(TAG, "execute on response");
                                    randomSet(response);
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
                    Log.d(TAG, "error caught upon clicking button");
                    e.printStackTrace();
                }
                // start API call.
                startAPICall("/" + lookUpId);
            }
        });
        Button prev = findViewById(R.id.searchLeft);
        Button next = findViewById(R.id.searchRight);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (arrIndex == 0) {
                        arrIndex = searchDialTotal - 1;
                        altToast = -1;
                    } else {
                        arrIndex -= 1;
                    }
                    isSearch = true;
                    ((TextView) findViewById(R.id.searchDialCurrent)).setText(String.valueOf(arrIndex + 1));
                    apiCallDone(tempArr.getJSONObject(arrIndex));
                } catch (Exception e) {
                    Log.d(TAG, "Previous ERROR: " + e.toString());
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (arrIndex == searchDialTotal - 1) {
                        arrIndex = 0;
                        altToast = 1;
                    } else {
                        arrIndex += 1;
                    }
                    isSearch = true;
                    ((TextView) findViewById(R.id.searchDialCurrent)).setText(String.valueOf(arrIndex + 1));
                    apiCallDone(tempArr.getJSONObject(arrIndex));
                } catch (Exception e) {
                    Log.d(TAG, "Previous ERROR: " + e.toString());
                }
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
                    "https://api.pokemontcg.io/v1/cards" + id,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            try {
                                JSONArray arr = response.getJSONArray("cards");
                                apiCallDoneSearch(response);
                            } catch (Exception e) {
                                apiCallDone(response);
                            }
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
            JSONObject card;
            if (isSearch) {
                card = response;
            } else {
                card = response.getJSONObject("card");
            }
            isSearch = false;
            // Display pokemonName.
            TextView pokemonName = findViewById(R.id.pokemonName);
            pokemonName.setText(card.get("name").toString());
            Log.i(TAG, "pokemonName = " + card.get("name").toString());
            // put pokemonName to searchBar.
            EditText searchBar = findViewById(R.id.searchBar);
            searchBar.setText(card.get("name").toString());
            // Display type.
            ImageView pokemonType = findViewById(R.id.pokemonType);
            try {
                JSONArray typeData = card.getJSONArray("types");
                pokemonType.setImageResource(toConstant(typeData.getString(0)));
                Log.d(TAG, "successfully loaded image at " + "res/drawable/type_"
                        + typeData.getString(0).toLowerCase() + ".png");
            } catch (Exception e) {
                pokemonType.setImageResource(0);
                Log.d(TAG, "Icon error: " + e.toString());
            }
            // Display HP.
            try {
                TextView hP = findViewById(R.id.hP);
                hP.setText("HP");
                TextView hPnumber = findViewById(R.id.hPnumber);
                hPnumber.setText(card.get("hp").toString());
                Log.i(TAG, "HP = " + card.get("hp").toString());
            } catch (Exception e) {
                TextView hP = findViewById(R.id.hP);
                hP.setText("");
                TextView hPnumber = findViewById(R.id.hPnumber);
                hPnumber.setText("");
            }
            // Display set.
            try {
                ImageView cardSetImage = findViewById(R.id.cardSetImage);
                Picasso.with(MainActivity.this).load("https://images.pokemontcg.io/" + card.get("setCode").toString() + "/logo.png").into(cardSetImage);
            } catch (Exception e) {

            }
            // Display weakMultiplier.
            try {
                JSONArray weaknesses = card.getJSONArray("weaknesses");
                TextView weakMultiplier = findViewById(R.id.weakMultiplier);
                ImageView weakType = findViewById(R.id.weaknessType);
                JSONObject tempA = weaknesses.getJSONObject(0);
                weakMultiplier.setText(tempA.get("value").toString());
                weakType.setImageResource(toConstant(tempA.get("type").toString()));
                Log.i(TAG, "weakMultiplier = " + tempA.get("value").toString());
            } catch (Exception e) {
                TextView weakMultiplier = findViewById(R.id.weakMultiplier);
                ImageView weakType = findViewById(R.id.weaknessType);
                weakMultiplier.setText("N/A");
                weakType.setImageResource(0);
            }
            // Display resMultiplier.
            try {
                JSONArray resistances = card.getJSONArray("resistances");
                TextView resMultiplier = findViewById(R.id.resMultiplier);
                ImageView resType = findViewById(R.id.resType);
                JSONObject tempB = resistances.getJSONObject(0);
                resMultiplier.setText(tempB.get("value").toString());
                resType.setImageResource(toConstant(tempB.get("type").toString()));
                Log.i(TAG, "resMultiplier = " + tempB.get("value").toString());
            } catch (Exception e) {
                TextView resMultiplier = findViewById(R.id.resMultiplier);
                ImageView resType = findViewById(R.id.resType);
                resMultiplier.setText("N/A");
                resType.setImageResource(0);
            }
            // Display retreatCost.
            try {
                JSONArray retreatCost = card.getJSONArray("retreatCost");
                TextView retreatMultiplier = findViewById(R.id.retreatMultiplier);
                ImageView retreatType = findViewById(R.id.retreatType);
                int tempC = retreatCost.length();
                retreatMultiplier.setText("×" + tempC); // any workarounds?
                retreatType.setImageResource(toConstant(retreatCost.getString(0)));
                Log.i(TAG, "retreatMultiplier = ×" + tempC);
            } catch (Exception e) {
                TextView retreatMultiplier = findViewById(R.id.retreatMultiplier);
                ImageView retreatType = findViewById(R.id.retreatType);
                retreatMultiplier.setText("N/A");
                retreatType.setImageResource(0);
            }
            // Get Moves.
            JSONArray moves;
            try {
                moves = card.getJSONArray("attacks");
            } catch (Exception e) {
                Log.d(TAG, "Attack error: " + e.toString());
                moves = null;
            }
            // Display Move 1.
            // Icon part.
            try {
                ((ImageView) findViewById(R.id.pokemonMove1Icon1)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(0).getJSONArray("cost").getString(0)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove1Icon1)).setImageResource(0);
            }
            try {
                ((ImageView) findViewById(R.id.pokemonMove1Icon2)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(0).getJSONArray("cost").getString(1)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove1Icon2)).setImageResource(0);
            }
            try {
                ((ImageView) findViewById(R.id.pokemonMove1Icon3)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(0).getJSONArray("cost").getString(2)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove1Icon3)).setImageResource(0);
            }
            try {
                ((ImageView) findViewById(R.id.pokemonMove1Icon4)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(0).getJSONArray("cost").getString(3)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove1Icon4)).setImageResource(0);
            }
            // Text part.
            try {
                TextView move1Name = findViewById(R.id.pokemonMove1Name);
                TextView move1Body = findViewById(R.id.move1Desc);
                TextView move1Damage = findViewById(R.id.pokemonMove1Atk);
                JSONObject move1Data = moves.getJSONObject(0);
                move1Name.setText(move1Data.get("name").toString());
                move1Body.setText(move1Data.get("text").toString());
                move1Damage.setText(move1Data.get("damage").toString());
                Log.i(TAG, "Name of move 1 is " + move1Data.get("name").toString());
            } catch (Exception e) {
                TextView move1Name = findViewById(R.id.pokemonMove1Name);
                TextView move1Body = findViewById(R.id.move1Desc);
                TextView move1Damage = findViewById(R.id.pokemonMove1Atk);
                move1Name.setText("");
                move1Body.setText("");
                move1Damage.setText("");
            }
            // Display Move 2.
            // Icon part.
            try {
                ((ImageView) findViewById(R.id.pokemonMove2Icon1)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(1).getJSONArray("cost").getString(0)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove2Icon1)).setImageResource(0);
            }
            try {
                ((ImageView) findViewById(R.id.pokemonMove2Icon2)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(1).getJSONArray("cost").getString(1)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove2Icon2)).setImageResource(0);
            }
            try {
                ((ImageView) findViewById(R.id.pokemonMove2Icon3)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(1).getJSONArray("cost").getString(2)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove2Icon3)).setImageResource(0);
            }
            try {
                ((ImageView) findViewById(R.id.pokemonMove2Icon4)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(1).getJSONArray("cost").getString(3)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove2Icon4)).setImageResource(0);
            }
            // Text part.
            try {
                TextView move2Name = findViewById(R.id.pokemonMove2Name);
                TextView move2Body = findViewById(R.id.move2Desc);
                TextView move2Damage = findViewById(R.id.pokemonMove2Atk);
                JSONObject move2Data = moves.getJSONObject(1);
                move2Name.setText(move2Data.get("name").toString());
                move2Body.setText(move2Data.get("text").toString());
                move2Damage.setText(move2Data.get("damage").toString());
                Log.i(TAG, "Name of move 2 is " + move2Data.get("name").toString());
            } catch (Exception e) {
                TextView move2Name = findViewById(R.id.pokemonMove2Name);
                TextView move2Body = findViewById(R.id.move2Desc);
                TextView move2Damage = findViewById(R.id.pokemonMove2Atk);
                move2Name.setText("");
                move2Body.setText("");
                move2Damage.setText("");
            }
            // Display Move 3.
            // Icon part.
            try {
                ((ImageView) findViewById(R.id.pokemonMove3Icon1)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(2).getJSONArray("cost").getString(0)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove3Icon1)).setImageResource(0);
            }
            try {
                ((ImageView) findViewById(R.id.pokemonMove3Icon2)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(2).getJSONArray("cost").getString(1)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove3Icon2)).setImageResource(0);
            }
            try {
                ((ImageView) findViewById(R.id.pokemonMove3Icon3)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(2).getJSONArray("cost").getString(2)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove3Icon3)).setImageResource(0);
            }
            try {
                ((ImageView) findViewById(R.id.pokemonMove3Icon4)).setImageResource(toConstant(card.getJSONArray("attacks").getJSONObject(2).getJSONArray("cost").getString(3)));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.pokemonMove3Icon4)).setImageResource(0);
            }
            // Text part.
            try {
                TextView move3Name = findViewById(R.id.pokemonMove3Name);
                TextView move3Body = findViewById(R.id.move3Desc);
                TextView move3Damage = findViewById(R.id.pokemonMove3Atk);
                JSONObject move3Data = moves.getJSONObject(2);
                move3Name.setText(move3Data.get("name").toString());
                move3Body.setText(move3Data.get("text").toString());
                move3Damage.setText(move3Data.get("damage").toString());
                Log.i(TAG, "Name of move 3 is " + move3Data.get("name").toString());
            } catch (Exception e) {
                TextView move3Name = findViewById(R.id.pokemonMove3Name);
                TextView move3Body = findViewById(R.id.move3Desc);
                TextView move3Damage = findViewById(R.id.pokemonMove3Atk);
                move3Name.setText("");
                move3Body.setText("");
                move3Damage.setText("");
            }
            // Display pokemonImage.
            ImageView tempD = findViewById(R.id.pokemonImage);
            Picasso.with(MainActivity.this).load(card.get("imageUrlHiRes").toString()).into(tempD);
            // Display a success or alternative Toast.
            if (altToast == -1) {
                Toast.makeText(MainActivity.this, "Searching from the end.", Toast.LENGTH_SHORT).show();
                altToast = 0;
            } else if (altToast == 1) {
                Toast.makeText(MainActivity.this, "Searching from the beginning.", Toast.LENGTH_SHORT).show();
                altToast = 0;
            } else {
                Toast.makeText(MainActivity.this, "√", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.i(TAG, "caught ERROR! " + e.toString());
            // Display a failing Toast.
            Toast.makeText(MainActivity.this,
                    "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    /**Change string to int constants.
     * @param str the string to be changed.
     * @return the int constant.
     */
    int toConstant(final String str) {
        switch (str) {
            case "Fire":
                return R.drawable.type_fire;
            case "Water":
                return R.drawable.type_water;
            case "Grass":
                return R.drawable.type_grass;
            case "Lightning":
                return R.drawable.type_electric;
            case "Fighting":
                return R.drawable.type_fighting;
            case "Psychic":
                return R.drawable.type_psychic;
            case "Colorless":
                return R.drawable.type_normal;
            case "Metal":
                return R.drawable.type_steel;
            case "Darkness":
                return R.drawable.type_dark;
            case "Dragon":
                return R.drawable.type_dragon;
            case "Fairy":
                return R.drawable.type_fairy;
            default:
                return 0;
        }
    }

    void randomSet(final JSONObject response) {
        try {
            JSONArray sets = response.getJSONArray("sets");
            JSONObject set = sets.getJSONObject((int) (Math.random() * 96 + 1));
            Log.d(TAG, "randomSet.set = " + set.toString());
            cardInSet = set.get("name").toString();
            Log.d(TAG, "variable: cardInSet = " + cardInSet);
            lookUpId = set.get("code").toString() + "-" + (int) (Math.random() * ((int) set.get("totalCards") - 1) + 1);
            Log.d(TAG, "variable: lookUpId = " + lookUpId);
        } catch (Exception e) {
            Log.d(TAG, "Random set error: " + e.toString());
        }
    }
    void apiCallDoneSearch(JSONObject response) {
        try {
            JSONArray arr = response.getJSONArray("cards");
            tempArr = arr;
            searchDialTotal = arr.length();
            ((TextView) findViewById(R.id.searchDialCurrent)).setText(String.valueOf(arrIndex + 1));
            ((TextView) findViewById(R.id.searchDialTotal)).setText(String.valueOf(searchDialTotal));
            JSONObject obj = arr.getJSONObject(0);
            isSearch = true;
            apiCallDone(obj);
        } catch (Exception e) {
            Log.d(TAG, "apiCallDoneSearch error: " + e.toString());
        }
    }
}