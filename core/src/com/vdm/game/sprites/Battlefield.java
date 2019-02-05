package com.vdm.game.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.vdm.game.ExplosionGame;
import com.vdm.game.interconnection.Multiplayer;

import java.util.Random;
import java.util.Stack;

//Данный класс описывает объект игрового поля
public class Battlefield {

    private static final int COUNT_OF_ROWS = 25; //Число "строк" игрового поля
    private static final int COUNT_OF_COLUMNS = 25; //Число "столбцов" игрового поля
    public enum TypeOfField { //Перечисление типов квадратов игрового поля
        VISITED,        //Посещенное поле (нужно для генерирования лабиринта) - 0
        EMPTY,          //Пустое поле - 1
        WALL,           //Стена - 2
        TREE,           //Дерево - 3
        ROCK,           //Скала - 4
        WATER,          //Вода - 5
        STRENGTH,       //Увеличение силы взрыва - 6
        EXTRALIFE,      //Дополнительная жизнь - 7
        RESERVE,        //Увеличение запаса бомб - 8
        HEALTH,         //Увеличение здоровья - 9
        EMPTY_BONUS,    //Пустое бонусное поле - 10
        BOMB,           //Бомба - 11
        EXPLOSION       //Взрыв - 12
    };
    private TypeOfField BattlefieldArray[][]; //Двумерный массив-проекция на игровое поле
    private TypeOfField BombArray[][]; //Двумерный массив-проекция с расположением бомб
    private TypeOfField ExplosionArray[][]; //Двумерный массив-проекция со взрывной волной
    private boolean ExplosionExists; //Переменная, оповещающая, что где-то на карте был взрыв
    private TypeOfField BonusArray[][]; //Двумерный массив-проекция с расположением бонусов
    private boolean boolBonusArray[][]; //Двумерный массив с идентификацией бонуса
    private Random rnd_for_bonus; //Случайное число для генерирования бонусов
    private TypeOfField AdditionalBonusArray[][]; //Дополнительный массив с бонусами, который используется для их первоначальной генерации

    private Texture
            textureEMPTY, //Текстура пустого поля
            textureWALL, //Текстура стены
            textureTREE,
            textureROCK,
            textureWATER,
            textureSTRENGTH,
            textureEXTRALIFE,
            textureRESERVE,
            textureHEALTH;

    private Texture font_leftside;
    private Animation font_animation;

    private Animation BattlefieldAnimation[][]; //Переменная-массив класса анимации для заполнения текстурами игрового поля
    private Animation BonusAnimation[][]; //Переменная-массив класса анимации для бонусов
    private Rectangle[] Bounds; //массив прямоугольников каждого игрового квадрата поля для текстур

