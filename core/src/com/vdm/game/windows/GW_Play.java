package com.vdm.game.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.vdm.game.ExplosionGame;
import com.vdm.game.interconnection.Multiplayer;
import com.vdm.game.sprites.Battlefield;
import com.vdm.game.sprites.Bomb;
import com.vdm.game.sprites.ButtonBar;
import com.vdm.game.sprites.Character;

public class GW_Play extends GameWindow {

    private Battlefield batfil;
    private int NumberOfPlayers;
    private int PlayerIdMinusOne; //Номер игрока за вычетом единицы: 0, 1, 2, 3
    private Character CharacterArray[];
    private int shift_x, shift_y;
    private Vector2 additional_shift_bf, additional_shift_ch;
    private ButtonBar butbar;
    private Bomb bomb;
    private boolean[] isMoving;
    private byte[] byte_out;
    private boolean[] b_ActionForStanding;

    private Music music_game;
    private boolean b_for_music;

    public GW_Play(ManagementSystemOfGameWindows msgw) {
        super(msgw);
        camera.setToOrtho(false, ExplosionGame.WIDTH, ExplosionGame.HEIGHT);
        batfil = new Battlefield();
        bomb = new Bomb();
        NumberOfPlayers = ExplosionGame.NoP;
        PlayerIdMinusOne = ExplosionGame.PlayerId - 1;
        isMoving = new boolean[NumberOfPlayers];
        b_ActionForStanding = new boolean[NumberOfPlayers];
        for(int i = 0; i < NumberOfPlayers; ++i) {
            isMoving[i] = false;
        }
        CharacterArray = new Character[NumberOfPlayers]; //Создание массива игроков
        for(int i = 0; i < NumberOfPlayers; ++i) {
            CharacterArray[i] = new Character(ExplosionGame.TypeOfTextureMassiv[i], i + 1); //Установка игрока с определенным номером (для выбора текстуры) и определенным id. Потом изменить!!!!!!!!!!!!!
        }
        shift_x = CharacterArray[PlayerIdMinusOne].getShift_x();
        shift_y = CharacterArray[PlayerIdMinusOne].getShift_y();
        additional_shift_bf = new Vector2();
        additional_shift_bf.set(ExplosionGame.WIDTH / 2 - 3 * CharacterArray[PlayerIdMinusOne].getSizesOfTexture().x / 2,
                ExplosionGame.HEIGHT / 2 - 3 * CharacterArray[PlayerIdMinusOne].getSizesOfTexture().y / 2);
        additional_shift_ch = new Vector2();
        additional_shift_ch.set(ExplosionGame.WIDTH / 2 - CharacterArray[PlayerIdMinusOne].getSizesOfTexture().x / 2,
                ExplosionGame.HEIGHT / 2 - CharacterArray[PlayerIdMinusOne].getSizesOfTexture().y / 2);
        butbar = new ButtonBar(CharacterArray[PlayerIdMinusOne]);

        //Мероприятие по извещению всех игроков о том,
        //что данный игрок сформировал игровое окно GW_Play со всеми нужными данными и готов приступить к самой игре
        byte_out = new byte[1];
        byte_out[0] = Multiplayer.ByteFormation(26);
        ExplosionGame.client.SendMessage(byte_out);

        //Музыка и звуковые эффекты
        music_game = Gdx.audio.newMusic(Gdx.files.internal("game.mp3"));
        music_game.setLooping(true);
        music_game.setVolume(1.0f);
        butbar.isButtonPressed('M', true);
        b_for_music = false;
        if(b_for_music) {
            music_game.play();
        }

        //Дублируем информацию о здоровье игроков
        ExplosionGame.MassivOfHealth = new int[NumberOfPlayers];
        for(int i = 0; i < NumberOfPlayers; ++i) {
            ExplosionGame.MassivOfHealth[i] = CharacterArray[i].getHealth();
        }

        //Синхронизируем начало игры
        while(true) { //Ждём, пока не получим сигнал о готовности всех четырех игроков к игре (от каждого отдельное сообщение)
            if(ExplosionGame.Flag_ReadyToPlay.TriggerStep(ExplosionGame.NoP)){
                break; //Если счетчик сообщений стал равным количеству игроков, выходим из режима ожидания, то есть из цикла while(true) {...}
            }
        }

    }

