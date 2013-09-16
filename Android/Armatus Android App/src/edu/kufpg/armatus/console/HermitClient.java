package edu.kufpg.armatus.console;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSortedSet;

import edu.kufpg.armatus.BaseActivity;
import edu.kufpg.armatus.PrefsActivity;
import edu.kufpg.armatus.networking.BluetoothUtils;
import edu.kufpg.armatus.networking.HermitHttpServerRequest;
import edu.kufpg.armatus.networking.HermitHttpServerRequest.HttpRequest;
import edu.kufpg.armatus.networking.InternetUtils;
import edu.kufpg.armatus.networking.data.Command;
import edu.kufpg.armatus.networking.data.CommandInfo;
import edu.kufpg.armatus.networking.data.CommandResponse;
import edu.kufpg.armatus.networking.data.Token;
import edu.kufpg.armatus.util.StringUtils;

public class HermitClient implements Parcelable {
	public static int NO_TOKEN = -1;

	private ConsoleActivity mConsole;
	private ProgressDialog mProgress;

	private RequestName mDelayedRequestName = RequestName.NULL;
	private String mServerUrl;
	private Token mToken;

	public HermitClient(ConsoleActivity console) {
		mConsole = console;
	}

	public void connect(String serverUrl) {
		mServerUrl = serverUrl;
		if (isNetworkConnected(RequestName.CONNECT)) {
			newConnectRequest().execute(mServerUrl + "/connect");
		}
	}

	public void runCommand(String input) {
		String[] inputs = input.trim().split(StringUtils.WHITESPACE);
		mConsole.addConsoleUserInputEntry(input);
		if (CustomCommandDispatcher.isCustomCommand(inputs[0])) {
			if (inputs.length == 1) {
				CustomCommandDispatcher.runCustomCommand(mConsole, inputs[0]);
			} else {
				CustomCommandDispatcher.runCustomCommand(mConsole, inputs[0],
						Arrays.copyOfRange(inputs, 1, inputs.length));
			}
		} else {
			if (isNetworkConnected(RequestName.COMMAND) && isTokenAcquired()) {
				Command command = new Command(mToken, StringUtils.withoutCharWrap(input));
				if (inputs[0].equals("abort") || inputs[0].equals("resume")) {
					newRunAbortResumeRequest().execute(mServerUrl + "/command", command.toString());
				} else {
					newRunCommandRequest().execute(mServerUrl + "/command", command.toString());
				}
			}

		}
	}

	public void fetchCommands() {
		if (isNetworkConnected(RequestName.COMMANDS)) {
			newFetchCommandsRequest().execute(mServerUrl + "/commands");
		}
	}

	private HermitHttpServerRequest<Token> newConnectRequest() {
		return new HermitHttpServerRequest<Token>(mConsole, HttpRequest.POST) {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				getActivity().setProgressBarVisibility(false);
				showProgressDialog(getActivity(), "Connecting...");
			}

			@Override
			protected void onActivityDetached() {
				if (mProgress != null) {
					mProgress.dismiss();
					mProgress = null;
				}
			}

			@Override
			protected void onActivityAttached() {
				if (mProgress == null) {
					showProgressDialog(getActivity(), "Connecting...");
				}
			}

			@Override
			protected Token onResponse(String response) {
				try {
					return new Token(new JSONObject(response));
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				dismissProgressDialog();
			}

			@Override
			protected void onPostExecute(Token token) {
				super.onPostExecute(token);
				mToken = token;
				dismissProgressDialog();
				getActivity().updateInput();
				fetchCommands();
			}

		};
	}