    public Battlefield() {
        //Создание двумерного массива-проекции на игровое поле
        BattlefieldArray = new TypeOfField[COUNT_OF_ROWS][COUNT_OF_COLUMNS];
        BombArray = new TypeOfField[COUNT_OF_ROWS][COUNT_OF_COLUMNS];
        ExplosionArray = new TypeOfField[COUNT_OF_ROWS][COUNT_OF_COLUMNS];
        BonusArray = new TypeOfField[COUNT_OF_ROWS][COUNT_OF_COLUMNS];
        AdditionalBonusArray = new TypeOfField[COUNT_OF_ROWS][COUNT_OF_COLUMNS];
        if(ExplosionGame.isServer) { //Для игрока, у которого есть сервер
            //Создание игрового поля на стороне игрока, владеющего сервером
            CreateBattlefield();
            CreateAdditionalBonusArray();
            //Заполнение статического массива игрового поля в ExplosionGame через отсылку сообщений
            byte[] byte_out = new byte[1];
            //Заполнение статического массива ландшафта
            for(int i = 0; i < COUNT_OF_ROWS; ++i) {
                for(int j = 0; j < COUNT_OF_COLUMNS; ++j) {
                    //Переводим значение ячейки поля ландшафта в байт-сигнал и отправляем серверу, чтобы он потом разослал его по игрокам
                    byte_out[0] = Multiplayer.ByteFormation(Multiplayer.IndexFromTypeOfField(BattlefieldArray[i][j]));
                    ExplosionGame.client.SendMessage(byte_out);
                }
            }
            //Заполнение статического массива бонусов
            for(int i = 0; i < COUNT_OF_ROWS; ++i) {
                for(int j = 0; j < COUNT_OF_COLUMNS; ++j) {
                    //Переводим значение ячейки поля бонусов в байт-сигнал и отправляем серверу, чтобы он потом разослал его по игрокам
                    byte_out[0] = Multiplayer.ByteFormation(Multiplayer.IndexFromTypeOfField(AdditionalBonusArray[i][j]));
                    ExplosionGame.client.SendMessage(byte_out);
                }
            }
        } else { //Для игроков, которые не являются серверами,...
            while(true) { //...остается только ждать, пока не заполнятся два статических массива в ExplosionGame после рассылки
                if(ExplosionGame.Flag_InitialArray.TriggerStep(2)) { //Сигналом служит количество проходов step в Flag_InitialArray
                    for(int i = 0; i < COUNT_OF_ROWS; ++i) {
                        for(int j = 0; j < COUNT_OF_COLUMNS; ++j) {
                            BattlefieldArray[i][j] = ExplosionGame.InitialArray[i][j]; //Заполняем массив ландшафта
                            AdditionalBonusArray[i][j] = ExplosionGame.InitialArrayBonuses[i][j]; //Заполняем дополнительный массив бонусов
                        }
                    }
                    break;
                }
            }
        }
        BattlefieldTexturesInit();
        ExplosionExists = false;
    }