    @Override
    protected void handleInput() {

        boolean bKeyB, bKeyU, bKeyD, bKeyR, bKeyL, bKeyM;
        bKeyB = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        bKeyU = Gdx.input.isKeyPressed(Input.Keys.UP);
        bKeyD = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        bKeyR = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        bKeyL = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        bKeyM = Gdx.input.isKeyPressed(Input.Keys.M);
        touchedCursor();
        if(butbar.isButtonPressed('M', bKeyM) || bKeyM) {
            b_for_music = b_for_music ? false : true;
            if(b_for_music) {
                music_game.play();
            } else {
                music_game.stop();
            }
        }
        if(butbar.isButtonPressed('L', bKeyL) || bKeyL) { //Сигнал на движение влево
            if(CharacterArray[PlayerIdMinusOne].isAlive()) { //Если персонаж жив, то включаем аппарат пересылки сообщений
                byte_out[0] = Multiplayer.ByteFormation(PlayerIdMinusOne + 1, 4);
                ExplosionGame.client.SendMessage(byte_out);
            } else { //Если персонаж погиб, тогда НЕ используем аппарат пересылки сообщений, но можем передвигаться по карте "невидимкой"
                shift_x -= CharacterArray[PlayerIdMinusOne].getVelocity();
            }
        }
        if(butbar.isButtonPressed('R', bKeyR) || bKeyR) { //Сигнал на движение вправо
            if(CharacterArray[PlayerIdMinusOne].isAlive()) { //Если персонаж жив, то включаем аппарат пересылки сообщений
                byte_out[0] = Multiplayer.ByteFormation(PlayerIdMinusOne + 1, 3);
                ExplosionGame.client.SendMessage(byte_out);
            } else { //Если персонаж погиб, тогда НЕ используем аппарат пересылки сообщений, но можем передвигаться по карте "невидимкой"
                shift_x += CharacterArray[PlayerIdMinusOne].getVelocity();
            }
        }
        if(butbar.isButtonPressed('D', bKeyD) || bKeyD) { //Сигнал на движение вниз
            if(CharacterArray[PlayerIdMinusOne].isAlive()) { //Если персонаж жив, то включаем аппарат пересылки сообщений
                byte_out[0] = Multiplayer.ByteFormation(PlayerIdMinusOne + 1, 2);
                ExplosionGame.client.SendMessage(byte_out);
            } else { //Если персонаж погиб, тогда НЕ используем аппарат пересылки сообщений, но можем передвигаться по карте "невидимкой"
                shift_y -= CharacterArray[PlayerIdMinusOne].getVelocity();
            }
        }
        if(butbar.isButtonPressed('U', bKeyU) || bKeyU) { //Сигнал на движение вверх
            if(CharacterArray[PlayerIdMinusOne].isAlive()) { //Если персонаж жив, то включаем аппарат пересылки сообщений
                byte_out[0] = Multiplayer.ByteFormation(PlayerIdMinusOne + 1, 1);
                ExplosionGame.client.SendMessage(byte_out);
            } else { //Если персонаж погиб, тогда НЕ используем аппарат пересылки сообщений, но можем передвигаться по карте "невидимкой"
                shift_y += CharacterArray[PlayerIdMinusOne].getVelocity();
            }
        }
        if(butbar.isButtonPressed('B', bKeyB) || bKeyB) { //Сигнал на сброс бомбы
            if(CharacterArray[PlayerIdMinusOne].isAlive()) { //Если персонаж жив, то включаем аппарат пересылки сообщений
                byte_out[0] = Multiplayer.ByteFormation(PlayerIdMinusOne + 1, 5);
                ExplosionGame.client.SendMessage(byte_out);
            }
        }

    }


    public boolean CharacterActions(int PlayerIdMinusOne, char c) {
        if(c == 'B') {
            CharacterArray[PlayerIdMinusOne].BombActivation(batfil);
            ExplosionGame.bomb_sound.play(); //Звук устанавливаемой бомбы
            return false;
        }
        boolean b = false;
        if(CharacterArray[PlayerIdMinusOne].isPossibleToMove(c, batfil)) {
            CharacterArray[PlayerIdMinusOne].Move(c, batfil);
            if(PlayerIdMinusOne == this.PlayerIdMinusOne) {
                switch(c){
                    case 'U': case 'D': shift_y = CharacterArray[PlayerIdMinusOne].getShift_y(); break;
                    case 'R': case 'L': shift_x = CharacterArray[PlayerIdMinusOne].getShift_x(); break;
                }
            }
            b = true;
        }
        return b;
    }


