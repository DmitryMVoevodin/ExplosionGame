package com.vdm.game.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.vdm.game.ExplosionGame;

public class GW_GameOver extends GameWindow {

    private Texture texture_lose_win;
    private Music music_lose_win;

    public GW_GameOver(ManagementSystemOfGameWindows msgw) {
        super(msgw);
        camera.setToOrtho(false, ExplosionGame.WIDTH, ExplosionGame.HEIGHT);
        if(ExplosionGame.Flag_isKilled.getValue()) {
            texture_lose_win = new Texture("lose.png");
            music_lose_win = Gdx.audio.newMusic(Gdx.files.internal("lose.mp3"));
        } else {
            texture_lose_win = new Texture("win.png");
            music_lose_win = Gdx.audio.newMusic(Gdx.files.internal("win.mp3"));
        }
        music_lose_win.setLooping(true);
        music_lose_win.setVolume(1.0f);
        music_lose_win.play();
    }

    @Override
    protected void handleInput() {
        if(Gdx.input.justTouched()) {
            //Разбираемся с вопросом сети
            ExplosionGame.client.KillExistence();
            ExplosionGame.client.dispose();
            if(ExplosionGame.isServer) {
                ExplosionGame.server.KillExistence();
                ExplosionGame.server.dispose();
            }
            //Разбираемся с вопросом всей игры
            ExplosionGame.Flag_End.setValue(true);
            //Разбираемся с вопросом текстур данного игрового окна
            texture_lose_win.dispose();
            music_lose_win.dispose();
            //Закрываем всю игру
            Gdx.app.exit();
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        if(!ExplosionGame.Flag_End.getValue()) {
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            spriteBatch.draw(texture_lose_win, 0, 0);
            spriteBatch.end();
        }
    }

    @Override
    public void dispose() {
        texture_lose_win.dispose();
        music_lose_win.dispose();
    }

}
