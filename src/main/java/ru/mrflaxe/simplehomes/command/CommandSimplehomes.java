package ru.mrflaxe.simplehomes.command;

import ru.mrflaxe.simplehomes.SimpleHomes;
import ru.mrflaxe.simplehomes.command.simplehomes.CommandHelp;
import ru.mrflaxe.simplehomes.command.simplehomes.CommandReload;
import ru.soknight.lib.command.preset.ModifiedDispatcher;
import ru.soknight.lib.configuration.Messages;

public class CommandSimplehomes extends ModifiedDispatcher {

	public CommandSimplehomes(SimpleHomes plugin, Messages messages) {
		super("simplehomes", messages);
		
		super.setExecutor("help", new CommandHelp(messages));
		super.setExecutor("reload", new CommandReload(plugin, messages));
		
		super.register(plugin, true);
	}

}