    //Метод, обновляющий и игроков, и игровое поле
    @Override
    public void update(float delta_t) {

        //Обработка сообщений из очереди
        byte bt; //Переменная для считываемого байта зашифрованной информации
        for(int i = 0; i < NumberOfPlayers; ++i) { //Для всех игроков...
            b_ActionForStanding[i] = false;// "обнулить" переменную логическую переменную-флаг "наличие активности"
        }
        int n = ExplosionGame.MessagesQueue2.size(); //Запомнить размер очереди скопившихся сообщений
        while(n > 0) { //Пока ещё есть сообщения в очереди...
            if(ExplosionGame.MessagesQueue2.size() > 0) {
                bt = ExplosionGame.MessagesQueue2.poll().getBytes()[0]; //Вытаскивать сообщение из очереди и запоминать байт
                Multiplayer.ByteTranslateToAction(bt);//Расшифровывать этот байт - меняются статические переменные PlayerID_tmp и command_tmp в ExplosionGame, и стат.переменные здоровья

                //Определить, было ли движение от игрока и реализовать активность этого движения и сброс бомб, если он имел место быть
                isMoving[ExplosionGame.PlayerID_tmp - 1] =
                        CharacterActions(ExplosionGame.PlayerID_tmp - 1, ExplosionGame.command_tmp); //Здесь производятся изменения!
                //Вообще была ли какая-нибудь активность? Сброс бомб или активность движения?
                b_ActionForStanding[ExplosionGame.PlayerID_tmp - 1] =
                        (isMoving[ExplosionGame.PlayerID_tmp - 1]) || (ExplosionGame.command_tmp == 'B'); //Флаг-переменная - было ли движение
            }
            n--; //Отметить, что размер накопившихся непрочитанных сообшений для текущего прогона отрисовки и обновления уменьшился
        }
        //Анимация для игрока, который ничего не делает, а просто стоит на месте
        for(int i = 0; i < NumberOfPlayers; ++i) { //Проверяем по всем игрокам
            if (!b_ActionForStanding[i]) { //Если не было никакой активности - сброса бомб или движения - запускаем анимацию
                CharacterArray[i].AnimationForStanding();
            }
        }

        //Обновления персонажей
        for(int i = 0; i < NumberOfPlayers; ++i) { //Пробегаем по всем персонажам
            if(CharacterArray[i].isAlive()) {
                CharacterArray[i].update(batfil); //Обновить героя, согласно информации с игрового поля
            }
        }
        batfil.update(); //Обновить боевое поле
        handleInput(); //Взаимодействие с вводом информации
        //Корректировка отрисовки и положения героя в случае перекрытия текстур
        for(int i = 0; i < NumberOfPlayers; ++i) { //Пробегаем по всем игрокам
            if(isMoving[i]) { //Если персонаж двигается
                CharacterArray[i].CorrectionTexturesOverlapping(batfil); //Корректируем перекрытие текстур
                if(i == PlayerIdMinusOne) { //Если это наш персонаж, то необходимо откорректировать весь экран, чтобы и он "прыжок" сделал
                    shift_x = CharacterArray[PlayerIdMinusOne].getShift_x();
                    shift_y = CharacterArray[PlayerIdMinusOne].getShift_y();
                }
                isMoving[i] = false;
            }
        }

        //Переключение экрана GW_Play на GW_GameOver
        if(ExplosionGame.Flag_isReadyToGameOver.TriggerStep(NumberOfPlayers - 1)) {
            msgw.set(new GW_GameOver(msgw));
        }

    }

    //Общая отрисовка (агрегирование)
    @Override
    public void render(SpriteBatch spriteBatch) {
        spriteBatch.setProjectionMatrix(camera.combined); //Использовать систему координат камеры
        spriteBatch.begin(); //Начало оптимизационого блока отрисовки
        DrawBattleField(spriteBatch, batfil);//Рисуем игровое поле
        DrawBonusField(spriteBatch); //Рисуем бонусы
        DrawBomb(spriteBatch); //Рисуем бомбы игрока
        DrawPlayers(spriteBatch); //Рисовать игроков
        DrawButtonBar(spriteBatch); //Рисовать панель кнопок
        spriteBatch.end(); //Конец оптимизационого блока отрисовки
    }

