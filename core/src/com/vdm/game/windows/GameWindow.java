package com.vdm.game.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.vdm.game.ExplosionGame;

//Абстрактный класс игрового окна
public abstract class GameWindow {

    protected OrthographicCamera camera; //Для ортографической камеры
    protected Vector3 mouse; //Для координат нажатия мыши
    protected ManagementSystemOfGameWindows msgw; //Ссылка на экземпляр класса-стека с игровыми окнами

    //Конструктор класса для начальных настройек
    public GameWindow(ManagementSystemOfGameWindows msgw) {
        this.msgw = msgw;
        camera = new OrthographicCamera(); //Создаем камеру и убеждаемся, что камера всегда будет показывать область мира (width x height)
        camera.setToOrtho(false, ExplosionGame.WIDTH, ExplosionGame.HEIGHT);
        mouse = new Vector3();
    }

    protected abstract void handleInput(); //Абстрактный метод по нажатию мыши
    public abstract void update(float delta_t); //Абстрактный метод обновления игрового окна
    public abstract void render(SpriteBatch spriteBatch); //Абстрактный метод отрисовки игрового окна
    public abstract void dispose(); //Абстрактный метод освобождения ресурсов

    //Метод изменения координат мыши при нажатии
    protected void touchedCursor() {
        if(Gdx.input.isTouched() || Gdx.input.justTouched()) { //Если есть прикосновение к экрану/нажата мышь
            mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0); //Формирование координат мыши
        } else {
            mouse.set(0, 0, 0);
        }
        camera.unproject(mouse); //Преобразование координат мыши в систему координат камеры
        ExplosionGame.CursorCoordinates.set(mouse.x, mouse.y); //Установить такие же координаты у "курсора" в классе ExplosionGame
    }

}
