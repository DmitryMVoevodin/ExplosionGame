package com.vdm.game.windows;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Stack;

//Класс системы управления игровыми окнами, которая представляет собой "стек"
public class ManagementSystemOfGameWindows {

    private Stack<GameWindow> StackOfGameWindows; //Переменная стека игровых окон

    //Конструктор класса, создающий стек
    public ManagementSystemOfGameWindows() {
        StackOfGameWindows = new Stack<GameWindow>();
    }

    //Положить новый элемент, новое игровое окно, в стек
    public void push(GameWindow gameWindow) {
        StackOfGameWindows.push(gameWindow);
    }

    //Убрать верхний элемент стека с освобождением ресурсов
    //посредством метода dispose() из класса GameWindow
    public void pop() {
        StackOfGameWindows.pop().dispose();
    }

    //Изменить верхний элемент посредством удаления его из стека
    //с освобождением ресурсов и добавлением нового игрового окна в стек
    public void set(GameWindow gameWindow) {
        StackOfGameWindows.pop().dispose();
        StackOfGameWindows.push(gameWindow);
    }

    //Обновление верхнего элемента стека
    public void update(float delta_t) {
        StackOfGameWindows.peek().update(delta_t);
    }

    //Отрисовка игрового окна, представляющего верхний
    //элемент стека
    public void render(SpriteBatch spriteBatch) {
        StackOfGameWindows.peek().render(spriteBatch);
    }

}
