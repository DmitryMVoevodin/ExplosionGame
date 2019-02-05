package com.vdm.game.interconnection;

import com.badlogic.gdx.Gdx;
import com.vdm.game.ExplosionGame;
import com.vdm.game.sprites.Battlefield;

public class Multiplayer {

    //Формирование байта с зашифрованным сообщением
    public static byte ByteFormation(int PlayerID, int MessageIndex){ //Для игрока с определенным номером PlayerID: 1, 2, 3, 4
        if(PlayerID > 4 || PlayerID < 1) { //Если указали неверный номер игрока, тогда...
            return ByteFormation(63); //Выбросим сообщение 63 служебной информации, которое не приводит ни к каким действиям
        }
        MessageIndex = (MessageIndex > 15) ? 15 : ((MessageIndex < 0) ? 15 : (MessageIndex)); //Если указали неверный номер, сообщение 15, то есть "пустое"
        return ((byte)((PlayerID - 1) * 16 + MessageIndex));
    }
    public static byte ByteFormation(int MessageIndex) {//Для служебной информации
        MessageIndex = (MessageIndex > 63) ? 63 : ((MessageIndex < 0) ? 63 : (MessageIndex)); //Если указали неверный номер, сообщение 63, то есть "пустое"
        return ((byte)(64 + MessageIndex));
    }

