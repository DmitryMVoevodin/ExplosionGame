package com.vdm.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.vdm.game.interconnection.Client;
import com.vdm.game.interconnection.Multiplayer;
import com.vdm.game.interconnection.Server;
import com.vdm.game.sprites.Battlefield;
import com.vdm.game.windows.GW_MainMenu;
import com.vdm.game.windows.ManagementSystemOfGameWindows;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class ExplosionGame extends ApplicationAdapter {

    //Музыка и эффекты
    public static Sound     bomb_sound,
                            explosion_sound,
                            health_sound,
                            extralife_sound,
                            reserve_sound,
                            strength_sound;

	//Флаги
	public static Multiplayer.Flag Flag_isReadyToMoveForward; //Флаг-счетчик готовности по вопросу заполнения массива номеров текстур
	public static Multiplayer.Flag Flag_MainMenu_To_Play; //Флаг переключения экрана GW_MainMenu на GW_Play
	public static Multiplayer.Flag Flag_InitialArray; //Флаг, сигнализирующей о готовности клиентов копировать лабиринты
	public static Multiplayer.Flag Flag_ReadyToPlay; //Флаг, сигнализирующий о готовности клиентов начать игру
	public static Multiplayer.Flag Flag_isKilled; //Флаг, сигнализирующий, что игрока убили
	public static Multiplayer.Flag Flag_isReadyToGameOver; //Флаг, сигнализирующий о готовности клиентов к переходу к экрану конца игры
    public static Multiplayer.Flag Flag_End; //Флаг для закрытия игры
    public static Multiplayer.Flag Flag_End2; //Флаг доп. для закрытия игры

	//Часть по взаимодействию с сетью
	public static boolean isServer; //Логическая переменная - является ли данная сторона игры серверной частью?
	public static Server server; //Переменная с потоками серверной части
	public static Client client; //Переменная с потоками клиентской части
	public static String ServerIP; //Переменная с ip-адресом серверной части
	public static String ClientIP; //Переменная с ip-адресом клиентской части
	public static int NoP; //Переменная с количеством игроков

	public static final int HEIGHT = 480; //Высота экрана
	public static final int WIDTH = 800; //Ширина экрана
	public static final String TITLE = "Explosion Game"; //Название игры
	public static final int PORT = 8888; //Порт для связи по сети
	public static int PlayerId; //Номер игрока

	public static Vector2 CursorCoordinates; //Координаты курсора

	public static Battlefield.TypeOfField[][] InitialArray; //Промежуточный массив с ландшафтом лабиринта (нужен при передаче информации через сеть)
	public static Battlefield.TypeOfField[][] InitialArrayBonuses; //Промежуточный массив с бонусами лабиринта (при передаче через сеть)
	public static int InitialArray_i, InitialArray_j; //Переменные-итераторы для заполнения промежуточных массивов ландшафта и бонусов.
	public static void ijIterationOfInitialArray() { //Метод по изменению итераторов и значения шага в флаге Flag_InitialArray
		ExplosionGame.InitialArray_j++;
		if(ExplosionGame.InitialArray_j == Battlefield.getCountOfColumns()) {
			ExplosionGame.InitialArray_j = 0;
			ExplosionGame.InitialArray_i++;
			if(ExplosionGame.InitialArray_i == Battlefield.getCountOfRows()) {
				ExplosionGame.InitialArray_i = 0;
				Flag_InitialArray.IncreaseStep();
			}
		}
	}

	public static int TypeOfTexture; //Номер текстуры игрока
	public static int[] TypeOfTextureMassiv; //Массив с номерами текстур

	public static Queue<String> MessagesQueue2; //Вторая очередь на стороне клиента (используется для удобства при отрисовке)
	public static int PlayerID_tmp; //Временный номер игрока - используется при получении и расшифровке сигналов-сообщений по действиям игроков
	public static char command_tmp; //Временный номер действия игрока - для получения и расшифровки сигналов-сообщений по действиям игроков

	public static int[] MassivOfHealth;

	private SpriteBatch batch; //Нужно для графической отрисовки
	private ManagementSystemOfGameWindows msgw; //Нужно для удобства управления графическими окнами путём создания стека из них

	public ExplosionGame(String _ClientIP, String _ServerIP, boolean _isServer, int _NoP, int _TypeOfTexture) {
		isServer = _isServer;
		ClientIP = _ClientIP;
		ServerIP = _ServerIP;
		NoP = _NoP;
		TypeOfTexture = _TypeOfTexture;
	}

	@Override
	public void create () {

		//Предварительные настройки

        bomb_sound = Gdx.audio.newSound(Gdx.files.internal("bomb.wav"));
        explosion_sound = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
        health_sound = Gdx.audio.newSound(Gdx.files.internal("health.wav"));
        extralife_sound = Gdx.audio.newSound(Gdx.files.internal("extralife.wav"));
        strength_sound = Gdx.audio.newSound(Gdx.files.internal("strength.wav"));
        reserve_sound = Gdx.audio.newSound(Gdx.files.internal("reserve.wav"));

		MessagesQueue2 = new LinkedList<String>();
		Flag_isReadyToMoveForward = new Multiplayer.Flag();
		Flag_MainMenu_To_Play = new Multiplayer.Flag();
		Flag_InitialArray = new Multiplayer.Flag();
		Flag_ReadyToPlay = new Multiplayer.Flag();
		Flag_isKilled = new Multiplayer.Flag();
		Flag_isReadyToGameOver = new Multiplayer.Flag();
		Flag_End = new Multiplayer.Flag();
        Flag_End2 = new Multiplayer.Flag();
		InitialArray = new Battlefield.TypeOfField[Battlefield.getCountOfRows()][Battlefield.getCountOfColumns()];
		InitialArrayBonuses = new Battlefield.TypeOfField[Battlefield.getCountOfRows()][Battlefield.getCountOfColumns()];
		InitialArray_i = 0; InitialArray_j = 0;

		//Часть по взаимодействию с сетью
		server = (isServer) ? (new Server(ServerIP, PORT, NoP)) : null;
		try {
			client = (isServer) ? (new Client(ServerIP, PORT, 1)) : (new Client(ServerIP, PORT, 2));
		} catch (IOException e) {}

		//Часть по взаимодействию с игрой
		batch = new SpriteBatch();
		msgw = new ManagementSystemOfGameWindows();
		Gdx.gl.glClearColor(1, 1, 1, 1); //Установка цвета очистки экрана
		msgw.push(new GW_MainMenu(msgw));
		CursorCoordinates = new Vector2();
		CursorCoordinates.set(0.0f, 0.0f);

	}

	@Override
	public void render () {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); //Очистка экрана цветом, установленным в методе create()
		if(Flag_End.Trigger()) {
		    Flag_End2.setValue(true);
			this.dispose();
		} else {
		    if(!Flag_End.getValue()) {
                msgw.update(Gdx.graphics.getDeltaTime());
                msgw.render(batch);
            }
		}
	}
	
	@Override
	public void dispose () {

	    bomb_sound.dispose();
        explosion_sound.dispose();
        health_sound.dispose();
        extralife_sound.dispose();
        strength_sound.dispose();
        reserve_sound.dispose();
	    batch.dispose();

	}

}