    //Отрисовка боевого поля
    private void DrawBattleField(SpriteBatch spriteBatch, Battlefield batfil) {
        //Отрисовка игрового поля
        for(int i = 0; i < Battlefield.getCountOfRows(); ++i) {
            for(int j = 0; j < Battlefield.getCountOfColumns(); ++j) {
                spriteBatch.draw(batfil.getBattleField(i, j),
                        i * CharacterArray[PlayerIdMinusOne].getSizesOfTexture().x - shift_x + additional_shift_bf.x,
                        (Battlefield.getCountOfRows() - j - 1) * CharacterArray[PlayerIdMinusOne].getSizesOfTexture().y - shift_y + additional_shift_bf.y);
            }
        }
        //Отрисовка заднего фона, то есть боковых полей
        spriteBatch.draw(batfil.getfont(),
                - 375 - shift_x + additional_shift_bf.x,
                - 216 - shift_y + additional_shift_bf.y);
    }

    //Отрисовка бонусов
    private void DrawBonusField(SpriteBatch spriteBatch) {
        for(int i = 0; i < Battlefield.getCountOfRows(); ++i) {
            for(int j = 0; j < Battlefield.getCountOfColumns(); ++j) {
                if(batfil.isBonus(i, j)) {
                    spriteBatch.draw(batfil.getBonusField(i, j),
                            i * CharacterArray[PlayerIdMinusOne].getSizesOfTexture().x - shift_x + additional_shift_bf.x,
                            (Battlefield.getCountOfRows() - j - 1) * CharacterArray[PlayerIdMinusOne].getSizesOfTexture().y - shift_y + additional_shift_bf.y);
                    batfil.updateBonus(i, j);
                }
            }
        }
    }

    //Отрисовка всех игроков
    private void DrawPlayers(SpriteBatch spriteBatch) {
        float x = 0;
        float y = 0;
        for(int i = 0; i < NumberOfPlayers; ++i) {
            if(CharacterArray[i].isAlive()) {
                if(i != PlayerIdMinusOne) { //Рисуем врага
                    x = CharacterArray[i].getFloatPosition().x * CharacterArray[PlayerIdMinusOne].getSizesOfTexture().x;
                    y = (Battlefield.getCountOfRows() - 1 - CharacterArray[i].getFloatPosition().y) * CharacterArray[PlayerIdMinusOne].getSizesOfTexture().y;
                    spriteBatch.draw(CharacterArray[i].getCharacter(),
                            x - shift_x + additional_shift_bf.x,
                            y - shift_y + additional_shift_bf.y);
                } else {
                    spriteBatch.draw(CharacterArray[i].getCharacter(), additional_shift_ch.x, additional_shift_ch.y); //Рисуем героя
                }
            }
        }
    }

    //Отрисовка игрового управления
    private void DrawButtonBar(SpriteBatch spriteBatch) {

        spriteBatch.draw(butbar.get_btn_bomb(),
                ExplosionGame.WIDTH - butbar.getWidth_btn_bomd(),
                (ExplosionGame.HEIGHT - butbar.getHeight_btn_bomd()) / 2); //Рисуем кнопку атаки

        spriteBatch.draw(butbar.get_btn_left(),
                0,
                (ExplosionGame.HEIGHT - butbar.getHeight_btn_UDRL()) / 2 ); //Кнопка влево

        spriteBatch.draw(butbar.get_btn_right(),
                butbar.getWidth_btn_UDRL() * 2,
                (ExplosionGame.HEIGHT - butbar.getHeight_btn_UDRL()) / 2 ); //Кнопка вправо

        spriteBatch.draw(butbar.get_btn_up(),
                butbar.getWidth_btn_UDRL() * 1,
                (ExplosionGame.HEIGHT - butbar.getHeight_btn_UDRL()) / 2 + butbar.getHeight_btn_UDRL()); //Кнопка вверх

        spriteBatch.draw(butbar.get_btn_down(),
                butbar.getWidth_btn_UDRL() * 1,
                (ExplosionGame.HEIGHT - butbar.getHeight_btn_UDRL()) / 2 - butbar.getHeight_btn_UDRL()); //Кнопка вниз

        //Отрисовка жизней
        butbar.updateHeart(0);
        for(int i = 0; i < CharacterArray[PlayerIdMinusOne].getHealth(); ++i) {
            spriteBatch.draw(butbar.get_heart(),i * butbar.getWidth_heart(), (ExplosionGame.HEIGHT - butbar.getHeight_heart()));
        }
        butbar.updateHeart(1);
        for(int i = 0; i < (CharacterArray[PlayerIdMinusOne].getLife() - CharacterArray[PlayerIdMinusOne].getHealth()); ++i) {
            spriteBatch.draw(butbar.get_heart(),(i + CharacterArray[PlayerIdMinusOne].getHealth()) * butbar.getWidth_heart(), (ExplosionGame.HEIGHT - butbar.getHeight_heart()));
        }

        //Отрисовка музыкального значка
        spriteBatch.draw(butbar.get_music(), 0,0);

    }