    //Расшифровка сообщения из получаемого байта
    public static void ByteTranslateToAction(byte message) {
        //Предварительная подготовка дешифровщика сообщения
        int PlayerID = 1; //Номер игрока
        boolean ServiceInformation; //Это служебная информация или нет?
        if((message & 64) != 64) { //То есть message представляет собой 00##.####
            ServiceInformation = false;
            //Дешифрование номера игрока
            switch(message & 48) { // 00ab.#### & 0011.0000 = 00ab.0000
                case 0: // То есть 0000.0000; для игрока 1 message будет 0000.####
                    PlayerID = 1;
                    break;
                case 16: //0001.0000; для игрока 2 message будет 0001.####
                    PlayerID = 2;
                    break;
                case 32: //0010.0000; для игрока 3 message будет 0010.####
                    PlayerID = 3;
                    break;
                case 48: //0011.0000; для игрока 4 message будет 0011.####
                    PlayerID = 4;
                    break;
            }
        } else { //Если message представляет собой 01##.####
            ServiceInformation = true;
        }
        //Процесс дешифрования//////////////////////////////////////////////////////////////////////
        byte[] b_OUT = new byte[1];
        if(ServiceInformation) { //Блок служебной информации
            switch(message & 63) { //01##.#### & 0011.1111 = 00##.####
                case 0: //0000.0000 - Назначение номера 1-ом игроку; исходный код: 0100.0000 = 64
                    ExplosionGame.PlayerId = 1;
                    break;
                case 1: //0000.0001 - Назначение номера 2-ому игроку; исходный код: 0100.0001 = 65
                    ExplosionGame.PlayerId = 2;
                    break;
                case 2: //0000.0010 - Назначение номера 3-ему игроку; исходный код: 0100.0010 = 66
                    ExplosionGame.PlayerId = 3;
                    break;
                case 3: //0000.0011 - Назначение номера 4-ому игроку; исходный код: 0100.0011 = 67
                    ExplosionGame.PlayerId = 4;
                    break;
                case 4: //0000.0100 - Смена окон c GW_MainMenu на GW_Play; исходный код: 0100.0100 = 68
                    ExplosionGame.Flag_MainMenu_To_Play.setValue(true);
                    break;
                case 5: //0000.0101 - Зарезервированная строка (если приложение будет разрабатываться далее); исходный код: 0100.0101 = 69
                    break;
                case 6: //0000.0110 - Зарезервированная строка (если приложение будет разрабатываться далее); исходный код: 0100.0110 = 70
                    break;
                case 7: //0000.0111 - Сообщение о занесении в ячейку боевого поля EMPTY; исходный код: 0100.0111 = 71
                    CreatingInitialArray(Battlefield.TypeOfField.EMPTY);
                    break;
                case 8: //0000.1000 - Сообщение о занесении в ячейку боевого поля WALL; исходный код: 0100.1000 = 72
                    CreatingInitialArray(Battlefield.TypeOfField.WALL);
                    break;
                case 9: //0000.1001 - Сообщение о занесении в ячейку боевого поля TREE; исходный код: 0100.1001 = 73
                    CreatingInitialArray(Battlefield.TypeOfField.TREE);
                    break;
                case 10: //0000.1010 - Сообщение о занесении в ячейку боевого поля ROCK; исходный код: 0100.1010 = 74
                    CreatingInitialArray(Battlefield.TypeOfField.ROCK);
                    break;
                case 11: //0000.1011 - Сообщение о занесении в ячейку боевого поля WATER; исходный код: 0100.1011 = 75
                    CreatingInitialArray(Battlefield.TypeOfField.WATER);
                    break;
                case 12: //0000.1100 - Сообщение о занесении в ячейку боевого поля STRENGTH; исходный код: 0100.1100 = 76
                    CreatingInitialArrayBonuses(Battlefield.TypeOfField.STRENGTH);
                    break;
                case 13: //0000.1101 - Сообщение о занесении в ячейку боевого поля EXTRALIFE; исходный код: 0100.1101 = 77
                    CreatingInitialArrayBonuses(Battlefield.TypeOfField.EXTRALIFE);
                    break;
                case 14: //0000.1110 - Сообщение о занесении в ячейку боевого поля RESERVE; исходный код: 0100.1110 = 78
                    CreatingInitialArrayBonuses(Battlefield.TypeOfField.RESERVE);
                    break;
                case 15: //0000.1111 - Сообщение о занесении в ячейку боевого поля HEALTH; исходный код: 0100.1111 = 79
                    CreatingInitialArrayBonuses(Battlefield.TypeOfField.HEALTH);
                    break;
                case 16: //0001.0000 - Сообщение о занесении в ячейку боевого поля HEALTH; исходный код: 0101.0000 = 80
                    CreatingInitialArrayBonuses(Battlefield.TypeOfField.EMPTY_BONUS);
                    break;
                // Здесь пропущено еще 10 кейсов для того, чтобы зарезервировать в общей сумме 20 видов сообщений для лабиринта в случае дальнейшей разработки приложения
                case 26: //0001.1010 - Сообщение о том, что игрок сформировал игровое окно GW_Play и готов приступить к игре; исходный код: 0101.1010 = 90
                    ExplosionGame.Flag_ReadyToPlay.IncreaseStep();
                    break;
                case 27: //0001.1011 - Сообщение о том, что игроков в игре - 1 человек; исходный код: 0101.1011 = 91
                    ExplosionGame.NoP = 1;
                    ExplosionGame.TypeOfTextureMassiv = new int[ExplosionGame.NoP];
                    break;
                case 28: //0001.1100 - Сообщение о том, что игроков в игре - 2 человека; исходный код: 0101.1100 = 92
                    ExplosionGame.NoP = 2;
                    ExplosionGame.TypeOfTextureMassiv = new int[ExplosionGame.NoP];
                    break;
                case 29: //0001.1101 - Сообщение о том, что игроков в игре - 3 человека; исходный код: 0101.1101 = 93
                    ExplosionGame.NoP = 3;
                    ExplosionGame.TypeOfTextureMassiv = new int[ExplosionGame.NoP];
                    break;
                case 30: //0001.1110 - Сообщение о том, что игроков в игре - 4 человека; исходный код: 0101.1110 = 94
                    ExplosionGame.NoP = 4;
                    ExplosionGame.TypeOfTextureMassiv = new int[ExplosionGame.NoP];
                    break;
                case 31: //0001.1111 - Сообщение о том, что ; исходный код: 0101.1111 = 95
                    break;
                case 32: //0010.0000 - Сообщение о том, что ; исходный код: 0110.0000 = 96
                    break;
                case 33: //0010.0001 - Сообщение о том, что ; исходный код: 0110.0001 = 97
                    break;
                case 34: //0010.0010 - Сообщение о том, что ; исходный код: 0110.0010 = 98
                    break;

                case 63: //0011.1111 - Зарезервированное "пустое" сообщение, то есть не нужно ничего делать; исходный код: 0111.1111 = 127
                    //Do nothing
                    break;
            }
        } else { //Блок информации по действиям персонажей
            switch(message & 15){//00ab.#### & 0000.1111 = 0000.####
                case 0://0000.0001 - Сообщение о том, что персонаж погиб и готов к переходу игры в завершающее окно; исходные коды: 0, 16, 32, 48
                    ExplosionGame.Flag_isReadyToGameOver.IncreaseStep();
                    break;
                case 1://0000.0001 - Сообщение о том, что персонаж делает попытку двинуться вверх; исходные коды: 1, 17, 33, 49
                    ExplosionGame.PlayerID_tmp = PlayerID;
                    ExplosionGame.command_tmp = 'U';
                    break;
                case 2://0000.0010 - Сообщение о том, что персонаж делает попытку двинуться вниз; исходные коды: 2, 18, 34, 50
                    ExplosionGame.PlayerID_tmp = PlayerID;
                    ExplosionGame.command_tmp = 'D';
                    break;
                case 3://0000.0011 - Сообщение о том, что персонаж делает попытку двинуться вправо; исходные коды: 3, 19, 35, 51
                    ExplosionGame.PlayerID_tmp = PlayerID;
                    ExplosionGame.command_tmp = 'R';
                    break;
                case 4://0000.0100 - Сообщение о том, что персонаж делает попытку двинуться влево; исходные коды: 4, 20, 36, 52
                    ExplosionGame.PlayerID_tmp = PlayerID;
                    ExplosionGame.command_tmp = 'L';
                    break;
                case 5://0000.0101 - Сообщение о том, что персонаж делает попытку сбросить бомбу; исходные коды: 5, 21, 37, 53
                    ExplosionGame.PlayerID_tmp = PlayerID;
                    ExplosionGame.command_tmp = 'B';
                    break;
                case 6://0000.0110 - Сообщение о том, что персонаж уменьшает здоровье; исходные коды: 6, 22, 38, 54
                    ExplosionGame.MassivOfHealth[PlayerID - 1] = Math.max(0, ExplosionGame.MassivOfHealth[PlayerID - 1] - 1);
                    break;
                case 7://0000.0111 - Сообщение о текстуре 1; исходные коды: 7, 23, 39, 55
                    ExplosionGame.TypeOfTextureMassiv[PlayerID - 1] = 1;
                    ExplosionGame.Flag_isReadyToMoveForward.IncreaseStep();
                    break;
                case 8://0000.1000 - Сообщение о текстуре 2; исходные коды: 8, 24, 40, 56
                    ExplosionGame.TypeOfTextureMassiv[PlayerID - 1] = 2;
                    ExplosionGame.Flag_isReadyToMoveForward.IncreaseStep();
                    break;
                case 9://0000.1001 - Сообщение о текстуре 3; исходные коды: 9, 25, 41, 57
                    ExplosionGame.TypeOfTextureMassiv[PlayerID - 1] = 3;
                    ExplosionGame.Flag_isReadyToMoveForward.IncreaseStep();
                    break;
                case 10://0000.1010 - Сообщение о текстуре 4; исходные коды: 10, 26, 42, 58
                    ExplosionGame.TypeOfTextureMassiv[PlayerID - 1] = 4;
                    ExplosionGame.Flag_isReadyToMoveForward.IncreaseStep();
                    break;
                case 15: //0000.1111 - Зарезервированное "пустое" сообщение, то есть не нужно ничего делать; исходные коды: 15, 31, 47, 63
                    //Do nothing
                    break;
            }
        }

    }

