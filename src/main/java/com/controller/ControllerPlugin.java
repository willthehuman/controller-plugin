package com.controller;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.events.CanvasSizeChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ContainableFrame;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

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
		if(client.isResized()){
			return client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA);
		}
		return fixedWidget;
	}

	public Widget getInventoryDrawWidget(){
		Widget fixedWidget = client.getWidget(WidgetInfo.FIXED_VIEWPORT_INVENTORY_CONTAINER);
		if(client.isResized()){
			return client.getWidget(WidgetInfo.RESIZABLE_VIEWPORT_INVENTORY_PARENT);
		}
		return fixedWidget;
	}

	public Widget getMainWidget(){
		Widget fixedWidget = client.getWidget(WidgetInfo.FIXED_VIEWPORT);
		if(client.isResized()){
			return client.getWidgetRoots()[0];
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

	protected void hideWidgets(boolean hide)
	{
		/*// hiding in fixed mode does not actually hide stuff and might break stuff so let's not do that
		if (hide && !client.isResized())
		{
			hideWidgets(false);
		}
		else
		{
			clientThread.invokeLater(() ->
			{
				// modern resizeable
				Widget root = client.getWidget(164, 65);
				if (root != null)
					hideWidgetChildren(root, hide);

				// classic resizeable
				root =  client.getWidget(161, 33);
				if (root != null)
					hideWidgetChildren(root, hide);

				// fix zoom modern resizeable
				// zoom is child widget with the id 2 but if the parent is hidden the child is too
				Widget zoom = client.getWidget(161, 90);
				if (zoom != null)
					zoom.setHidden(false);

				// fix zoom classic resizeable
				// zoom is child widget with the id 2 but if the parent is hidden the child is too
				zoom = client.getWidget(164, 87);
				if (zoom != null)
					zoom.setHidden(false);
			});
		}*/

	}
}
