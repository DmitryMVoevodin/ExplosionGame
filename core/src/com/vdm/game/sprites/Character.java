package com.vdm.game.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.vdm.game.ExplosionGame;
import com.vdm.game.interconnection.Multiplayer;

import java.util.Stack;

public class Character {

    private boolean Alive;
    private int PlayerId;
    private Texture texture;
    private Animation CharacterAnimation;
    private Vector2 Position;
    private Vector2 floatPosition;
    private int Velocity;
    private static int WIDTH, HEIGHT;
    private int shift_x, shift_y;

    private int life; //Количество жизней на данный момент
    private int lifeMax; //Максимальное количество жизней
    private int health; //Количество здоровых жизней на данный момент

    private Bomb[] BombReserve; //Массив запаса бомб у игрока
    private int reserve; //Переменная максимального количества бомб у игрока на данный момент
    private int reserveMax; //Переменная максимально допустимого количества бомб в игре для одного игрока

    private boolean ifSendFinalMessage = false;

    //Конструктор установки основных конфигураций персонажа
    public Character(int TypeOfCharacter, int PlayerId) {
        this.Alive = true; //Установка того факта, что персонаж жив
        this.texture = new Texture("character" + TypeOfCharacter + ".png"); //Выбор текстуры героя
        this.CharacterAnimation = new Animation(new TextureRegion(this.texture), 4,3,0.5f); //Анимирование текстуры героя
        this.PlayerId = PlayerId;
        this.WIDTH = (int)getSizesOfTexture().x;
        this.HEIGHT = (int)getSizesOfTexture().y;
        PositionAndShiftsInit(this.PlayerId);
        this.Velocity = 16; //Установка первоначальной скорости игрока
        this.reserveMax = 10; //Mаксимально допустимое количество бомб за всю игру у одного игрока равно 10
        this.BombReserve = new Bomb[10]; //Создаем обойму
        this.reserve = 3; //Начальный запас бомб у игрока равен 3
        for(int i = 0; i < reserveMax; ++i) { //Заполняем массив бомбами
            this.BombReserve[i] = new Bomb();
        }
        this.life = 3;
        this.lifeMax = 6;
        this.health = this.life;
    }

    //Установка первоначальных координат персонажа на игровом поле и сдвигов
    private void PositionAndShiftsInit(int playerId) {
        int x;
        int y;
        switch (playerId) {
            case 1: //Установка "целочисленных" первоначальных координат и сдвигов для первого игрока
                x = 1;
                y = Battlefield.getCountOfRows() - 2;
                this.shift_x = 0;
                this.shift_y = 0;
                break;
            case 2: //Установка "целочисленных" первоначальных координат и сдвигов для второго игрока
                x = Battlefield.getCountOfColumns() - 2;
                y = 1;
                this.shift_x = (Battlefield.getCountOfColumns() - 3) * WIDTH;
                this.shift_y = (Battlefield.getCountOfRows() - 3) * HEIGHT;
                break;
            case 3: //Установка "целочисленных" первоначальных координат и сдвигов для третьего игрока
                x = 1;
                y = 1;
                this.shift_x = 0;
                this.shift_y = (Battlefield.getCountOfRows() - 3) * HEIGHT;
                break;
            case 4: //Установка "целочисленных" первоначальных координат и сдвигов для четвертого игрока
                x = Battlefield.getCountOfColumns() - 2;
                y = Battlefield.getCountOfRows() - 2;
                this.shift_x = (Battlefield.getCountOfColumns() - 3) * WIDTH;
                this.shift_y = 0;
                break;
            default: //Установка "целочисленных" первоначальных координат и сдвигов для игрока "без номера" - там же, где и первый игрок
                x = 1;
                y = Battlefield.getCountOfRows() - 2;
                this.shift_x = 0;
                this.shift_y = 0;
                break;
        }
        this.Position = new Vector2(x, y); //Задание "дискретной" начальной позиции игрока
        this.floatPosition = new Vector2(this.Position); //Установка "дробных" первоначальных координат героя
    }

