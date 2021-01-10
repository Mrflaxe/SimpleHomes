package ru.mrflaxe.simplehomes.commands;

import ru.mrflaxe.simplehomes.SimpleHomes;
import ru.mrflaxe.simplehomes.commands.sub.SubcommandHelp;
import ru.mrflaxe.simplehomes.commands.sub.SubcommandReload;
import ru.soknight.lib.command.preset.ModifiedDispatcher;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;

public class SubcommandHandler extends ModifiedDispatcher {

	public SubcommandHandler(SimpleHomes plugin, Messages messages, Configuration config) {
		super("simplehomes", messages);
		
		super.setExecutor("help", new SubcommandHelp(messages));
		super.setExecutor("reload", new SubcommandReload(messages, config));
		
		super.register(plugin);
	}

}
