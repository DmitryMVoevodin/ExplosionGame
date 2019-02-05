package com.vdm.game.sprites;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

//Это класс, осуществляющий анимацию игровых объектов и игрового поля
public class Animation {

    private Array<TextureRegion> frames; //Массив (коллекция gdx) кадров текстур класса TextureRegion
    private float frame_MaxTime; //Максимальное время длительности фрейма
    private float frame_CurrentTime; //Текущее время для фрейма
    private int frame_Count; //Количество фреймов
    private int frame_Number; //Номер текущего фрейма среди всех фреймов
    private int frame_Width; //Ширина одного фрейма
    private int frame_Height; //Высота одного фрейма
    private int type; //Тип анимации - 0 для горизонтальной ленты и 1 для прямоугольного блока анимации
    private int character_metronom; //Аналог метронома для переключения анимации при передвижении персонажа
    private char character_direction;

    //Конструктор класса Animation с конфигурационными установками анимации
    public Animation(TextureRegion region, int frame_Count, float AnimationTime) {
        //Заполнение полей класса Animation
        this.type = 0;
        this.frame_Count = frame_Count; //Установка количества кадров
        this.frame_Width = region.getRegionWidth() / frame_Count; //Ширина кадра - это анимационная "горизонтальная" лента, деленная на количество фреймов на ней
        this.frame_Height = region.getRegionHeight(); //Высота кадра - просто высота "горизонтальной" анимационной ленты
        this.frame_MaxTime = AnimationTime / frame_Count; //Длительность одного фрейма выисляется как общее время на анимацию, деленное на количество фреймов в ней
        this.frame_CurrentTime = 0; //Обнуляем счетчик текущего времени
        this.frame_Number = 0; //Номер текущего кадра анимации (в данном случае, первого) - нулевой
        //Заполнение массива кадров для анимации
        frames = new Array<TextureRegion>(); //Установка массива кадров текстур
        for(int i = 0; i < frame_Count; ++i) { //Заполняем массив кадров текстур "нарезанными" из анимационной ленты кадрами
            frames.add(new TextureRegion(region, i * frame_Width, 0, frame_Width, frame_Height));
        }
    }

    //Перегрузка конструктора класса Animation для анимации передвижения персонажа
    public Animation(TextureRegion region, int frame_Count_horizontal, int frame_Count_vertical, float AnimationTime) {
        //Заполнение полей класса Animation
        this.type = 1;
        this.frame_Count = frame_Count_horizontal * frame_Count_vertical; //Установка количества кадров
        this.frame_Width = region.getRegionWidth() / frame_Count_vertical; //Здесь уже ширина и высота вычисляется из анимационной картинки-блока,
        this.frame_Height = region.getRegionHeight() / frame_Count_horizontal; //поэтому есть отличия с предыдущим конструктором
        this.frame_MaxTime = AnimationTime / (frame_Count_vertical - 1); //Длительность одного фрейма
        this.frame_CurrentTime = 0; //Обнуляем счетчик текущего времени
        this.frame_Number = 1; //Номер текущего кадра анимации (в данном случае, первого) - нулевой
        frames = new Array<TextureRegion>(); //Установка массива кадров текстур
        for(int i = 0; i < frame_Count_horizontal; ++i) { //Заполняем массив кадров текстур
            for(int j = 0; j < frame_Count_vertical; ++j) {
                frames.add(new TextureRegion(region, j * frame_Height, i * frame_Width, frame_Width, frame_Height));
            }
        }
        this.character_metronom = 0;
        this.character_direction = 'D';

    }

    //Обновление номера используемого фрейма
    public void update(float delta_t) { //delta_t контролирует скорость для проигрывания одного фрейма в анимации
        frame_CurrentTime += delta_t;  //Увеличиваем текущее время, соответствующее текущему фрейму
        if(frame_CurrentTime > frame_MaxTime) { //Если это время привысило установленный лимит на один кадр, тогда...
            frame_Number++; //Увеличим номер текущего фрейма, то есть "перейдём" на соседний фрейм
            frame_CurrentTime = 0; //и обнулим счетчик текущего времени, чтобы с новым соседним фреймом работать по-новому
        } //Но перейдя на соседний фрейм, мы могли достичь конца анимационной ленты и "выйти за предел" массива кадров, поэтому...
        if(frame_Number >= frame_Count) { //Если номер текущего фрейма превысил номер последнего фрейма, тогда...
            frame_Number = 0; //Зацикливаем анимацию путём смены номера фрейма на нулевой, то есть на первоначальный кадр
        }
    }

    //Обновление для панели игровых показателей
    public void updateCharacteristics(int frame_Number) {
        this.frame_Number = (frame_Number > frame_Count) ? (frame_Count - 1) : ((frame_Number < 0) ? 0 : frame_Number);
    }

    //Обновление анимации передвижения персонажа
    //c - U,D,R,L; delta_t контролирует скорость одного фрейма в анимации; b - двигается игрок или нет
    public void updateCharacterMovement(char c, float delta_t) {
        if(c != 'S') { // если герой движется
            character_direction = c;
            character_metronom++;
            if(character_metronom == 4) character_metronom = 0;
        } else {
            character_metronom = 0;
            if(c == 'S') c = character_direction;
        }
        frame_CurrentTime += delta_t;  //Увеличиваем текущее время, соответствующее текущему фрейму
        if(frame_CurrentTime > frame_MaxTime) { //Если это время привысило установленный лимит на один кадр, тогда...
            switch(c) { //Изначально берем кадры, на которых персонаж стоит, а не идет
                case 'U':
                    frame_Number = 10;
                    break;
                case 'D':
                    frame_Number = 1;
                    break;
                case 'R':
                    frame_Number = 7;
                    break;
                case 'L':
                    frame_Number = 4;
                    break;
            }
            frame_Number += ((character_metronom == 1)?(1):((character_metronom == 3)?(-1):(0))); //Изменяем фрейм
            //frame_Number += (character_metronom % 2 == 1)?1:(-1); //альтернативная версия изменения фрейма
            frame_CurrentTime = 0; //и обнулим счетчик текущего времени, чтобы с новым соседним фреймом работать по-новому
        }
    }

    //Обновление анимации нажатия кнопок
    //delta_t контролирует скорость одного фрейма в анимации
    public void updateButtonBar(boolean isPressed, float delta_t) {
        if (isPressed) {
            frame_CurrentTime = 0;
            frame_Number = 1;
        } else {
            frame_CurrentTime += delta_t;  //Увеличиваем текущее время, соответствующее текущему фрейму
            if(frame_CurrentTime > frame_MaxTime) {
                frame_Number = 0;
                frame_CurrentTime = 0; //и обнулим счетчик текущего времени, чтобы с новым соседним фреймом работать по-новому
            }
        }
    }

    //
    public void updateButtonMusic() {
        frame_Number++;
        if(frame_Number >= frame_Count) {
            frame_Number = 0;
        }
    }

    public void updateBattlefield() {
        frame_Number += (frame_Number < frame_Count - 1) ? 1 : 0;
    }

    //Получение текущего фрейма анимации для какой-либо дальнейшей отрисовки его в программе
    public TextureRegion getFrame() {
        return frames.get(frame_Number); //Кадр берется по номеру текущего фрейма из массива кадров, заполненного в конструкторе
    }

}