    //Обновление жизни и других параметров персонажа с учетом ситуации на боевом поле
    public void update(Battlefield batfil) {
        //Обновление ранений и гибели игрока
        setHealth(ExplosionGame.MassivOfHealth[PlayerId - 1]); //Устанавливаем здоровье
        if(ExplosionGame.MassivOfHealth[PlayerId - 1] == 0) { //Если здоровье опустилось до нуля, тогда...
            Alive = false; //аннулируем статус существования игрока
        }
        if(this.PlayerId == ExplosionGame.PlayerId) { //Если это наш персонаж, тогда...
            byte[] byte_out = new byte[1];
            if(batfil.isExplosion((int)Position.x, (int)Position.y)) { //Проверка, попал ли персонаж под взрыв
                byte_out[0] = Multiplayer.ByteFormation(PlayerId, 6); //Отправляем сообщение об уменьшении его здоровья
                ExplosionGame.client.SendMessage(byte_out);
            }
            if((health == 0) && (!ifSendFinalMessage)) { //Если здоровье игрока опустилось до нуля, тогда...
                Alive = false; //аннулируем статус существования игрока
                ExplosionGame.Flag_isKilled.setValue(true); //Дублируем эту информацию в статической переменной-флаге
                //Отправляем сообщение о готовности персонажа перейти к окну окончания игры
                byte_out[0] = Multiplayer.ByteFormation(PlayerId, 0);
                ExplosionGame.client.SendMessage(byte_out);
                ifSendFinalMessage = true;
            }
        }
        /*if(batfil.isExplosion((int)Position.x, (int)Position.y)) { //Проверка, попал ли персонаж под взрыв
            setHealth(health - 1); //Забрать одну единицу здоровья
            if(health == 0) { //Если здоровье игрока опустилось до нуля, тогда...
                Alive = false; //аннулируем статус существования игрока
                if(this.PlayerId == ExplosionGame.PlayerId) {
                    ExplosionGame.Flag_isKilled.setValue(true); //Дублируем эту информацию в статической переменной-флаге
                    //Отправляем сообщение о готовности персонажа перейти к окну окончания игры
                    byte[] byte_out = new byte[1];
                    byte_out[0] = Multiplayer.ByteFormation(PlayerId, 0);
                    ExplosionGame.client.SendMessage(byte_out);
                }
            }
        }*/
        //Бонусное обновление
        if(batfil.isBonus((int)Position.x, (int)Position.y)) { //Проверка, есть ли на данной ячейке бонус
            batfil.takeBonus((int)Position.x, (int)Position.y, this); //Если да, тогда изымаем бонус
        }
    }

    //Получение значения скорости героя
    public int getVelocity() {
        return Velocity;
    }

    //Установка значения скорости героя
    public void setVelocity(int Velocity) {
        this.Velocity = Velocity;
    }

    //Проверка, может ли персонаж сделать ход в соответствующую сторону
    public boolean isPossibleToMove(char c, Battlefield batfil) {
        boolean b = false;
        switch (c) {
            case 'U':
                if (batfil.isEMPTY(Math.round(floatPosition.x), Math.round(floatPosition.y - (Velocity * 1.0f) / HEIGHT)))
                    b = true;
                break;
            case 'D':
                if (batfil.isEMPTY(Math.round(floatPosition.x), Math.round(floatPosition.y + (Velocity * 1.0f) / HEIGHT)))
                    b = true;
                break;
            case 'L':
                if (batfil.isEMPTY(Math.round(floatPosition.x - (Velocity * 1.0f) / WIDTH), Math.round(floatPosition.y)))
                    b = true;
                break;
            case 'R':
                if (batfil.isEMPTY(Math.round(floatPosition.x + (Velocity * 1.0f) / WIDTH), Math.round(floatPosition.y)))
                    b = true;
                break;
        }
        //System.out.println(b);
        return b;
    }

    //Метод отрисовки анимации, если герой стоит на месте
    public void AnimationForStanding() {
        CharacterAnimation.updateCharacterMovement('S', 0.01f);
    }

