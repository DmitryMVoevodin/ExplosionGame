package com.vdm.game;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Копирование информации с Activity для сетевых настроек
		String ClientIP = ConnectionMenu.ClientIP; //Получение ip-адреса клиента
		String ServerIP = ConnectionMenu.ServerIP; //Получение ip-адреса сервера
		boolean isServer = ConnectionMenu.isServer; //Получение информации о том, должны ли мы содержать серверную часть
		int NoP = ConnectionMenu.NoP; //Получение количества игроков
		int TypeOfTexture = ConnectionMenu.TypeOfTexture; //Получить тип игрока (его текстуру)

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration(); //Создание переменной конфигурации
		//Энергосберегающие мероприятия через созданную переменную конфигурации
		config.useCompass = false; //Отключение компаса для экономии заряда батареи
		config.useAccelerometer = false; //Отключение акселерометра для экономии заряда батареи

		//Запуск игры с сетевыми настройками и выбранной энергосберегающей конфигурацией
		initialize(new ExplosionGame(ClientIP, ServerIP, isServer, NoP, TypeOfTexture), config);
	}
}