    //Метод создания лабиринта-игрового поля
    private void CreateBattlefield() {
        //Предварительная подготовка массива перед генерированием лабиринта. Создание "решетки".
        for(int i = 0; i < COUNT_OF_ROWS; ++i) {
            for(int j = 0; j < COUNT_OF_COLUMNS; ++j) {
                if(
                        (i % 2 != 0) && (j % 2 != 0) && //Если ячейка имеет нечетные индексы i и j
                                (i < COUNT_OF_ROWS - 2) && (j < COUNT_OF_COLUMNS - 2) && //и если не является краевой
                                (i > 1) && (j > 1)) {

                    BattlefieldArray[i][j] = TypeOfField.EMPTY; //Делаем такую ячейку пустой, свободной для прохождения

                } else {
                    BattlefieldArray[i][j] = TypeOfField.WALL; //Иначе возводим непроходимое препятствие - стену
                }
                BombArray[i][j] = TypeOfField.EMPTY; //Массив бомб изначально пуст
                ExplosionArray[i][j] = TypeOfField.EMPTY; //Массив взрывов изначально пуст
                BonusArray[i][j] = TypeOfField.EMPTY; //Массив бонусов изначально пуст
            }
        }
        //Генерирование лабиринта
        Stack<Vector2> AddStack = new Stack<Vector2>(); //Создание стека ячеек, у которых будем искать ближайших соседей
        Vector2 NeighbourArray[] = new Vector2[4]; //Создаем массив соседей
        for(int i = 0; i < NeighbourArray.length; ++i) {
            NeighbourArray[i] = new Vector2();
        }
        int NeighbourNumber; //Количеество соседей
        Vector2 CurrentCell = new Vector2(); //Текущая клетка с координатами x и y
        CurrentCell.set(3, 3); //Присвоение текущей клетке статуса начальной через установку координат
        Vector2 NeighbourCell = new Vector2(); //Соседняя клетка с координатами x и y
        do{
            //Процесс поиска соседей клетки и заполнения соответствующего массива соседей
            NeighbourNumber = 0; // Обнуление счетчика соседей
            //Если сверху пустая клетка, тогда...
            if(BattlefieldArray[(int)CurrentCell.x][(int)CurrentCell.y - 2] == TypeOfField.EMPTY) {
                NeighbourArray[NeighbourNumber++].set(CurrentCell.x, CurrentCell.y - 2);
            }
            //Если справа пустая клетка, тогда...
            if(BattlefieldArray[(int)CurrentCell.x + 2][(int)CurrentCell.y] == TypeOfField.EMPTY) {
                NeighbourArray[NeighbourNumber++].set(CurrentCell.x + 2, CurrentCell.y);
            }
            //Если снизу пустая клетка, тогда...
            if(BattlefieldArray[(int)CurrentCell.x][(int)CurrentCell.y + 2] == TypeOfField.EMPTY) {
                NeighbourArray[NeighbourNumber++].set(CurrentCell.x, CurrentCell.y + 2);
            }
            //Если слева пустая клетка, тогда...
            if(BattlefieldArray[(int)CurrentCell.x - 2][(int)CurrentCell.y] == TypeOfField.EMPTY) {
                NeighbourArray[NeighbourNumber++].set(CurrentCell.x - 2, CurrentCell.y);
            }
            //Если у клетки ещё есть свободные и непосещенные соседи, тогда...
            if(NeighbourNumber > 0) {
                //Выбираем случайного соседа из имеющихся и запоминаем эту ячейку
                int rnd = new Random().nextInt(NeighbourNumber);
                NeighbourCell.set(NeighbourArray[rnd].x, NeighbourArray[rnd].y);
                //Кладем данные о текущей ячейке в стек
                AddStack.push(new Vector2());
                AddStack.peek().set(CurrentCell.x, CurrentCell.y);
                //Убираем стенку между текущей и запомненной соседней ячейкой
                BattlefieldArray[(int)((CurrentCell.x + NeighbourCell.x) / 2)][(int)((CurrentCell.y + NeighbourCell.y) / 2)] = TypeOfField.EMPTY;
                //Обозначаем текущую ячейку посещенной (VISITED)
                BattlefieldArray[(int)CurrentCell.x][(int)CurrentCell.y] = TypeOfField.VISITED;
                //Передвигаем текущую ячейку на место соседней
                CurrentCell.set(NeighbourCell.x, NeighbourCell.y);
            } else if(AddStack.size() > 0) { //Если соседей свободных нет, но при этом в стеке еще есть ячейки, тогда...
                BattlefieldArray[(int)CurrentCell.x][(int)CurrentCell.y] = TypeOfField.VISITED; //Обозачаем последнюю ячейку посещенной
                CurrentCell = AddStack.pop(); //Убираем из стека последнюю заполненную ячейку, делая её текущей ячейкой
            } else { //Если же мы не имеем уже стека, соседей никаких рядом нет, но остались какие-то изолированные области и точки, тогда...
                boolean EmptyIsFound = false; //Переменная-индикатор того, что нашли изолированную точку
                for(int i = 0; i < COUNT_OF_ROWS; ++i) { //Пробегаемся по первоначальной решетке игрового поля, чтобы найти изолированную точку
                    for(int j = 0; j < COUNT_OF_COLUMNS; ++j) {
                        if(
                                (i % 2 != 0) && (j % 2 != 0) && //Если ячейка имеет нечетные индексы i и j
                                        (i < COUNT_OF_ROWS - 2) && (j < COUNT_OF_COLUMNS - 2) && //и если не является краевой
                                        (i > 1) && (j > 1) &&
                                        BattlefieldArray[i][j] == TypeOfField.EMPTY) { //Если нашли такую свобоную ячейку на решетке, тогда...
                            BattlefieldArray[(int)CurrentCell.x][(int)CurrentCell.y] = TypeOfField.VISITED; //Обозачаем ячейку посещенной
                            CurrentCell.set(i,j); //Текущую ячейку делаем с такими же координатами
                            EmptyIsFound = true; //Подсвечиваем индикатор
                            break; //Выходим из цикла
                        }
                    }
                    if(EmptyIsFound) break; //Выходим из цикла
                }
                if(!EmptyIsFound) break; //Если ничего не нашли, тогда заканчиваем программу по основной части генерирования лабиринта
            }
        } while(true);
        //Окончательная обработка массива
        Random rnd_textures = new Random();
        for(int i = 3; i < COUNT_OF_ROWS - 3; ++i) { //Обработка основного игрового поля
            for(int j = 3; j < COUNT_OF_COLUMNS - 3; ++j) {
                //Все "посещенные" клетки заменяем на просто "пустые"
                if(BattlefieldArray[i][j] == TypeOfField.VISITED) {
                    BattlefieldArray[i][j] = TypeOfField.EMPTY;
                }
                else if(BattlefieldArray[i][j] == TypeOfField.WALL){ //Если стена, тогда нужно заменить на другую текстуру пейзажа
                    double r = (rnd_textures.nextInt(100)+1) / 100.0; //Генерирование случайной величины от 0 до 1
                    if(r <= 0.6) BattlefieldArray[i][j] = TypeOfField.TREE; //Вероятность дерева 60%
                    else if(r <= 0.9) BattlefieldArray[i][j] = TypeOfField.ROCK; //Вероятность камня 30%
                    else BattlefieldArray[i][j] = TypeOfField.WATER; //Вероятность воды 10%
                }
            }
        }
        //Добавление стартовых площадок
        //Левый верхний угол
        for(int i = 1; i < 4; ++i) {
            for(int j = 1; j < 4; ++j) {
                BattlefieldArray[i][j] = TypeOfField.EMPTY;
            }
        }
        BattlefieldArray[2][2] = TypeOfField.ROCK;
        //Левый нижний угол
        for(int i = 1; i < 4; ++i) {
            for(int j = COUNT_OF_ROWS - 4; j < COUNT_OF_ROWS - 1; ++j) {
                BattlefieldArray[i][j] = TypeOfField.EMPTY;
            }
        }
        BattlefieldArray[2][COUNT_OF_ROWS - 3] = TypeOfField.ROCK;
        //Правый верхний угол
        for(int i = COUNT_OF_COLUMNS - 4; i < COUNT_OF_COLUMNS - 1; ++i) {
            for(int j = 1; j < 4; ++j) {
                BattlefieldArray[i][j] = TypeOfField.EMPTY;
            }
        }
        BattlefieldArray[COUNT_OF_COLUMNS - 3][2] = TypeOfField.ROCK;
        //Правый нижний угол
        for(int i = COUNT_OF_COLUMNS - 4; i < COUNT_OF_COLUMNS - 1; ++i) {
            for(int j = COUNT_OF_ROWS - 4; j < COUNT_OF_ROWS - 1; ++j) {
                BattlefieldArray[i][j] = TypeOfField.EMPTY;
            }
        }
        BattlefieldArray[COUNT_OF_COLUMNS - 3][COUNT_OF_ROWS - 3] = TypeOfField.ROCK;

    }