    //Метод движения игрока (изменение position) на одну позицию в соответствующее направление
    public void Move(char c, Battlefield batfil) {
        switch (c) {
            case 'U':
                setFloatPosition(floatPosition.x, floatPosition.y - (Velocity * 1.0f) / HEIGHT);
                shift_y += Velocity;
                if(!ShiftCorrection('U', batfil)) {
                    //
                }
                break;
            case 'D':
                setFloatPosition(floatPosition.x, floatPosition.y + (Velocity * 1.0f) / HEIGHT);
                shift_y -= Velocity;
                if(!ShiftCorrection('D', batfil)) {
                    //
                }
                break;
            case 'L':
                setFloatPosition(floatPosition.x - (Velocity * 1.0f) / WIDTH, floatPosition.y);
                shift_x -= Velocity;
                if(!ShiftCorrection('L', batfil)) {
                    //
                }
                break;
            case 'R':
                setFloatPosition(floatPosition.x + (Velocity * 1.0f) / WIDTH, floatPosition.y);
                shift_x += Velocity;
                if(!ShiftCorrection('R', batfil)) {
                    //
                }
                break;
        }
        setPosition(Math.round(floatPosition.x), Math.round(floatPosition.y));
        if(Math.abs(floatPosition.x - Position.x) < 0.00001) floatPosition.set(Position.x, floatPosition.y);
        if(Math.abs(floatPosition.y - Position.y) < 0.00001) floatPosition.set(floatPosition.x, Position.y);

    }

    //Корректировка сдвигов картинок таким образом, чтобы текстуры не наезжали друг на друга
    private boolean ShiftCorrection(char c, Battlefield batfil) {
        if(!isPossibleToMove(c, batfil)) {
            switch(c) {
                case 'U':
                    shift_y -= Velocity;
                    setFloatPosition(floatPosition.x, Math.round(floatPosition.y));
                    break;
                case 'D':
                    shift_y += Velocity;
                    setFloatPosition(floatPosition.x, Math.round(floatPosition.y));
                    break;
                case 'R':
                    shift_x -= Velocity;
                    setFloatPosition(Math.round(floatPosition.x), floatPosition.y);
                    break;
                case 'L':
                    shift_x += Velocity;
                    setFloatPosition(Math.round(floatPosition.x), floatPosition.y);
                    break;
            }
            return true;
        }
        CharacterAnimation.updateCharacterMovement(c, 0.1f); //Анимация движения персонажа
        return false;
    }

    //Корректировка того, чтобы игрок не мог ходить по соседним текстурам
    public void CorrectionTexturesOverlapping(Battlefield batfil) {
        if(!isPossibleToMove('U', batfil)) {
            shift_y = ((shift_y % HEIGHT) == 0 ) ? shift_y : (((shift_y / HEIGHT)) * HEIGHT);
            setFloatPosition(floatPosition.x, Math.round(floatPosition.y));
        }
        if(!isPossibleToMove('D', batfil)) {
            shift_y = ((shift_y % HEIGHT) == 0 ) ? shift_y : (((shift_y / HEIGHT) + 1) * HEIGHT);
            setFloatPosition(floatPosition.x, Math.round(floatPosition.y));
        }
        if(!isPossibleToMove('R', batfil)) {
            shift_x = ((shift_x % WIDTH) == 0 ) ? shift_x : (((shift_x / WIDTH)) * WIDTH);
            setFloatPosition(Math.round(floatPosition.x), floatPosition.y);
        }
        if(!isPossibleToMove('L', batfil)) {
            shift_x = ((shift_x % WIDTH) == 0 ) ? shift_x : (((shift_x / WIDTH) + 1) * WIDTH);
            setFloatPosition(Math.round(floatPosition.x), floatPosition.y);
        }
    }

    //Получение "целочисленных" координат положения героя
    public Vector2 getPosition() {
        return Position;
    }

    //Установка "целочисленных" координат положения героя
    public void setPosition(int i, int j) {
        this.Position.set(i,j);
    }

