package com.controller;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.ContainableFrame;
import net.runelite.client.ui.overlay.Overlay;

import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

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

    boolean aButtonPressed = false;
    boolean bButtonPressed = false;
    boolean startButtonPressed = false;

    @Inject
    private DebugOverlay(Client client, ControllerPlugin plugin)
    {
        this.client = client;
        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);

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

        // draw point on player
        if (getPlayer() != null) {
            //drawLocalPoint(graphics, getPlayer().getLocalLocation(), Color.CYAN);
        }

        // draw joystick pos
        if(leftJoystickPoint != null && lockMouse){
            moveMouseToPoint(leftJoystickPoint, new java.awt.Point((int) (plugin.getWindow().getLocation().getX() + client.getCanvas().getX()), (int) (plugin.getWindow().getLocation().getY() + client.getCanvas().getY() + 25)));
            drawPoint(graphics, leftJoystickPoint, Color.RED);
        }

        // draw squares
        drawSquare(graphics, plugin.getMinimapDrawWidget().getCanvasLocation().getX(), plugin.getMinimapDrawWidget().getCanvasLocation().getY(), plugin.getMinimapDrawWidget().getWidth(), plugin.getMinimapDrawWidget().getHeight(), Color.red);
        drawSquare(graphics, getInventoryAndTabsLocation().getX(), getInventoryAndTabsLocation().getY(), getInventoryAndTabsWidth(), getInventoryAndTabsHeight(), Color.yellow);
        drawSquare(graphics, plugin.getMainWidget().getCanvasLocation().getX(), plugin.getMainWidget().getCanvasLocation().getY(), plugin.getMainWidget().getWidth(), plugin.getMainWidget().getHeight(), Color.green);

        return null;
    }

    private Point getInventoryAndTabsLocation(){
        Widget minimap = plugin.getMinimapDrawWidget();
        return new Point(minimap.getCanvasLocation().getX(), minimap.getCanvasLocation().getY() + minimap.getHeight());
    }

    private int getInventoryAndTabsWidth(){
        Widget minimap = plugin.getMinimapDrawWidget();
        return minimap.getWidth();
    }

    private int getInventoryAndTabsHeight(){
        return client.getCanvasHeight() - getInventoryAndTabsLocation().getY();
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

    private void drawLine(Graphics2D graphics,Point start, Point end, Color color){
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke((float) 1));
        graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
    }

    private void drawSquare(Graphics2D graphics, int x, int y, int width, int height, Color color){
        Point topLeft = new Point(x, y);
        Point topRight = new Point(x + width, y);
        Point bottomLeft = new Point(x, y + height);
        Point bottomRight = new Point(x + width, y + height);

        drawLine(graphics, topLeft, topRight, color);
        drawLine(graphics, topLeft, bottomLeft, color);
        drawLine(graphics, bottomLeft, bottomRight, color);
        drawLine(graphics, topRight, bottomRight, color);
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
            if(controller.getButton(SDL.SDL_CONTROLLER_BUTTON_LEFTSHOULDER)){
                leftJoystickPoint = calculateJoystickPos(controller, plugin.getMinimapDrawWidget());
            } else if (controller.getButton(SDL.SDL_CONTROLLER_BUTTON_RIGHTSHOULDER)){
                leftJoystickPoint = calculateJoystickPos(controller, getInventoryAndTabsWidth(), getInventoryAndTabsHeight(), getInventoryAndTabsLocation());
            } else {
                leftJoystickPoint = calculateJoystickPos(controller, plugin.getMainWidget());
            }
        }

       // handleCameraMovement(controller);

        if (controller.getButton(SDL.SDL_CONTROLLER_BUTTON_A)) {
            if (!aButtonPressed) {
                plugin.externalLog("A Pressed");
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                aButtonPressed = true;
            }
        } else {
            if (aButtonPressed) {
                plugin.externalLog("A Released");
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                aButtonPressed = false;
            }
        }

        if (controller.getButton(SDL.SDL_CONTROLLER_BUTTON_B)) {
            if (!bButtonPressed) {
                plugin.externalLog("B Pressed");
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                bButtonPressed = true;
            }
        } else {
            if (bButtonPressed) {
                plugin.externalLog("B Released");
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                bButtonPressed = false;
            }
        }

        if (controller.getButton(SDL.SDL_CONTROLLER_BUTTON_START)) {
            if (!startButtonPressed) {
                plugin.externalLog("START Pressed");
                lockMouse = !lockMouse;
                startButtonPressed = true;
            }
        } else {
            startButtonPressed = false;
        }

        /*try {
            for (int i = 0; i < controller.joystick.numAxes(); i++) {
                plugin.externalLog("Axis #" + i + " : " + SDL.SDL_GameControllerGetStringForAxis(i));
            }
        } catch (Exception exception){
            plugin.externalLog(exception.getMessage());
        }*/
    }

    private void moveMouseToPoint(Point targetPoint, java.awt.Point viewportOffset) {
        // Get the current mouse position
        java.awt.Point currentMouse = MouseInfo.getPointerInfo().getLocation();

        // Calculate the difference between current and target points
        int deltaX = (int) (targetPoint.getX() - currentMouse.x + viewportOffset.getX());
        int deltaY = (int) (targetPoint.getY() - currentMouse.y + viewportOffset.getY());

        // Move the mouse using Robot
        robot.mouseMove(currentMouse.x + deltaX, currentMouse.y + deltaY);
    }

    private void handleCameraMovement(SDL2Controller controller) {
        double xJoy = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_RIGHTX);
        double yJoy = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_RIGHTY);

        // Release all keys initially
        robot.keyRelease(KeyEvent.VK_RIGHT);
        robot.keyRelease(KeyEvent.VK_LEFT);
        robot.keyRelease(KeyEvent.VK_UP);
        robot.keyRelease(KeyEvent.VK_DOWN);

        // Handle x-axis movement
        if (xJoy > 0.5) {
            robot.keyPress(KeyEvent.VK_RIGHT);
        } else if (xJoy < 0.5) {
            robot.keyPress(KeyEvent.VK_LEFT);
        }

        // Handle y-axis movement
        if (yJoy > 0.5) {
            robot.keyPress(KeyEvent.VK_UP);
        } else if (yJoy < 0.5) {
            robot.keyPress(KeyEvent.VK_DOWN);
        }
    }

    private Point calculateJoystickPos(SDL2Controller controller, Widget parent) {
        // Joystick values ranging from -1 to 1
        double xJoy = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTX);
        double yJoy = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTY);

        // Window dimensions
        double windowWidth = parent.getWidth();
        double windowHeight = parent.getHeight();

        // Centered point within the window
        double xPoint = parent.getCanvasLocation().getX() + windowWidth / 2;
        double yPoint = parent.getCanvasLocation().getY() + windowHeight / 2;

        // Calculate the resulting point
        double xResult = xPoint + xJoy * windowWidth;
        double yResult = yPoint + yJoy * windowHeight;

        // Clamp the resulting point within the window boundaries
        xResult = Math.min(parent.getCanvasLocation().getX() + windowWidth, Math.max(parent.getCanvasLocation().getX(), xResult));
        yResult = Math.min(parent.getCanvasLocation().getY() + windowHeight, Math.max(parent.getCanvasLocation().getY(), yResult));

        return new Point((int) xResult, (int) yResult);
    }

    private Point calculateJoystickPos(SDL2Controller controller, int width, int height, Point location) {
        // Joystick values ranging from -1 to 1
        double xJoy = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTX);
        double yJoy = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTY);

        // Centered point within the window
        double xPoint = location.getX() + (double) width / 2;
        double yPoint = location.getY() + (double) height / 2;

        // Calculate the resulting point
        double xResult = xPoint + xJoy * width;
        double yResult = yPoint + yJoy * height;

        // Clamp the resulting point within the window boundaries
        xResult = Math.min(location.getX() + width, Math.max(location.getX(), xResult));
        yResult = Math.min(location.getY() + height, Math.max(location.getY(), yResult));

        return new Point((int) xResult, (int) yResult);
    }
}
