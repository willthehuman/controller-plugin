package com.controller;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import org.libsdl.SDL;

@ConfigGroup("bindings")
public interface ControllerConfig extends Config
{
	enum Button {
		A(SDL.SDL_CONTROLLER_BUTTON_A),
		B(SDL.SDL_CONTROLLER_BUTTON_B),
		X(SDL.SDL_CONTROLLER_BUTTON_X),
		Y(SDL.SDL_CONTROLLER_BUTTON_Y),
		UP_ARROW(SDL.SDL_CONTROLLER_BUTTON_DPAD_UP),
		DOWN_ARROW(SDL.SDL_CONTROLLER_BUTTON_DPAD_DOWN),
		LEFT_ARROW(SDL.SDL_CONTROLLER_BUTTON_DPAD_LEFT),
		RIGHT_ARROW(SDL.SDL_CONTROLLER_BUTTON_DPAD_RIGHT),
		START(SDL.SDL_CONTROLLER_BUTTON_START),
		SELECT(SDL.SDL_CONTROLLER_BUTTON_BACK),
		RIGHT_BUMPER(SDL.SDL_CONTROLLER_BUTTON_RIGHTSHOULDER),
		LEFT_BUMBER(SDL.SDL_CONTROLLER_BUTTON_LEFTSHOULDER);

		public final int buttonMask;

		Button(int mask){
			this.buttonMask = mask;
		}
	}

	@ConfigItem(
			keyName = "toggler",
			name = "Mouse capture toggle",
			description = "The controller button to toggle whether the mouse is captured or not.",
			position = 0
	)
	default Button toggleButton()
	{
		return Button.START;
	}
	@ConfigItem(
		keyName = "leftClick",
		name = "Left mouse click",
		description = "The controller button to simulate a left mouse click.",
			position = 1
	)
	default Button leftClickButton()
	{
		return Button.A;
	}
	@ConfigItem(
			keyName = "rightClick",
			name = "Right mouse click",
			description = "The controller button to simulate a right mouse click.",
			position = 2
	)
	default Button rightClickButton()
	{
		return Button.B;
	}
	@ConfigItem(
			keyName = "minimapFocus",
			name = "Minimap selector",
			description = "The controller button to focus the minimap.",
			position = 3
	)
	default Button minimapButton()
	{
		return Button.LEFT_BUMBER;
	}
	@ConfigItem(
			keyName = "inventoryFocus",
			name = "Inventory selector",
			description = "The controller button to focus the inventory.",
			position = 4
	)
	default Button inventoryButton()
	{
		return Button.RIGHT_BUMPER;
	}
/*	@ConfigItem(
			keyName = "fn",
			name = "Function key",
			description = "Should simulate a press to the fn key before simulating the F keys.",
			position = 5
	)
	default boolean simulateFN()
	{
		return false;
	}*/
	@ConfigItem(
			keyName = "prayersTab",
			name = "Prayer tab",
			description = "The controller button to open the prayers.",
			position = 6
	)
	default Button prayerButton()
	{
		return Button.UP_ARROW;
	}
	@ConfigItem(
			keyName = "spellsTab",
			name = "Spell book tab",
			description = "The controller button to open the spells.",
			position = 7
	)
	default Button spellsButton()
	{
		return Button.RIGHT_ARROW;
	}
	@ConfigItem(
			keyName = "combatTab",
			name = "Combat tab",
			description = "The controller button to open the combat tab.",
			position = 8
	)
	default Button combatButton()
	{
		return Button.LEFT_ARROW;
	}
	@ConfigItem(
			keyName = "inventoryTab",
			name = "Inventory tab",
			description = "The controller button to open the inventory.",
			position = 9
	)
	default Button inventoryTabButton()
	{
		return Button.DOWN_ARROW;
	}
}
