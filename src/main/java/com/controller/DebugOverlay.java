package com.controller;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;

import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.InputEvent;

import org.libsdl.SDL;
import uk.co.electronstudio.sdl2gdx.*;

public class DebugOverlay extends Overlay {

    private final Client client;
    private Player player;
    private final ControllerPlugin plugin;
    private SDL2ControllerManager controllerManager;

    private Point leftJoystickPoint;
    private Robot robot;

    private Boolean lockMouse;
    @Inject
    private DebugOverlay(Client client, ControllerPlugin plugin)
    {
        this.client = client;
        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);

        controllerManager = new SDL2ControllerManager();
        lockMouse = true;

        try {
            robot = new Robot();
        } catch (AWTException e) {
            plugin.externalLog(e.getMessage());
        }
    }

    private Player getPlayer(){
        if (client.getLocalPlayer() == null)
        {
            return null;
        }

        if(player == null){
            player = client.getLocalPlayer();
        }

        return player;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        processInput();

        if (getPlayer() != null) {
            drawLocalPoint(graphics, getPlayer().getLocalLocation(), Color.CYAN);
        }

        if(leftJoystickPoint != null){
            drawPoint(graphics, leftJoystickPoint, Color.RED);
        }

        return null;
    }

    private void drawLocalPoint(Graphics2D graphics, LocalPoint position, Color color)
    {
        final Point point = localToScene(position);

        if (point == null)
        {
            return;
        }

        graphics.setColor(color);
        graphics.setStroke(new BasicStroke((float) 2));
        graphics.drawOval(point.getX(), point.getY(), 10, 10);
    }

    private void drawPoint(Graphics2D graphics, Point point, Color color)
    {
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke((float) 2));
        graphics.drawOval(point.getX(), point.getY(), 10, 10);
    }

    private Point localToScene(LocalPoint location)
    {
        if (location == null)
        {
            return null;
        }

        return Perspective.localToCanvas(
                client,
                new LocalPoint(location.getX(), location.getY()),
                client.getPlane());
    }

    private void processInput() {
        if(controllerManager.getControllers().size == 0){
            plugin.externalLog("No controller connected!");
            return;
        }

        try {
            controllerManager.pollState();
        } catch (Exception exception) {
            plugin.externalLog(exception.getMessage());
        }

        SDL2Controller controller = (SDL2Controller) controllerManager.getControllers().get(0);
        //plugin.externalLog("leftx : " + controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTX) + "lefty : " + controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTY));

        if(getPlayer() != null && lockMouse){
            leftJoystickPoint = new Point((int)(localToScene(getPlayer().getLocalLocation()).getX() + controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTX)*client.getViewportWidth()/2), (int)(localToScene(getPlayer().getLocalLocation()).getY() + controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTY)*client.getViewportHeight()/2));
            robot.mouseMove(leftJoystickPoint.getX(), leftJoystickPoint.getY());
        }

        if(controller.getButton(SDL.SDL_CONTROLLER_BUTTON_A)){
            plugin.externalLog("A");
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }

        if(controller.getButton(SDL.SDL_CONTROLLER_BUTTON_B)){
            plugin.externalLog("B");
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        }

        if(controller.getButton(SDL.SDL_CONTROLLER_BUTTON_START)){
            plugin.externalLog("START");
            lockMouse = !lockMouse;
        }

        /*try {
            for (int i = 0; i < controller.joystick.numAxes(); i++) {
                plugin.externalLog("Axis #" + i + " : " + SDL.SDL_GameControllerGetStringForAxis(i));
            }
        } catch (Exception exception){
            plugin.externalLog(exception.getMessage());
        }*/
    }
}
