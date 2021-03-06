package com.ferrariapps.youtubeapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.ferrariapps.youtubeapp.R;
import com.ferrariapps.youtubeapp.adapter.AdapterVideo;
import com.ferrariapps.youtubeapp.api.YouTubeService;
import com.ferrariapps.youtubeapp.helper.RetrofitConfig;
import com.ferrariapps.youtubeapp.helper.YouTubeConfig;
import com.ferrariapps.youtubeapp.listener.RecyclerItemClickListener;
import com.ferrariapps.youtubeapp.model.Item;
import com.ferrariapps.youtubeapp.model.Resultado;
import com.ferrariapps.youtubeapp.model.Video;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private YouTubePlayer.PlaybackEventListener playbackEventListener;
    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener;
    private RecyclerView recyclerVideos;
    private List<Item> videos = new ArrayList<>();
    private Resultado resultado;
    private AdapterVideo adapterVideo;
    private MaterialSearchView searchView;
    private Retrofit retrofit;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("YouTube");
        setSupportActionBar(toolbar);

        searchView = findViewById(R.id.searchView);
        recyclerVideos = findViewById(R.id.recyclerVideos);
        retrofit = RetrofitConfig.getRetrofit();

        recuperarVideos("");

        searchView.setHint("Pesquisar");
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                recuperarVideos(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                recuperarVideos("");
            }
        });

    }

    private void recuperarVideos(String pesquisa){

        String q = pesquisa.replaceAll(" ","+");
        YouTubeService youTubeService = retrofit.create(YouTubeService.class);
        youTubeService.recuperarVideos(
                "snippet","relevance","100",
                YouTubeConfig.CHAVE_YOUTUBE_API, q
        ).enqueue(new Callback<Resultado>() {
            @Override
            public void onResponse(Call<Resultado> call, Response<Resultado> response) {
                if (response.isSuccessful()){
                    resultado = response.body();
                    videos = resultado.items;
                    configurarRecyclerView();
                }
            }

            @Override
            public void onFailure(Call<Resultado> call, Throwable t) {

            }
        });

    }

    public void configurarRecyclerView(){

        adapterVideo = new AdapterVideo(videos,this);
        recyclerVideos.setHasFixedSize(true);
        recyclerVideos.setLayoutManager(new LinearLayoutManager(this));
        recyclerVideos.setAdapter(adapterVideo);
        recyclerVideos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerVideos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Item video = videos.get(position);
                                String idVideo = video.id.videoId;
                                Intent intent = new Intent(MainActivity.this,PlayerActivity.class);
                                intent.putExtra("idVideo",idVideo);
                                startActivity(intent);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        searchView.setMenuItem(item);
        return true;
    }
}