    private static void CreatingInitialArray(Battlefield.TypeOfField element) {
        ExplosionGame.InitialArray[ExplosionGame.InitialArray_i][ExplosionGame.InitialArray_j] = element;
        ExplosionGame.ijIterationOfInitialArray();
    }
    private static void CreatingInitialArrayBonuses(Battlefield.TypeOfField element) {
        ExplosionGame.InitialArrayBonuses[ExplosionGame.InitialArray_i][ExplosionGame.InitialArray_j] = element;
        ExplosionGame.ijIterationOfInitialArray();
    }

    //Вернуть зашифрованный байт (для передачи по сети) от типа ячейки игрового поля
    public static int IndexFromTypeOfField(Battlefield.TypeOfField element) {
        byte int_element = 0;
        switch(element) {
            case EMPTY: int_element = 7; break;
            case WALL: int_element = 8; break;
            case TREE: int_element = 9; break;
            case ROCK: int_element = 10; break;
            case WATER: int_element = 11; break;
            case STRENGTH: int_element = 12; break;
            case EXTRALIFE: int_element = 13; break;
            case RESERVE: int_element = 14; break;
            case HEALTH: int_element = 15; break;
            case EMPTY_BONUS: int_element = 16; break;
        }
        return int_element;
    }

    //Класс флагов
    public static class Flag{

        private boolean flag; //Логическая переменная-флаг
        private int step; // Переменная-счетчик

        //Конструктор, "обнуляющий" логическую переменную-флаг и счетчик
        public Flag() {
            flag = false;
            step = 0;
        }

        //Триггерный метод: проверяется в условии if(obj_flag.Trigger())
        //значение величины flag. Если true, тогда условие выполняется, а flag "триггерно"
        //становится равным false, то есть "обнуляется".
        public boolean Trigger() {
            boolean flag = this.flag;
            this.flag = false;
            return flag;
        }

        //Метод, который выдает false, когда step еще не достиг "переключающего" значения TriggerStepValue,
        //и true, когда достиг. Но по достижении условия step "триггерно обнуляется".
        //Используется в условиях if(obj_flag.TriggerStep(a)).
        public boolean TriggerStep(int TriggerStepValue) {
            if(step >= TriggerStepValue) {
                step = 0;
                return true;
            } else {
                return false;
            }
        }

        //Увеличить значение счетчика на единицу
        public void IncreaseStep() {
            ++step;
        }

        //Установить значение логической переменной
        public void setValue(boolean flag) {
            this.flag = flag;
        }

        //Установить значение переменной-счетчика
        public void setValue(int step) {
            this.step = step;
        }

        //Установить значения логической переменной и переменной-счетчика
        public void setValue(boolean flag, int step) {
            this.flag = flag;
            this.step = step;
        }

        //Получить значение логической переменной
        public boolean getValue() {
            return flag;
        }

        //Получить значение переменной-счетчика
        public int getValueStep() {
            return step;
        }

    }

}