    //Метод создания поля для дополнительного массива с бонусами (AdditionalBonusArray)
    private void CreateAdditionalBonusArray() {
        rnd_for_bonus = new Random();
        for(int i = 0; i < COUNT_OF_ROWS; ++i) {
            for(int j = 0; j < COUNT_OF_COLUMNS; ++j) {

                if(BattlefieldArray[i][j] == TypeOfField.TREE) { //Если на данном месте стоит дерево, то под ним может быть бонус
                    if(rnd_for_bonus.nextInt(100) < 40) { //С вероятностью 60% - у нас бонус в этом месте
                        //В данном месте мы уже знаем, что будет бонус. Но не знаем какой именно. Сгенерируем случайную величину, чтобы определить это.
                        int int_rnd_for_bonus = rnd_for_bonus.nextInt(100); //Случайное число от 0 до 99 - как вероятность в процентах
                        if(int_rnd_for_bonus < 30) { //С вероятностью в 30%
                            AdditionalBonusArray[i][j] = TypeOfField.STRENGTH; //Помечаем, что в данной ячейке находится бонус усиления взрыва
                        } else if(int_rnd_for_bonus < 70) { //С вероятностью в (70% - 30%) = 40%
                            AdditionalBonusArray[i][j] = TypeOfField.RESERVE; //Помечаем, что в данной ячейке находится бонус увеличения запасов бомб
                        } else if(int_rnd_for_bonus < 90) {//С вероятностью в (90% - 70%) = 20%
                            AdditionalBonusArray[i][j] = TypeOfField.HEALTH; //Помечаем, что в данной ячейке находится бонус восстановления здоровья персонажа
                        }
                        else { //С вероятностью в (100% - 90%) = 10%
                            AdditionalBonusArray[i][j] = TypeOfField.EXTRALIFE; //Помечаем, что в данной ячейке находится бонус дополнительной жизни
                        }

                    } else { //Если не выпала вероятность бонуса, тогда ставим "пустую бонусную" клетку
                        AdditionalBonusArray[i][j] = TypeOfField.EMPTY_BONUS;
                    }
                } else { //Если на данном месте не дерево, тогда и бонуса в этой ячейке быть не может - ставим "пустую бонусную" клетку
                    AdditionalBonusArray[i][j] = TypeOfField.EMPTY_BONUS;
                }

            }
        }
    }

