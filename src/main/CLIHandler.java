package main;

public abstract class CLIHandler {

	public static void start(String[] args) {
		
		System.out.print("RESPONSE:");
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equalsIgnoreCase("--command") && args.length > i+1) {
				switch(args[i+1]) {
					case "get_my_dm_address":
						getMyDmAddress();
						break;
				}
			}
		}
	}

	private static void getMyDmAddress() {
		System.out.println(Main.dmAddress);
	}

}
