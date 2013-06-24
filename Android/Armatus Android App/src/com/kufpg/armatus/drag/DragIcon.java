package com.kufpg.armatus.drag;

import java.util.Locale;

import com.kufpg.armatus.BaseActivity;
import com.kufpg.armatus.console.CommandDispatcher;
import com.kufpg.armatus.console.CommandDispatcher.Command;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A draggable image that represents a Command that can be run on console entry Keywords.
 */
public class DragIcon extends ImageView {
	private String mCommandName, mCommandImagePath;

	public DragIcon(Context context) {
		this(context, null);
	}

	public DragIcon(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setOnLongClickListener(new DragViewClickListener());
	}

	public String getCommandName() {
		return mCommandName;
	}

	public void setCommandName(String commandName) {
		if (commandName == null) {
			mCommandName = "toast"; //Because toast is delicious
		} else if (CommandDispatcher.isAlias(commandName)) {
			mCommandName = CommandDispatcher.unaliasCommand(commandName);
		} else {
			mCommandName = commandName;
		}
		
		Command command = CommandDispatcher.getCommand(mCommandName);
		if (command == null) {
			mCommandName = "toast"; //It fills you up right
		}
		
		String pathCommand = new String(commandName);
		if (command.getCommandAlias() != null) {
			pathCommand = command.getCommandAlias();
		}

		String groupName = CommandDispatcher.getCommand(mCommandName).getGroupName();	
		groupName = groupName.replaceAll("[/ ]", "_").toLowerCase(Locale.US);
		mCommandImagePath = "command_" + groupName + "_" + pathCommand.replace("-", "").toLowerCase(Locale.US);

		int resid = getResources().getIdentifier(mCommandImagePath, "drawable", BaseActivity.PACKAGE_NAME);
		if (resid != 0) {
			setBackground(getResources().getDrawable(resid));
		}
	}

	public static boolean commandHasIcon(Context context, String commandName) {
		Command command = CommandDispatcher.getCommand(commandName);
		if (commandName == null || command == null) {
			return false;
		}
		String groupName = command.getGroupName();
		if (groupName == null) {
			return false;
		}
		groupName = groupName.replaceAll("[/ ]", "_").toLowerCase(Locale.US);
		
		String pathCommand = new String(commandName);
		if (command.getCommandAlias() != null) {
			pathCommand = command.getCommandAlias();
		}
		String path = "command_" + groupName + "_" + pathCommand.replace("-", "").toLowerCase(Locale.US);
		int resid = context.getResources().getIdentifier(path, "drawable", BaseActivity.PACKAGE_NAME);
		return resid != 0;
	}
}