    //Первоначальное установление текстур для игрового поля
    private void BattlefieldTexturesInit() {

        font_leftside = new Texture("font_leftside.png");
        font_animation = new Animation(new TextureRegion(font_leftside), 1, 0.5f);

        textureEMPTY = new Texture("texture_grass.png");
        textureWALL = new Texture("texture_wall.png");
        textureTREE = new Texture("texture_tree.png");
        textureROCK = new Texture("texture_rock.png");
        textureWATER = new Texture("texture_water.png");
        textureSTRENGTH = new Texture("texture_strength.png");
        textureEXTRALIFE = new Texture("texture_extralife.png");
        textureRESERVE = new Texture("texture_reserve.png");
        textureHEALTH = new Texture("texture_health.png");

        BattlefieldAnimation = new Animation[COUNT_OF_ROWS][COUNT_OF_COLUMNS];
        BonusAnimation = new Animation[COUNT_OF_ROWS][COUNT_OF_COLUMNS];
        boolBonusArray = new boolean[COUNT_OF_ROWS][COUNT_OF_COLUMNS];
        for(int i = 0; i < COUNT_OF_ROWS; ++i) {
            for(int j = 0; j < COUNT_OF_COLUMNS; ++j) {

                boolBonusArray[i][j] = false;

                switch(BattlefieldArray[i][j]) {
                    case EMPTY:
                        BattlefieldAnimation[i][j] = new Animation(new TextureRegion(textureEMPTY), 2, 0.5f);
                        break;
                    case WALL:
                        BattlefieldAnimation[i][j] = new Animation(new TextureRegion(textureWALL), 1, 0.5f);
                        break;
                    case TREE:
                        BattlefieldAnimation[i][j] = new Animation(new TextureRegion(textureTREE), 3, 0.5f);
                        break;
                    case ROCK:
                        BattlefieldAnimation[i][j] = new Animation(new TextureRegion(textureROCK), 1, 0.5f);
                        break;
                    case WATER:
                        BattlefieldAnimation[i][j] = new Animation(new TextureRegion(textureWATER), 1, 0.5f);
                        break;
                }

            }
        }
    }

    //Пустое ли поле?
    public boolean isEMPTY(int i, int j) {
        if(BattlefieldArray[i][j] == TypeOfField.EMPTY)
            return true;
        else return false;
    }

    //Установить ячейку как пустую (для поля бомб)
    public void setEmptyToField(int i, int j) {
        BombArray[i][j] = TypeOfField.EMPTY;
    }

    //Есть ли на данной ячейке поля бомба (для поля бомб)?
    public boolean isBOMB(int i, int j) {
        if(BombArray[i][j] == TypeOfField.BOMB)
            return true;
        else return false;
    }

    //Установить ячейку как ту, на которой лежит бомба (для поля бомб)
    public void setBombToField(int i, int j) {
        BombArray[i][j] = TypeOfField.BOMB;
    }

    //Есть ли на данной ячейке поля взрыв (для поля взрывов)?
    public boolean isExplosion(int i, int j) {
        if(ExplosionArray[i][j] == TypeOfField.EXPLOSION)
            return true;
        else return false;
    }

