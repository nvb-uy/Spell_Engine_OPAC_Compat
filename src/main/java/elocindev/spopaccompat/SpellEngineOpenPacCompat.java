package elocindev.spopaccompat;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpellEngineOpenPacCompat implements ModInitializer {
	public static final String MODID = "spell-engine-openpac-compat";

	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		LOGGER.info("Spell Engine x Open Parties and Claims");
	}
}