package ru.mrflaxe.simplehomes.commands;

import ru.mrflaxe.simplehomes.SimpleHomes;
import ru.mrflaxe.simplehomes.commands.sub.SubcommandHelp;
import ru.soknight.lib.command.preset.ModifiedDispatcher;
import ru.soknight.lib.configuration.Messages;

public class SubcommandHandler extends ModifiedDispatcher {

	public SubcommandHandler(SimpleHomes plugin, Messages messages) {
		super("simplehomes", messages);
		
		super.setExecutor("help", new SubcommandHelp(messages));
		
		super.register(plugin);
	}

}
