package com.vdm.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.vdm.game.ExplosionGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = ExplosionGame.WIDTH; //Установка ширины десктопного приложения
		config.height = ExplosionGame.HEIGHT; //Установка высоты десктопного приложения
		config.title = ExplosionGame.TITLE; //Установка заголовка десктопного приложения

		new LwjglApplication(new ExplosionGame("Ura!"), config);
	}
}
