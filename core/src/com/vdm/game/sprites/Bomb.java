package com.vdm.game.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.vdm.game.ExplosionGame;

public class Bomb {

    private boolean Active;
    private Texture textureBomb;
    private Animation BombAnimation;
    private Vector2 Position;
    private float Timer;
    private float delta_t_for_Timer;

    private Texture textureExplosion;
    private Animation ExplosionAnimation;
    private float TimerExplosion;
    private float delta_t_for_TimerExplosion;
    private boolean ActiveExplosion;
    private Vector2 PositionExplosion;
    private int Strength;
    private int MaxStrength;
    private boolean ArrayOfDamagedRegion[][];

    public Bomb() {

        //Для бомбы
        Active = false;
        Position = new Vector2(); Position.set(0,0);
        textureBomb = new Texture("bomb.png"); //Выбор текстуры бомбы
        BombAnimation = new Animation(new TextureRegion(textureBomb),6,1.0f); //Анимирование текстуры бомбы
        Timer = 0;
        delta_t_for_Timer = 4; //4 секунды проходит до взрыва бомбы

        //Для взрыва
        PositionExplosion = new Vector2(); PositionExplosion.set(0,0);
        Strength = 1;
        MaxStrength = 5;
        ArrayOfDamagedRegion = new boolean[4][MaxStrength];
        for(int i  = 0; i < 4; ++i) { //0 - Up, 1 - Down, 2 - Right, 3 - Left
            for(int j = 0; j < MaxStrength; ++j) {
                ArrayOfDamagedRegion[i][j] = false;
            }
        }
        ActiveExplosion = false;
        textureExplosion = new Texture("texture_explosion.png"); //Выбор текстуры взрыва
        ExplosionAnimation = new Animation(new TextureRegion(textureExplosion),1,1.0f);  //Анимирование текстуры взрыва
        TimerExplosion = 0;
        delta_t_for_TimerExplosion = 0.5f; //0.5 секунды отводится на анимацию взрыва
    }

    //Обновление анимации бомбы
    public void update(float delta_t, Battlefield batfil) {
        if(Active) { //Если в данный момент активна бомба
            BombAnimation.update(delta_t);
            Timer += Gdx.graphics.getDeltaTime();
            if(Timer >= delta_t_for_Timer) {
                Timer = 0;
                Active = false;
                batfil.setEmptyToField((int)Position.x, (int)Position.y);
                //Активация взрыва и заполнение массива взрывов
                PositionExplosion.set((int)Position.x, (int)Position.y);
                ActiveExplosion = true;
                /*for(int i  = 0; i < 4; ++i) {
                    for(int j = 0; j < Strength; ++j) {
                        ArrayOfDamagedRegion[i][j] = true;
                    }
                }*/
                ExplosionGame.explosion_sound.play();
                batfil.setExplosionToField((int)PositionExplosion.x, (int)PositionExplosion.y, Strength, ArrayOfDamagedRegion);
            }
        }
    }

    //Обновление анимации взрыва
    public void updateExplosion(Battlefield batfil) {
        if(ActiveExplosion) { //Если в данный момент активен взрыв
            TimerExplosion += Gdx.graphics.getDeltaTime();
            if(TimerExplosion >= delta_t_for_TimerExplosion) {
                TimerExplosion = 0;
                ActiveExplosion = false;
                //batfil.setExplosionToField((int)PositionExplosion.x, (int)PositionExplosion.y, Strength, ArrayOfDamagedRegion);
                for(int i  = 0; i < 4; ++i) {
                    for(int j = 0; j < Strength; ++j) {
                        ArrayOfDamagedRegion[i][j] = false;
                    }
                }
            }
        }
    }

    //Получения кадра анимации бомбы
    public TextureRegion getBomb() {
        return BombAnimation.getFrame();
    }

    //Установить позицию бомбы на боевом поле
    public void setPosition(int i, int j) {
        Position.set(i, j);
    }

    //Получить позицию бомбы на боевом поле
    public Vector2 getPosition() {
        return Position;
    }

    //Установить активность бомбы
    public void setActive(boolean Active) {
        this.Active = Active;
    }

    //Узнать, активирована ли бомба
    public boolean getActive() {
        return Active;
    }

    //Получения кадра анимации взрыва
    public TextureRegion getExplosion() {
        return ExplosionAnimation.getFrame();
    }

    //Установить активность взрыва
    public void setActiveExplosion(boolean ActiveExplosion) {
        this.ActiveExplosion = ActiveExplosion;
    }

    //Узнать, активирован ли взрыв
    public boolean getActiveExplosion() {
        return ActiveExplosion;
    }

    //Получить показатель силы взрыва
    public int getStrength() {
        return Strength;
    }

    //Установить показатель силы взрыва
    public void setStrength(int Strength) {
        this.Strength = (Strength < 1) ? 1 : ((Strength > MaxStrength) ? MaxStrength : Strength);
    }

    //Получить значение ячейки массива взрывов
    public boolean getArrayOfDamagedRegion_ij(int i, int j) {
        return ArrayOfDamagedRegion[i][j];
    }

    //Получить позицию начального взрыва на боевом поле
    public Vector2 getPositionExplosion() {
        return PositionExplosion;
    }

    //Освобождение ресурсов
    public void dispose() {
        textureBomb.dispose();
        textureExplosion.dispose();
    }
}
