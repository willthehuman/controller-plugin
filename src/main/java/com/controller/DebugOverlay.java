package com.controller;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import org.libsdl.SDL;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class DebugOverlay extends Overlay {

    private final Client client;
    private Player player;
    private final ControllerPlugin plugin;
    private SDL2ControllerManager controllerManager;
    private SDL2Controller controller;
    private Point leftJoystickPoint;
    private Robot robot;

    private Boolean lockMouse;

    boolean rightClickButtonPressed = false;
    boolean leftClickButtonPressed = false;
    boolean toggleButtonPressed = false;
    boolean prayerButtonPressed = false;
    boolean spellsButtonPressed = false;
    boolean combatButtonPressed = false;
    boolean inventoryButtonPressed = false;
    boolean rightStickXActive = false;
    boolean rightStickYActive = false;


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

        // move mouse
        if(leftJoystickPoint != null && lockMouse){
            moveMouseToPoint(leftJoystickPoint, new java.awt.Point((int) (plugin.getWindow().getLocation().getX() + client.getCanvas().getX()), (int) (plugin.getWindow().getLocation().getY() + client.getCanvas().getY() + 25)));
            //drawPoint(graphics, leftJoystickPoint, Color.RED);
        }

        // draw squares around focused panel
        if(controller != null){
            if(controller.getButton(plugin.getControllerConfig().minimapButton().buttonMask)){
                drawSquare(graphics, plugin.getMinimapDrawWidget().getCanvasLocation().getX(), plugin.getMinimapDrawWidget().getCanvasLocation().getY(), plugin.getMinimapDrawWidget().getWidth(), plugin.getMinimapDrawWidget().getHeight(), Color.red);
            } else if (controller.getButton(plugin.getControllerConfig().inventoryButton().buttonMask)){
                if(!client.isResized()){
                    drawSquare(graphics, getInventoryAndTabsLocation().getX(), getInventoryAndTabsLocation().getY(), getInventoryAndTabsWidth(), getInventoryAndTabsHeight(), Color.yellow);
                } else {
                    drawSquare(graphics, plugin.getInventoryDrawWidget().getCanvasLocation().getX(), plugin.getInventoryDrawWidget().getCanvasLocation().getY(), plugin.getInventoryDrawWidget().getWidth(), plugin.getInventoryDrawWidget().getHeight(), Color.yellow);
                }
            } else {
                if(lockMouse)
                    drawSquare(graphics, plugin.getMainWidget().getCanvasLocation().getX(), plugin.getMainWidget().getCanvasLocation().getY(), plugin.getMainWidget().getWidth(), plugin.getMainWidget().getHeight(), Color.green);
            }
        }
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

        controller = (SDL2Controller) controllerManager.getControllers().get(0);

        if(getPlayer() != null && plugin.getControllerConfig() != null && lockMouse){
            if(controller.getButton(plugin.getControllerConfig().minimapButton().buttonMask)){
                leftJoystickPoint = calculateJoystickPos(plugin.getMinimapDrawWidget());
            } else if (controller.getButton(plugin.getControllerConfig().inventoryButton().buttonMask)){
                if(!client.isResized()){
                    leftJoystickPoint = calculateJoystickPos(getInventoryAndTabsWidth(), getInventoryAndTabsHeight(), getInventoryAndTabsLocation());
                } else {
                    leftJoystickPoint = calculateJoystickPos(plugin.getInventoryDrawWidget());
                }
            } else {
                leftJoystickPoint = calculateJoystickPos(plugin.getMainWidget());
            }
        }

        handleCameraMovement();
        handleCameraZoom();

        // Left click
        if (controller.getButton(plugin.getControllerConfig().leftClickButton().buttonMask)) {
            if (!leftClickButtonPressed) {
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                leftClickButtonPressed = true;
            }
        } else {
            if (leftClickButtonPressed) {
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                leftClickButtonPressed = false;
            }
        }

        // Right click
        if (controller.getButton(plugin.getControllerConfig().rightClickButton().buttonMask)) {
            if (!rightClickButtonPressed) {
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                rightClickButtonPressed = true;
            }
        } else {
            if (rightClickButtonPressed) {
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                rightClickButtonPressed = false;
            }
        }

        // Toggler
        if (controller.getButton(plugin.getControllerConfig().toggleButton().buttonMask)) {
            if (!toggleButtonPressed) {
                lockMouse = !lockMouse;
                toggleButtonPressed = true;
            }
        } else {
            toggleButtonPressed = false;
        }

        //Prayer tab
        if (controller.getButton(plugin.getControllerConfig().prayerButton().buttonMask)) {
            if (!prayerButtonPressed) {
                robot.keyPress(KeyEvent.VK_F5);
                prayerButtonPressed = true;
            }
        } else {
            if (prayerButtonPressed) {
                robot.keyRelease(KeyEvent.VK_F5);
                prayerButtonPressed = false;
            }
        }

        //Spells tab
        if (controller.getButton(plugin.getControllerConfig().spellsButton().buttonMask)) {
            if (!spellsButtonPressed) {
                robot.keyPress(KeyEvent.VK_F6);
                spellsButtonPressed = true;
            }
        } else {
            if (spellsButtonPressed) {
                robot.keyRelease(KeyEvent.VK_F6);
                spellsButtonPressed = false;
            }
        }

        //Combat tab
        if (controller.getButton(plugin.getControllerConfig().combatButton().buttonMask)) {
            if (!combatButtonPressed) {
                robot.keyPress(KeyEvent.VK_F1);
                combatButtonPressed = true;
            }
        } else {
            if (combatButtonPressed) {
                robot.keyRelease(KeyEvent.VK_F1);
                combatButtonPressed = false;
            }
        }

        //Inventory tab
        if (controller.getButton(plugin.getControllerConfig().inventoryTabButton().buttonMask)) {
            if (!inventoryButtonPressed) {
                robot.keyPress(KeyEvent.VK_ESCAPE);
                inventoryButtonPressed = true;
            }
        } else {
            if (inventoryButtonPressed) {
                robot.keyRelease(KeyEvent.VK_ESCAPE);
                inventoryButtonPressed = false;
            }
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

    private void handleCameraMovement() {
        double xJoy = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_RIGHTX);
        double yJoy = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_RIGHTY);

        // Handle x-axis movement
        if (xJoy > 0.5) {
            if(!rightStickXActive){
                robot.keyPress(KeyEvent.VK_RIGHT);
                rightStickXActive = true;
            }
        } else if (xJoy < -0.5) {
            if(!rightStickXActive){
                robot.keyPress(KeyEvent.VK_LEFT);
                rightStickXActive = true;
            }
        } else {
            if(rightStickXActive){
                robot.keyRelease(KeyEvent.VK_LEFT);
                robot.keyRelease(KeyEvent.VK_RIGHT);
                rightStickXActive = false;
            }
        }

        // Handle y-axis movement
        if (yJoy > 0.5) {
            if(!rightStickYActive){
                robot.keyPress(KeyEvent.VK_UP);
                rightStickYActive = true;
            }
        } else if (yJoy < -0.5) {
            if(!rightStickYActive){
                robot.keyPress(KeyEvent.VK_DOWN);
                rightStickYActive = true;
            }
        } else {
            if (rightStickYActive) {
                robot.keyRelease(KeyEvent.VK_UP);
                robot.keyRelease(KeyEvent.VK_DOWN);
                rightStickYActive = false;
            }
        }
    }

    private void handleCameraZoom(){
        double zoomOutTrigger = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_TRIGGERLEFT);
        double zoomInTrigger = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_TRIGGERRIGHT);

        // Handle zoom out
        if (zoomOutTrigger > 0.5) {
            robot.mouseWheel(-1);
        }

        // Handle zoom in
        if (zoomInTrigger > 0.5) {
            robot.mouseWheel(1);
        }
    }

    private Point calculateJoystickPos(Widget parent) {
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
        double xResult = xPoint + xJoy * windowWidth / 1.5;
        double yResult = yPoint + yJoy * windowHeight / 1.5;

        // Clamp the resulting point within the window boundaries
        xResult = Math.min(parent.getCanvasLocation().getX() + windowWidth, Math.max(parent.getCanvasLocation().getX(), xResult));
        yResult = Math.min(parent.getCanvasLocation().getY() + windowHeight, Math.max(parent.getCanvasLocation().getY(), yResult));

        return new Point((int) xResult, (int) yResult);
    }

    private Point calculateJoystickPos(int width, int height, Point location) {
        // Joystick values ranging from -1 to 1
        double xJoy = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTX);
        double yJoy = controller.getAxis(SDL.SDL_CONTROLLER_AXIS_LEFTY);

        // Centered point within the window
        double xPoint = location.getX() + (double) width / 2;
        double yPoint = location.getY() + (double) height / 2;

        // Calculate the resulting point
        double xResult = xPoint + xJoy * width / 1.5;
        double yResult = yPoint + yJoy * height / 1.5;

        // Clamp the resulting point within the window boundaries
        xResult = Math.min(location.getX() + width, Math.max(location.getX(), xResult));
        yResult = Math.min(location.getY() + height, Math.max(location.getY(), yResult));

        return new Point((int) xResult, (int) yResult);
    }
}