    //Установить ячейку как ту, на которой имеется взрыв (для поля взрывов)
    public void setExplosionToField(int i, int j, int strength, boolean ArrayOfDamagedRegion[][]) {
        //Сама клетка-очаг взрыва
        ExplosionExists = true; //Оповещаем игровое поле, что где-то на карте взрывов мы поставили взрыв
        ExplosionArray[i][j] = TypeOfField.EXPLOSION; //Взрываем текущую клетку
        BattlefieldAnimation[i][j].updateBattlefield(); //Обновляем анимацию - делаем выженную землю
        //Взрывная волна
        for(int c = 0; c < 4; ++c) { //Бежим по всем четырем направлениям взрыва, имитируя взрывную волну
            int ii, jj;
            int a1 = 0, a2 = 0, a3 = 0, b1 = 0, b2 = 0, b3 = 0;
            switch(c) {
                case 0: a1 = 1; a2 = 0; a3 = 0; b1 = 1; b2 = 0; b3 = 0; break;
                case 1: a1 = 0; a2 = 1; a3 = 0; b1 = 1; b2 = 0; b3 = 0; break;
                case 2: a1 = 0; a2 = 0; a3 = 1; b1 = 0; b2 = 1; b3 = 0; break;
                case 3: a1 = 0; a2 = 0; a3 = 1; b1 = 0; b2 = 0; b3 = 1; break;
            }
            boolean flag = false; //Флаг для остановки движения взрыва, если на пути скала, стена или дерево
            for(int g = 1; g <= strength; ++g) {
                ii = i * b1 + Math.min(i + g , COUNT_OF_ROWS) * b2 + Math.max(i - g , 0) * b3;
                jj = Math.max(j - g, 0) * a1 + Math.min(j + g, COUNT_OF_COLUMNS) * a2 + j * a3;
                switch(BattlefieldArray[ii][jj]) {
                    case EMPTY:
                        ExplosionArray[ii][jj] = TypeOfField.EXPLOSION;
                        break;
                    case WATER:
                        ExplosionArray[ii][jj] = TypeOfField.EXPLOSION;
                        break;
                    case WALL:
                    case ROCK:
                        flag = true;
                        break;
                    case TREE:
                        ExplosionArray[ii][jj] = TypeOfField.EXPLOSION;
                        BattlefieldArray[ii][jj] = TypeOfField.EMPTY;
                        BattlefieldAnimation[ii][jj].updateBattlefield(); //Обновляем анимацию - убираем дерево
                        //Работа с бонусами
                        if(AdditionalBonusArray[ii][jj] != TypeOfField.EMPTY_BONUS) { //Если на данной ячейке бонус (то есть клетка не является пустой) тогда...
                            boolBonusArray[ii][jj] = true; //Делаем пометку, что на данный момент статус этой ячейки - бонус
                            BonusArray[ii][jj] = AdditionalBonusArray[ii][jj]; //Передаем информацию из дополнительного массива бонусов в основной
                            AdditionalBonusArray[ii][jj] = TypeOfField.EMPTY_BONUS; //"Обнуляем" ячейку дополнительного массива
                            switch(BonusArray[ii][jj]) { //Работа с подгрузкой текстур-анимаций для очередного бонуса
                                case STRENGTH: //Добавляем анимацию для бонуса усиления взрыва
                                    BonusAnimation[ii][jj] = new Animation(new TextureRegion(textureSTRENGTH), 10, 2);
                                    break;
                                case EXTRALIFE: //Добавляем анимацию для бонуса дополнительной жизни
                                    BonusAnimation[ii][jj] = new Animation(new TextureRegion(textureEXTRALIFE), 32, 2);
                                    break;
                                case RESERVE: //Добавляем анимацию для бонуса увеличения количества бомб
                                    BonusAnimation[ii][jj] = new Animation(new TextureRegion(textureRESERVE), 5, 2);
                                    break;
                                case HEALTH: //Добавляем анимацию для бонуса восстановления здоровья
                                    BonusAnimation[ii][jj] = new Animation(new TextureRegion(textureHEALTH), 6, 2);
                                    break;
                            }
                        }
                        flag = true;
                        break;
                }
                if(ExplosionArray[ii][jj] == TypeOfField.EXPLOSION) {
                    ArrayOfDamagedRegion[c][g - 1] = true;
                }
                if(flag) {
                    break;
                }
            }
        }
    }

