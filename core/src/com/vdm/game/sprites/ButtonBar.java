package com.vdm.game.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.vdm.game.ExplosionGame;

public class ButtonBar {

    private Texture texture_btn_bomb;
    private Texture texture_btn_up;
    private Texture texture_btn_down;
    private Texture texture_btn_right;
    private Texture texture_btn_left;
    private Animation animation_btn_bomb;
    private Animation animation_btn_up;
    private Animation animation_btn_down;
    private Animation animation_btn_right;
    private Animation animation_btn_left;

    private Texture texture_heart;
    private Animation animation_heart;

    private Texture texture_music;
    private Animation animation_music;

    public ButtonBar(Character character){

        this.texture_btn_bomb = new Texture("btn_bomb.png");
        this.animation_btn_bomb = new Animation(new TextureRegion(texture_btn_bomb), 2, 1.0f);

        this.texture_btn_up = new Texture("btn_up.png");
        this.animation_btn_up = new Animation(new TextureRegion(texture_btn_up), 2, 1.0f);

        this.texture_btn_down = new Texture("btn_down.png");
        this.animation_btn_down = new Animation(new TextureRegion(texture_btn_down), 2, 1.0f);

        this.texture_btn_right = new Texture("btn_right.png");
        this.animation_btn_right = new Animation(new TextureRegion(texture_btn_right), 2, 1.0f);

        this.texture_btn_left = new Texture("btn_left.png");
        this.animation_btn_left = new Animation(new TextureRegion(texture_btn_left), 2, 1.0f);

        this.texture_heart = new Texture("heart.png");
        this.animation_heart = new Animation(new TextureRegion(texture_heart), 2, 1.0f);

        this.texture_music = new Texture("music.png");
        this.animation_music = new Animation(new TextureRegion(texture_music), 2, 1.0f);

    }

    //Получение элементов панели управления для дальнейшей отрисовки в классе GW_Play
    public TextureRegion get_btn_bomb() { return animation_btn_bomb.getFrame(); }
    public TextureRegion get_btn_up() { return  animation_btn_up.getFrame(); }
    public TextureRegion get_btn_down() { return  animation_btn_down.getFrame(); }
    public TextureRegion get_btn_right() { return  animation_btn_right.getFrame(); }
    public TextureRegion get_btn_left() { return  animation_btn_left.getFrame(); }
    public TextureRegion get_heart() { return  animation_heart.getFrame(); }
    public TextureRegion get_music() { return animation_music.getFrame(); }

    //Получение размеров кнопок
    public float getWidth_btn_bomd() { return texture_btn_bomb.getWidth() / 2; }
    public float getHeight_btn_bomd() { return texture_btn_bomb.getHeight(); }
    public float getWidth_btn_UDRL() { return texture_btn_up.getWidth() / 2; }
    public float getHeight_btn_UDRL() { return texture_btn_up.getHeight(); }
    public float getWidth_heart() { return texture_heart.getWidth() / 2; }
    public float getHeight_heart() { return texture_heart.getHeight(); }
    public float getWidth_music() {return texture_music.getWidth() / 2; }
    public float getHeight_music() {return  texture_music.getHeight(); }

    //Проверка, нажата ли кнопка
    public boolean isButtonPressed(char c, boolean bKey) {
        switch(c) {
            case 'B':
                if(bKey ||
                        ExplosionGame.CursorCoordinates.x >= ExplosionGame.WIDTH - getWidth_btn_bomd() &&
                                ExplosionGame.CursorCoordinates.x <= ExplosionGame.WIDTH &&
                                ExplosionGame.CursorCoordinates.y >= (ExplosionGame.HEIGHT - getHeight_btn_bomd()) / 2 &&
                                ExplosionGame.CursorCoordinates.y <= (ExplosionGame.HEIGHT + getHeight_btn_bomd()) / 2) {
                    animation_btn_bomb.updateButtonBar(true, 0.5f);
                    return true;
                } else {
                    animation_btn_bomb.updateButtonBar(false, 0.05f);
                }
                break;
            case 'U':
                if(bKey ||
                        ExplosionGame.CursorCoordinates.x >= getWidth_btn_UDRL() * 1 &&
                                ExplosionGame.CursorCoordinates.x < getWidth_btn_UDRL() * 2 &&
                                ExplosionGame.CursorCoordinates.y >= (ExplosionGame.HEIGHT + getHeight_btn_UDRL()) / 2 &&
                                ExplosionGame.CursorCoordinates.y < (ExplosionGame.HEIGHT + 3 * getHeight_btn_UDRL()) / 2) {
                    animation_btn_up.updateButtonBar(true, 0.5f);
                    return true;
                } else {
                    animation_btn_up.updateButtonBar(false, 0.05f);
                }
                break;
            case 'D':
                if(bKey ||
                        ExplosionGame.CursorCoordinates.x >= getWidth_btn_UDRL() * 1 &&
                                ExplosionGame.CursorCoordinates.x < getWidth_btn_UDRL() * 2 &&
                                ExplosionGame.CursorCoordinates.y >= (ExplosionGame.HEIGHT - 3 * getHeight_btn_UDRL()) / 2 &&
                                ExplosionGame.CursorCoordinates.y < (ExplosionGame.HEIGHT - getHeight_btn_UDRL()) / 2) {
                    animation_btn_down.updateButtonBar(true, 0.5f);
                    return true;
                } else {
                    animation_btn_down.updateButtonBar(false, 0.05f);
                }
                break;
            case 'R':
                if(bKey ||
                        ExplosionGame.CursorCoordinates.x >= getWidth_btn_UDRL() * 2 &&
                                ExplosionGame.CursorCoordinates.x < getWidth_btn_UDRL() * 3 &&
                                ExplosionGame.CursorCoordinates.y >= (ExplosionGame.HEIGHT - getHeight_btn_UDRL()) / 2 &&
                                ExplosionGame.CursorCoordinates.y < (ExplosionGame.HEIGHT + getHeight_btn_UDRL()) / 2) {
                    animation_btn_right.updateButtonBar(true, 0.5f);
                    return true;
                } else {
                    animation_btn_right.updateButtonBar(false, 0.05f);
                }
                break;
            case 'L':
                if(bKey ||
                        ExplosionGame.CursorCoordinates.x >= 0 &&
                                ExplosionGame.CursorCoordinates.x < getWidth_btn_UDRL() &&
                                ExplosionGame.CursorCoordinates.y >= (ExplosionGame.HEIGHT - getHeight_btn_UDRL()) / 2 &&
                                ExplosionGame.CursorCoordinates.y < (ExplosionGame.HEIGHT + getHeight_btn_UDRL()) / 2) {
                    animation_btn_left.updateButtonBar(true, 0.5f);
                    return true;
                } else {
                    animation_btn_left.updateButtonBar(false, 0.05f);
                }
                break;
            case 'M':
                if(bKey ||
                        ExplosionGame.CursorCoordinates.x >= 0 &&
                                ExplosionGame.CursorCoordinates.x <= getWidth_music() &&
                                ExplosionGame.CursorCoordinates.y >= 0 &&
                                ExplosionGame.CursorCoordinates.y <= getHeight_music()) {
                    animation_music.updateButtonMusic();
                    return true;
                }
                break;
        }
        return false;
    }

    public void updateHeart(int frame_Number){
        animation_heart.updateCharacteristics(frame_Number);
    }

    public void dispose() {
        texture_btn_bomb.dispose();
        texture_btn_up.dispose();
        texture_btn_down.dispose();
        texture_btn_right.dispose();
        texture_btn_left.dispose();
        texture_heart.dispose();
        texture_music.dispose();
    }

}