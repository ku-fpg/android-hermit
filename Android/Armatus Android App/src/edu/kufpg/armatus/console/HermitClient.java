package edu.kufpg.armatus.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import edu.kufpg.armatus.BaseActivity;
import edu.kufpg.armatus.PrefsActivity;
import edu.kufpg.armatus.command.CustomCommandDispatcher;
import edu.kufpg.armatus.command.CommandGroup;
import edu.kufpg.armatus.networking.BluetoothUtils;
import edu.kufpg.armatus.networking.HermitHttpGetRequest;
import edu.kufpg.armatus.networking.HermitHttpPostRequest;
import edu.kufpg.armatus.networking.InternetUtils;
import edu.kufpg.armatus.util.StringUtils;

public class HermitClient {

	private ConsoleActivity mConsole;

	private enum RequestName {
		CONNECT, COMMAND, COMMANDS
	};

	private RequestName mRequestName;
	private HermitHttpGetRequest<Token> connectRequest;
	private HermitHttpPostRequest<CommandResponse> runCommandRequest;
	private HermitHttpPostRequest<List<CommandInfo>> fetchCommandsRequest;

	public HermitClient(ConsoleActivity console) {
		mConsole = console;
		connectRequest = new HermitHttpGetRequest<Token>(mConsole) {

			@Override
			protected Token onResponse(String response) {
				// TODO Auto-generated method stub

				try {
					return new Token(new JSONObject(response));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
			
			@Override
			protected void onPostExecute(Token token) {
				super.onPostExecute(token);
				fetchCommands();
			}

		};
		runCommandRequest = new HermitHttpPostRequest<CommandResponse>(mConsole) {

			@Override
			protected CommandResponse onResponse(String response) {
				// TODO Auto-generated method stub

				try {
					return new CommandResponse(new JSONObject(response));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(CommandResponse response) {
				super.onPostExecute(response);
				getActivity().appendConsoleEntry(PrettyPrinter.createPrettyText(response.glyphs));
			}

		};
		fetchCommandsRequest = new HermitHttpPostRequest<List<CommandInfo>>(
				mConsole) {

			@Override
			protected List<CommandInfo> onResponse(String response) {
				// TODO Auto-generated method stub
				JSONObject insertNameHere = null;
				try {
					insertNameHere = new JSONObject(response);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (insertNameHere == null) {
					return null;
				} else {
					try {
						JSONArray cmds = insertNameHere.getJSONArray("cmds");
						List<CommandInfo> commandList = new ArrayList<CommandInfo>();
						for (int i = 0; i < cmds.length(); i++) {
							JSONObject cmdInfo = cmds.getJSONObject(i);
							String name = cmdInfo.getString("name");
							String help = cmdInfo.getString("help");
							JSONArray tags = cmdInfo.getJSONArray("tags");
							List<String> tagList = new ArrayList<String>();
							for (int j = 0; j < tags.length(); j++) {
								tagList.add(tags.getString(j));
								commandList.add(new CommandInfo(name, help, tagList));
							}
						}

						return commandList;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}

				}
			}

			@Override
			protected void onPostExecute(List<CommandInfo> commands) {
				super.onPostExecute(commands);
				ImmutableSortedSet.Builder<String> tagSetBuilder = ImmutableSortedSet.naturalOrder();
				ImmutableListMultimap.Builder<String, String> tagMapBuilder = ImmutableListMultimap.builder();
				ImmutableSortedSet.Builder<String> commandSetBuilder = ImmutableSortedSet.naturalOrder();

				for (CommandInfo cmdInfo : commands) {
					commandSetBuilder.add(cmdInfo.name);
					for (String tag : cmdInfo.tags) {
						tagSetBuilder.add(tag);
						tagMapBuilder.put(tag, cmdInfo.name);
					}
				}
				getActivity().initCommandRelatedVariables(commandSetBuilder.build(), new ArrayList<String>(tagSetBuilder.build()),
						tagMapBuilder.build());
			}

		};
	}

	private boolean isConnected(ConsoleActivity console, RequestName name) {
		String server = PrefsActivity.getPrefs(console).getString(
				BaseActivity.NETWORK_SOURCE_KEY, null);
		if (BaseActivity.NETWORK_SOURCE_BLUETOOTH_SERVER.equals(server)) {
			if (BluetoothUtils.isBluetoothEnabled(console)) {
				if (BluetoothUtils.getBluetoothDevice(console) != null) {
					return true;
				} else {
					notifyDelay(name);
					BluetoothUtils.findDeviceName(console);
				}
			} else {
				notifyDelay(name);
				BluetoothUtils.enableBluetooth(console);
			}
		} else if (BaseActivity.NETWORK_SOURCE_WEB_SERVER.equals(server)) {
			if (InternetUtils.isAirplaneModeOn(console)) {
				console.appendConsoleEntry("Error: Please disable airplane mode before attempting to connect.");
			} else if (!InternetUtils.isWifiConnected(console)
					&& !InternetUtils.isMobileConnected(console)) {
				notifyDelay(name);
				InternetUtils.enableWifi(console);
			} else {
				return true;
			}
		}
		return false;
	}

	public void connect() {
		// remember the server to talk to,
		// and initialize the class-specific token.
		if (isConnected(mConsole, RequestName.CONNECT)) {
			try {
				String result = connection.post("/connect", "");
				JSONObject jsonToken = new JSONObject(result);

				mToken = new Token(jsonToken);
				this.connection = connection;
			} catch (Exception e) {
				throw new Error("something bad has happened");
			}
		}
	}

	public void runCommand(String str) {
		String[] words = str.split(StringUtils.WHITESPACE);
		if (CustomCommandDispatcher.isCustomCommand(words[0])) {
			
			CustomCommandDispatcher.runCustomCommand(mConsole, words[0],
					Arrays.copyOfRange(words, 1, words.length));
		} else {
			if (isConnected(mConsole, RequestName.COMMAND)) {
				JSONObject o = new JSONObject();
				try {
					o.put("token", mToken.toJSONObject());
					o.put("cmd", str);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				try {
					String result = connection.post("/command", o.toString());
					// return new HermitClient.CommandResponse(new
					// JSONObject(result));
				} catch (Exception e) {
					throw new Error("something bad has happened");
				}
			}

		}
	}

	public void fetchCommands() {
		if (isConnected(mConsole, RequestName.COMMANDS)) {

		}
	}

	public class CommandInfo {
		public final String name, help;
		public final List<String> tags;

		public CommandInfo(String name, String help, List<String> tags) {
			this.name = name;
			this.help = help;
			this.tags = tags;
		}

	}

	public static List<Glyph> listOfGlyphs(JSONArray a) {
		List<Glyph> list = new ArrayList<Glyph>();
		for (int i = 0; i < a.length(); i++) {
			try {
				list.add(new Glyph(a.getJSONObject(i)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}

	public static class CommandResponse {
		public final Token token;
		public final List<Glyph> glyphs;

		public CommandResponse(JSONObject o) throws JSONException {
			this(new Token(o.getJSONObject("token")), listOfGlyphs(o
					.getJSONArray("glyphs")));
		}

		public CommandResponse(Token token, List<Glyph> glyphs) {
			this.token = token;
			this.glyphs = glyphs;
		}
	}

	public static enum GlyphStyle {
		NORMAL, KEYWORD, SYNTAX, VAR, TYPE, LIT
	}

	public static GlyphStyle getStyle(JSONObject o) {
		if (o.has("style")) {
			GlyphStyle mGlyphStyle = null;
			try {
				mGlyphStyle = GlyphStyle.valueOf(o.getString("style"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return mGlyphStyle;
		} else {
			return GlyphStyle.NORMAL;
		}

	}

	public static class Glyph {
		public final String text;
		public final GlyphStyle style;

		public Glyph(JSONObject o) throws JSONException {
			this(o.getString("text"), getStyle(o));
		}

		public Glyph(String text, GlyphStyle style) {
			this.text = text;
			this.style = style;
		}

	}

	public static class Token {
		public final int unique, token;

		public Token(JSONObject o) throws JSONException {
			this(o.getInt("unique"), o.getInt("token"));
		}

		public Token(int unique, int token) {
			this.unique = unique;
			this.token = token;
		}

		public JSONObject toJSONObject() {
			JSONObject o = new JSONObject();
			try {
				o.put("unique", unique);
				o.put("token", token);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return o;
		}
	}

	public void runDelayedRequest() {
		if (mRequestName != null) {
			switch (mRequestName) {
			case CONNECT:
				connect();
				break;
			case COMMAND:
				fetchCommands();
				break;
			case COMMANDS:
				fetchCommands();
				break;
			}
		}
	}

	private void notifyDelay(RequestName name) {
		mRequestName = name;
	}

	public boolean isRequestDelayed() {
		return mRequestName != null;
	}

}