    //Получение "дробных" координат положения героя
    public Vector2 getFloatPosition() {
        return floatPosition;
    }

    //Установка "дробных" координат положения героя
    public void setFloatPosition(float x, float y) {
        this.floatPosition.set(x,y);
    }

    //Получения кадра анимации персонажа
    public TextureRegion getCharacter() {
        return CharacterAnimation.getFrame();
    }

    //Получение размера одного из кадров анимации персонажа
    public Vector2 getSizesOfTexture(){
        Vector2 v2sizes = new Vector2();
        v2sizes.set(texture.getWidth() / 3, texture.getHeight() / 4);
        return v2sizes;
    }

    //Получение сдвига по оси абсцисс
    public int getShift_x() {
        return shift_x;
    }
    //Получение сдвигов по оси ординат
    public int getShift_y() {
        return shift_y;
    }
    //Получение номера игрока
    public int getPlayerId() {
        return PlayerId;
    }
    //Установка номера игрока
    public void setPlayerId(int PlayerId) {
        this.PlayerId = PlayerId;
    }
    //Получение ответа "жив ли персонаж"
    public boolean isAlive() {
        return Alive;
    }
    //Установка характеристики "жив ли персонаж"
    public void setAlive(boolean Alive) {
        this.Alive = Alive;
    }
    //Получить размер запаса бомб у игрока
    public int getReserve() {
        return reserve;
    }
    //Задать размер запаса бомб у игрока
    public void setReserve(int reserve) { this.reserve = (reserve > reserveMax) ? reserveMax : ((reserve < 1) ? 1 : reserve); }
    //Получить максимально допустимый размер запаса бомб на одного игрока
    public int getReserveMax() { return reserveMax; }
    //Получить значение массива-запаса бомб с заданным номером
    public Bomb getBombReserve(int i) { return ((i < reserve && i > -1) ? BombReserve[i] : BombReserve[0]); }
    //Получить значение количества жизней на данный момент (и здоровых, и утерянных)
    public int getLife() { return life; }
    //Установить значение количества жизней на данный момент (и здоровых, и утерянных)
    public void setLife(int life) {this.life = (life > lifeMax) ? lifeMax : ((life < 0) ? 0 : life); }
    //Получить значение максимального количества жизней (и здоровых, и утерянных)
    public int getLifeMax() { return lifeMax; }
    //Установить значение максимального количества жизней (и здоровых, и утерянных)
    public void setLifeMax(int lifeMax) {this.lifeMax = (lifeMax > 6) ? 6 : ((lifeMax < 3) ? 3 : lifeMax); }
    //Получить значение здоровых жизней на данный момент
    public int getHealth() { return health; }
    //Установить значение здоровых жизней на данный момент
    public void setHealth(int health) {this.health = (health > life) ? life : ((health < 0) ? 0 : health); }

    //Бросить бомбу, то есть активировать её
    public void BombActivation(Battlefield batfil) {
        for(int i = 0; i < reserve; ++i) { //Бежим по всем возможным на данный момент бомбам запаса
            if((!BombReserve[i].getActive()) && //Если бомба неактивирована
                    (!batfil.isBOMB((int)this.getPosition().x, (int)this.getPosition().y))) { //и на слое бомб игрового поля нет пометки "бомба", тогда...
                BombReserve[i].setActive(true); //Делаем у бомбы статус "Активирована"
                BombReserve[i].setPosition((int)this.getPosition().x, (int)this.getPosition().y); //Устанавливаем у нее такие же координаты, как и у игрока
                batfil.setBombToField((int)this.getPosition().x, (int)this.getPosition().y); //Делаем пометку на поле, что на нем лежит бомба
                break; //Выходим из цикла, т.к. дальше нет смысла искать неактивированные бомбы
            }
        }
    }

    //Освобождение ресурсов
    public void dispose() {
        texture.dispose(); //"Освобождаем" картинку персонажа
        for(int i = 0; i < BombReserve.length; ++i) { //Освобождаем ресурсы для всего запаса бомб
            BombReserve[i].dispose();
        }
    }

}