    //Отрисовка бомб и последующего взрыва
    private void DrawBomb(SpriteBatch spriteBatch) {

        for(int player_index = 0; player_index < NumberOfPlayers; ++player_index) { //Для всех игроков

            float x_bomb, y_bomb, x_expl, y_expl;
            for(int i = 0; i < CharacterArray[player_index].getReserve(); ++i) {
                x_bomb = (CharacterArray[player_index].getBombReserve(i).getPosition().x)* CharacterArray[player_index].getSizesOfTexture().x - shift_x + additional_shift_bf.x;
                y_bomb = (Battlefield.getCountOfRows() - 1 - CharacterArray[player_index].getBombReserve(i).getPosition().y) * CharacterArray[player_index].getSizesOfTexture().y - shift_y + additional_shift_bf.y;
                if(CharacterArray[player_index].getBombReserve(i).getActive()) {
                    spriteBatch.draw(CharacterArray[player_index].getBombReserve(i).getBomb(), x_bomb, y_bomb);
                    CharacterArray[player_index].getBombReserve(i).update((float)(1.0/6.0), batfil);
                }
                x_expl = (CharacterArray[player_index].getBombReserve(i).getPositionExplosion().x)* CharacterArray[player_index].getSizesOfTexture().x - shift_x + additional_shift_bf.x;;
                y_expl = (Battlefield.getCountOfRows() - 1 - CharacterArray[player_index].getBombReserve(i).getPositionExplosion().y) * CharacterArray[player_index].getSizesOfTexture().y - shift_y + additional_shift_bf.y;
                if(CharacterArray[player_index].getBombReserve(i).getActiveExplosion()) {
                    spriteBatch.draw(CharacterArray[player_index].getBombReserve(i).getExplosion(), x_expl, y_expl);
                    for(int j = 0; j < CharacterArray[player_index].getBombReserve(i).getStrength(); ++j) {
                        if(CharacterArray[player_index].getBombReserve(i).getArrayOfDamagedRegion_ij(0,j)) { //Up
                            spriteBatch.draw(CharacterArray[player_index].getBombReserve(i).getExplosion(), x_expl, y_expl + CharacterArray[player_index].getSizesOfTexture().y * (j + 1));
                        }
                        if(CharacterArray[player_index].getBombReserve(i).getArrayOfDamagedRegion_ij(1,j)) { //Down
                            spriteBatch.draw(CharacterArray[player_index].getBombReserve(i).getExplosion(), x_expl, y_expl - CharacterArray[player_index].getSizesOfTexture().y * (j + 1));
                        }
                        if(CharacterArray[player_index].getBombReserve(i).getArrayOfDamagedRegion_ij(2,j)) { //Right
                            spriteBatch.draw(CharacterArray[player_index].getBombReserve(i).getExplosion(), x_expl + CharacterArray[player_index].getSizesOfTexture().x * (j + 1), y_expl);
                        }
                        if(CharacterArray[player_index].getBombReserve(i).getArrayOfDamagedRegion_ij(3,j)) { //Left
                            spriteBatch.draw(CharacterArray[player_index].getBombReserve(i).getExplosion(), x_expl - CharacterArray[player_index].getSizesOfTexture().x * (j + 1), y_expl);
                        }
                    }
                    CharacterArray[player_index].getBombReserve(i).updateExplosion(batfil);
                }
            }

        }//Переходим к следующему игроку

    }

    @Override
    public void dispose() {

        music_game.dispose();

        for(int i = 0; i < NumberOfPlayers; ++i) {
            CharacterArray[i].dispose();
        }
        butbar.dispose();
        bomb.dispose();
        batfil.dispose();

        //ExplosionGame.client.dispose();
        //ExplosionGame.server.KillExistence();
        //ExplosionGame.server.dispose();

    }

}