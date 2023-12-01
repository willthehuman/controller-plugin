package com.controller;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.events.CanvasSizeChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.*;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;
import javax.swing.*;

@Slf4j
@PluginDescriptor(
	name = "Controller"
)
public class ControllerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ControllerConfig config;
	@Inject
	private DebugOverlay debugOverlay;

	@Inject
	private OverlayManager overlayManager;

	private GraphicsDevice gd;
	private JRootPane rootPane;
	private ContainableFrame frame;
	private Window window;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Controller plugin started!");

		gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		//get root frame
		Window[] windows = Frame.getWindows();
		//log the window names
		for (Window w : windows) {
			if(w instanceof ContainableFrame){
				window = w;
			}
		}
		frame = (ContainableFrame) window;
		rootPane = ((ContainableFrame) window).getRootPane();

		overlayManager.add(debugOverlay);
	}

	public Widget getMinimapDrawWidget()
	{
		Widget fixedWidget = client.getWidget(WidgetInfo.FIXED_VIEWPORT_MINIMAP);
		if(fixedWidget == null){
			return client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA);
		}
		return fixedWidget;
	}

	public Widget getInventoryDrawWidget(){
		Widget fixedWidget = client.getWidget(WidgetInfo.FIXED_VIEWPORT_INVENTORY_CONTAINER);
		if(fixedWidget == null){
			return client.getWidget(WidgetInfo.RESIZABLE_VIEWPORT_INVENTORY_PARENT);
		}
		return fixedWidget;
	}

	public Widget getMainWidget(){
		Widget fixedWidget = client.getWidget(WidgetInfo.FIXED_VIEWPORT);
		if(fixedWidget == null){
			return client.getWidget(WidgetInfo.MULTICOMBAT_RESIZABLE_CLASSIC);
		}
		return fixedWidget;
	}

	public Rectangle getScreen(){
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	}

	public Window getWindow(){
		return window;
	}

	public void externalLog(String text){
		log.info(text);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Controller plugin stopped!");

		overlayManager.remove(debugOverlay);
	}

	@Subscribe
	public void onGameTick(GameTick gameTick) {

	}

	@Subscribe
	public void onCanvasSizeChanged(CanvasSizeChanged canvasSizeChanged){
	}

	@Provides
	ControllerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ControllerConfig.class);
	}
}
