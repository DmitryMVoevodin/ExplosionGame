package com.vdm.game.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.vdm.game.ExplosionGame;
import com.vdm.game.interconnection.Multiplayer;

public class GW_MainMenu extends GameWindow {

    private Texture texture_wait;
    private Music music_wait;

    private byte bt;
    private boolean b_flag;
    byte[] b_out;

    public GW_MainMenu(ManagementSystemOfGameWindows msgw) {
        super(msgw);
        if(ExplosionGame.isServer) {
            camera.setToOrtho(false, ExplosionGame.WIDTH, ExplosionGame.HEIGHT);
            texture_wait = new Texture("wait.png");
            music_wait = Gdx.audio.newMusic(Gdx.files.internal("wait.mp3"));
            music_wait.setLooping(true);
            music_wait.setVolume(1.0f);
            music_wait.play();
        }
        b_flag = false;
        b_out = new byte[1];
    }

    @Override
    protected void handleInput() {

    }

    @Override
    public void update(float dt) {
        if(ExplosionGame.Flag_MainMenu_To_Play.getValue()) { //Переключение экрана GW_MainMenu на GW_Play
            if(!b_flag) {
                b_out[0] = Multiplayer.ByteFormation(ExplosionGame.PlayerId, ExplosionGame.TypeOfTexture + 6);
                ExplosionGame.client.SendMessage(b_out);
                b_flag = true;
            }
            if(ExplosionGame.Flag_isReadyToMoveForward.TriggerStep(ExplosionGame.NoP)) {
                msgw.set(new GW_Play(msgw));
            }
        }
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        if(ExplosionGame.isServer) {
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            spriteBatch.draw(texture_wait, 0, 0);
            spriteBatch.end();
        }
    }

    @Override
    public void dispose() {
        if(ExplosionGame.isServer) {
            texture_wait.dispose();
            music_wait.dispose();
        }
    }

}