	private HermitHttpServerRequest<List<CommandInfo>> newFetchCommandsRequest() {
		return new HermitHttpServerRequest<List<CommandInfo>>(mConsole, HttpRequest.GET) {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				getActivity().setProgressBarVisibility(false);
				showProgressDialog(getActivity(), "Fetching commands...");
			}

			@Override
			protected void onActivityDetached() {
				if (mProgress != null) {
					mProgress.dismiss();
					mProgress = null;
				}
			}

			@Override
			protected void onActivityAttached() {
				if (mProgress == null) {
					showProgressDialog(getActivity(), "Fetching commands...");
				}
			}

			@Override
			protected List<CommandInfo> onResponse(String response) {
				JSONObject insertNameHere = null;
				try {
					insertNameHere = new JSONObject(response);
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
				try {
					JSONArray cmds = insertNameHere.getJSONArray("cmds");
					ImmutableList.Builder<CommandInfo> commandListBuilder = ImmutableList.builder();
					for (int i = 0; i < cmds.length(); i++) {
						commandListBuilder.add(new CommandInfo(cmds.getJSONObject(i)));
					}
					return commandListBuilder.build();
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				dismissProgressDialog();
			}

			@Override
			protected void onPostExecute(List<CommandInfo> commands) {
				super.onPostExecute(commands);
				ImmutableSortedSet.Builder<String> tagSetBuilder = ImmutableSortedSet.naturalOrder();
				ImmutableListMultimap.Builder<String, CommandInfo> tagMapBuilder = ImmutableListMultimap.builder();
				ImmutableSortedSet.Builder<String> commandSetBuilder = ImmutableSortedSet.naturalOrder();

				for (CommandInfo cmdInfo : commands) {
					commandSetBuilder.add(cmdInfo.getName());
					for (String tag : cmdInfo.getTags()) {
						tagSetBuilder.add(tag);
						tagMapBuilder.put(tag, cmdInfo);
					}
				}

				Commands.setCommandSet(commandSetBuilder.build());
				Commands.setTagList(ImmutableList.copyOf(tagSetBuilder.build()));
				Commands.setTagMap(tagMapBuilder.build());
				getActivity().getWordCompleter().resetFilter(getActivity().getInput());
				getActivity().updateCommandExpandableMenu();
				dismissProgressDialog();
			}

		};
	}

	private HermitHttpServerRequest<CommandResponse> newRunCommandRequest() {
		return new HermitHttpServerRequest<CommandResponse>(mConsole, HttpRequest.POST) {
			@Override
			protected CommandResponse onResponse(String response) {
				try {
					return new CommandResponse(new JSONObject(response));
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(CommandResponse response) {
				super.onPostExecute(response);
				mToken.setAst(response.getAst());
				getActivity().appendCommandResponse(response);
			}

		};
	}

	private HermitHttpServerRequest<String> newRunAbortResumeRequest() {
		return new HermitHttpServerRequest<String>(mConsole, HttpRequest.POST) {
			@Override
			protected String onResponse(String response) {
				try {
					return new JSONObject(response).getString("msg");
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(String response) {
				super.onPostExecute(response);
				mToken = null;
				getActivity().appendErrorResponse(response);
			}

		};
	}

	public void runDelayedRequest() {
		if (mDelayedRequestName != null) {
			switch (mDelayedRequestName) {
			case CONNECT:
				connect(mServerUrl);
				break;
			case COMMAND:
				fetchCommands();
				break;
			case COMMANDS:
				fetchCommands();
				break;
			default:
				break;
			}
		}
	}

	void attachConsole(ConsoleActivity console) {
		mConsole = console;
	}

	private void dismissProgressDialog() {
		if (mProgress != null) {
			mProgress.dismiss();
		}
	}

	public int getAst() {
		return (mToken != null) ? mToken.getAst() : NO_TOKEN;
	}

	private boolean isNetworkConnected(RequestName name) {
		String server = PrefsActivity.getPrefs(mConsole).getString(
				BaseActivity.NETWORK_SOURCE_KEY, null);
		if (BaseActivity.NETWORK_SOURCE_BLUETOOTH_SERVER.equals(server)) {
			if (BluetoothUtils.isBluetoothEnabled(mConsole)) {
				if (BluetoothUtils.getBluetoothDevice(mConsole) != null) {
					return true;
				} else {
					notifyDelay(name);
					BluetoothUtils.findDeviceName(mConsole);
				}
			} else {
				notifyDelay(name);
				BluetoothUtils.enableBluetooth(mConsole);
			}
		} else if (BaseActivity.NETWORK_SOURCE_WEB_SERVER.equals(server)) {
			if (InternetUtils.isAirplaneModeOn(mConsole)) {
				mConsole.appendErrorResponse("ERROR: Please disable airplane mode before attempting to connect.");
			} else if (!InternetUtils.isWifiConnected(mConsole)
					&& !InternetUtils.isMobileConnected(mConsole)) {
				notifyDelay(name);
				InternetUtils.enableWifi(mConsole);
			} else {
				return true;
			}
		}
		return false;
	}

	public boolean isRequestDelayed() {
		return !mDelayedRequestName.equals(RequestName.NULL);
	}

	private boolean isTokenAcquired() {
		if (mToken == null) {
			mConsole.appendErrorResponse("ERROR: No token (connect to server first).");
			return false;
		}
		return true;
	}

	private void notifyDelay(RequestName name) {
		mDelayedRequestName = name;
	}

	public void notifyDelayedRequestFinished() {
		mDelayedRequestName = RequestName.NULL;
	}

	private void showProgressDialog(Context context, String message) {
		mProgress = new ProgressDialog(context);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setMessage(message);
		mProgress.setCancelable(false);
		mProgress.show();
	}

	private enum RequestName {
		CONNECT, COMMAND, COMMANDS, NULL
	};

	public static final Parcelable.Creator<HermitClient> CREATOR
	= new Parcelable.Creator<HermitClient>() {
		public HermitClient createFromParcel(Parcel in) {
			return new HermitClient(in);
		}

		public HermitClient[] newArray(int size) {
			return new HermitClient[size];
		}
	};

	private HermitClient(Parcel in) {
		mDelayedRequestName = RequestName.values()[in.readInt()];
		mServerUrl = in.readString();
		mToken = in.readParcelable(HermitClient.class.getClassLoader());
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mDelayedRequestName.ordinal());
		dest.writeString(mServerUrl);
		dest.writeParcelable(mToken, flags);
	}

}
