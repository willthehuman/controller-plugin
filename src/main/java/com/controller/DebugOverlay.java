package com.controller;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;

import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;


public class DebugOverlay extends Overlay {

    private final Client client;

    @Inject
    private DebugOverlay(Client client)
    {
        this.client = client;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        drawPoint(graphics);

        return null;
    }

    private void drawPoint(Graphics2D graphics)
    {
        if (client.getLocalPlayer() == null)
        {
            return;
        }

        final Point point = localToScene(client.getLocalPlayer().getLocalLocation());

        if (point == null)
        {
            return;
        }

        graphics.setColor(Color.CYAN);
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

}
