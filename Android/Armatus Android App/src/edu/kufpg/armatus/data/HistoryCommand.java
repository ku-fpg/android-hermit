package edu.kufpg.armatus.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.primitives.Ints;

import android.os.Parcel;
import android.os.Parcelable;

public class HistoryCommand implements Comparable<HistoryCommand>, Parcelable {
	private static final String FROM = "from", CMD = "cmd", TO = "to";
	
	private final int mFrom, mTo;
	private final String mCommand;
	
	public HistoryCommand(int from, String command, int to) {
		mFrom = from;
		mCommand = command;
		mTo = to;
	}
	
	public HistoryCommand(JSONObject o) throws JSONException {
		this(o.getInt(FROM), o.getString(CMD), o.getInt(TO));
	}
	
	public int getFrom() {
		return mFrom;
	}
	
	public String getCommand() {
		return mCommand;
	}
	
	public int getTo() {
		return mTo;
	}
	
	public static Parcelable.Creator<HistoryCommand> CREATOR =
			new Parcelable.Creator<HistoryCommand>() {
		@Override
		public HistoryCommand createFromParcel(Parcel source) {
			int from = source.readInt();
			String command = source.readString();
			int to = source.readInt();
			return new HistoryCommand(from, command, to);
		}

		@Override
		public HistoryCommand[] newArray(int size) {
			return new HistoryCommand[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mFrom);
		dest.writeString(mCommand);
		dest.writeInt(mTo);
	}

	@Override
	public int compareTo(HistoryCommand another) {
		return Ints.compare(getFrom(), another.getFrom());
	}

}