    //Получить количество "строк" игрового поля
    public static int getCountOfRows() {
        return COUNT_OF_ROWS;
    }

    //Получить количество "столбцов" игрового поля
    public static int getCountOfColumns() {
        return COUNT_OF_COLUMNS;
    }

    //Получить кадр боевого поля для анимации на экране
    public TextureRegion getBattleField(int i, int j) {
        return BattlefieldAnimation[i][j].getFrame();
    }

    //Получить кадр бонуса для анимации на экране
    public TextureRegion getBonusField(int i, int j) {
        return BonusAnimation[i][j].getFrame();
    }

    //Обновить анимацию бонуса
    public void updateBonus(int i, int j) {
        BonusAnimation[i][j].update(0.02f);
    }

    //Есть ли на данной ячейке поля бонус?
    public boolean isBonus(int i, int j) {
        return boolBonusArray[i][j];
    }

    //Метод по обновлению поля бонусов, если герой берет бонус. Вызывается из класса Character
    public void takeBonus(int i, int j, Character character) {
        switch(BonusArray[i][j]) { //Определяем, какой бонус лежит на данной ячейке
            case STRENGTH:
                int stren = character.getBombReserve(0).getStrength();
                for(int ii = 0; ii < character.getReserveMax(); ++ii) { //Бежим по всем бомбам персонажа (даже по потенциальным) и увеличиваем радиус атаки
                    character.getBombReserve(ii).setStrength(stren + 1);
                }
                ExplosionGame.strength_sound.play(); //Звук получения бонуса силы
                break;
            case RESERVE:
                character.setReserve(character.getReserve() + 1);
                for(int ii = 0; ii < character.getReserveMax(); ++ii) { //Где-то есть "слабое место" в коде - пытаемся устранить "баг"
                    character.getBombReserve(ii).setStrength(character.getBombReserve(0).getStrength()); //Для всех бомб переписываем силу взрыва
                }
                ExplosionGame.reserve_sound.play(); //Звук получения бонуса увеличения запаса бомб
                break;
            case HEALTH:
                character.setHealth(character.getHealth() + 1); //Добавляем одну единицу здоровья
                ExplosionGame.MassivOfHealth[character.getPlayerId() - 1] = character.getHealth();
                ExplosionGame.health_sound.play(); //Звук получения бонуса здоровья
                break;
            case EXTRALIFE:
                character.setLife(character.getLife() + 1); //Добавляем одну жизнь
                character.setHealth(character.getHealth() + 1); //Делаем эту жизнь здоровой
                ExplosionGame.MassivOfHealth[character.getPlayerId() - 1] = character.getHealth();
                ExplosionGame.extralife_sound.play(); //Звук получения бонуса доп.жизни
                break;
        }
        BonusArray[i][j] = TypeOfField.EMPTY; //После взятия бонуса, делаем ячейку поля бонусов пустым
        boolBonusArray[i][j] = false; //Статус ячейки - нет бонуса теперь, т.к. взяли его
    }

    //Получить кадр бокового фона для анимации на экране
    public TextureRegion getfont() {
        return font_animation.getFrame();
    }

    //Обновить боевое поле
    public void update() {
        if(ExplosionExists) {
            for(int i = 0; i < COUNT_OF_ROWS; ++i) {
                for (int j = 0; j < COUNT_OF_COLUMNS; ++j) {
                    ExplosionArray[i][j] = TypeOfField.EMPTY; //Очистить массив взрывов
                }
            }
            ExplosionExists = false;
        }
    }

    //Освобождение ресурсов после использования
    public void dispose() {

        font_leftside.dispose();
        textureEMPTY.dispose();
        textureWALL.dispose();
        textureTREE.dispose();
        textureROCK.dispose();
        textureWATER.dispose();
        textureSTRENGTH.dispose();
        textureEXTRALIFE.dispose();
        textureRESERVE.dispose();
        textureHEALTH.dispose();

    }

}