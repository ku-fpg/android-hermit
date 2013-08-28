package edu.kufpg.armatus.console;

import java.io.Serializable;

import android.text.SpannableStringBuilder;

import edu.kufpg.armatus.console.HermitClient.CommandResponse;
import edu.kufpg.armatus.util.StringUtils;

/**
 * Contains values that describe an entry in {@link ConsoleListView}. This class
 * is primarily used for populating {@link android.view.View Views} in {@link
 * ConsoleEntryAdapter}.
 */
public class ConsoleEntry implements Serializable {
	private static final long serialVersionUID = -1808578272659814103L;
	
	/** 
	 * The unique entry number used to identify this entry. Entry numbers begin
	 * at 0 (although the first entry in the console may not be 0 since the number
	 * of entries could exceed the {@link ConsoleActivity#CONSOLE_ENTRY_LIMIT
	 * CONSOLE_ENTRY_LIMIT}).
	 */
	private int mNum;
	
	private String mUserInput;
	private CommandResponse mCommandResponse;
	private String mErrorResponse;
	
	private transient CharSequence mShortContents;

	public ConsoleEntry(int entryNum, String userInput) {
		this(entryNum, userInput, null, null);
	}
	
	public ConsoleEntry(int entryNum, String userInput, CommandResponse commandResponse) {
		this(entryNum, userInput, commandResponse, null);
	}
	
	public ConsoleEntry(int entryNum, String userInput, String errorResponse) {
		this(entryNum, userInput, null, errorResponse);
	}
	
	protected ConsoleEntry(int entryNum, String userInput, CommandResponse commandResponse, String errorResponse) {
		mNum = entryNum;
		mUserInput = userInput;
		mCommandResponse = commandResponse;
		mErrorResponse = errorResponse;
		
		SpannableStringBuilder builder = new SpannableStringBuilder();
		if (userInput != null) {
			builder.append(userInput).append("\n");
		}
		if (commandResponse != null) {
			builder.append(PrettyPrinter.createPrettyText(commandResponse.glyphs)).append("\n");
		}
		if (errorResponse != null) {
			builder.append(errorResponse).append("\n");
		}
		
		mShortContents = builder.delete(builder.length()-1, builder.length());
	}

	/**
	 * Constructs a new instance with the specified {@link ConsoleEntry}'s number
	 * and contents.
	 * @param entry the {@code ConsoleEntry} to copy.
	 */
	public ConsoleEntry(ConsoleEntry entry) {
		this(entry.getNum(), entry.getUserInput(), entry.getCommandResponse(), entry.getErrorResponse());
	}

	/**
	 * Returns the entry's unique number.
	 * @return the number used to identify this entry.
	 */
	public int getNum() {
		return mNum;
	}
	
	public String getUserInput() {
		return mUserInput;
	}
	
	public CommandResponse getCommandResponse() {
		return mCommandResponse;
	}
	
	public String getErrorResponse() {
		return mErrorResponse;
	}

	/**
	 * Returns this entry's contents without the {@code hermit<num> }prefix.
	 * @return the unadorned entry contents.
	 */
	public CharSequence getShortContents() {
		return mShortContents;
	}
	
	/**
	 * Returns this entry's contents including the {@code hermit<num> }prefix.
	 * @return the entry contents, including the prefix.
	 */
	public CharSequence getFullContents() {
		SpannableStringBuilder builder = new SpannableStringBuilder("hermit<").append(""+getNum())
				.append(">").append(StringUtils.NBSP).append(getShortContents());
		return builder;
	}
	
	public void appendCommandResponse(CommandResponse commandResponse) {
		mCommandResponse = commandResponse;
		SpannableStringBuilder builder = new SpannableStringBuilder(getShortContents()).append("\n")
				.append(PrettyPrinter.createPrettyText(mCommandResponse.glyphs));
		mShortContents = builder;
	}
	
	public void appendErrorResponse(String errorResponse) {
		mErrorResponse = errorResponse;
		SpannableStringBuilder builder = new SpannableStringBuilder(getShortContents()).append("\n")
				.append(mErrorResponse);
		mShortContents = builder;
	}

}